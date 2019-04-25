package com.mishindmitriy.homethings.host;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.mishindmitriy.homethings.Logger;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.reactivestreams.Publisher;

import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Function3;

import static com.mishindmitriy.homethings.Config.DAY_START_HOUR;
import static com.mishindmitriy.homethings.Config.HOST_TIMEZONE_OFFSET;
import static com.mishindmitriy.homethings.Config.MIN_TEMPERATURE;
import static com.mishindmitriy.homethings.Config.NIGHT_START_HOUR;
import static com.mishindmitriy.homethings.FirebaseHelper.getDayTempReference;
import static com.mishindmitriy.homethings.FirebaseHelper.getNightTempReference;

/**
 * Created by mishindmitriy on 23.09.2017.
 */

class RxFabric {
    static Flowable<Double> createSettingTempFlowable() {
        return Flowable.combineLatest(
                createTempFlowable(getDayTempReference()).startWith(MIN_TEMPERATURE),
                createTempFlowable(getNightTempReference()).startWith(MIN_TEMPERATURE),
                createDateTimeFlowable(),
                new Function3<Double, Double, DateTime, Double>() {
                    @Override
                    public Double apply(Double settingDayTemp, Double settingNightTemp, DateTime now) throws Exception {
                        Logger.l("day temp: " + settingDayTemp + "; night temp " + settingNightTemp);
                        return isDay(now.withZone(DateTimeZone.forOffsetHours(HOST_TIMEZONE_OFFSET)))
                                ? settingDayTemp : settingNightTemp;
                    }

                    private boolean isDay(DateTime now) {
                        final int hourOfDay = now.getHourOfDay();
                        Logger.l("hour of day: " + hourOfDay);
                        return hourOfDay >= DAY_START_HOUR && hourOfDay < NIGHT_START_HOUR;
                    }
                }
        ).distinctUntilChanged();
    }

    private static Publisher<DateTime> createDateTimeFlowable() {
        return Flowable.interval(20, TimeUnit.SECONDS)
                .map(new Function<Long, DateTime>() {
                    @Override
                    public DateTime apply(Long aLong) throws Exception {
                        return DateTime.now();
                    }
                });
    }

    private static Flowable<Double> createTempFlowable(final DatabaseReference tempReference) {
        return Flowable.create(new FlowableOnSubscribe<Double>() {
            @Override
            public void subscribe(final FlowableEmitter<Double> e) throws Exception {
                final ValueEventListener listener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null && dataSnapshot.exists()) {
                            double settingTemp = dataSnapshot.getValue(double.class);
                            e.onNext(settingTemp);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        e.onError(databaseError.toException());
                    }
                };
                tempReference.addValueEventListener(listener);
                e.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        tempReference.removeEventListener(listener);
                    }
                });
            }
        }, BackpressureStrategy.LATEST);
    }
}
