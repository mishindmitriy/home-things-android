package com.mishindmitriy.homethings;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;
import org.reactivestreams.Publisher;

import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Function3;

/**
 * Created by mishindmitriy on 23.09.2017.
 */

public class FirebaseHelper {
    private static int DAY_START_HOUR = 7;
    private static int NIGHT_START_HOUR = 22;

    static {
        setHostOnline();
        getHostOnlineRef().onDisconnect().setValue(false);
    }

    public static DatabaseReference getMaintainedTemperatureRef() {
        return FirebaseDatabase.getInstance()
                .getReference("heating/monitoring/maintainedTemperature");
    }

    public static DatabaseReference getHostOnlineRef() {
        return FirebaseDatabase.getInstance()
                .getReference("hostOnline");
    }

    public static void updateHumidity(final double humidity) {
        updateValue(ValueType.humidity, humidity);
    }

    public static Disposable createSettingHostOnlineDisposable() {
        return Observable.interval(5, TimeUnit.SECONDS)
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        Logger.l("set host online");
                        getHostOnlineRef().setValue(true);
                    }
                });
    }

    public static Flowable<Double> createSettingTempObservable() {
        return Flowable.combineLatest(
                createTempFlowable(getDayTempReference()).startWith(18.0),
                createTempFlowable(getNightTempReference()).startWith(18.0),
                createDateTimeFlowable(),
                new Function3<Double, Double, DateTime, Double>() {
                    @Override
                    public Double apply(Double settingDayTemp, Double settingNightTemp, DateTime now) throws Exception {
                        return isDay(now) ? settingDayTemp : settingNightTemp;
                    }
                }

        )
                .doOnNext(new Consumer<Double>() {
                    @Override
                    public void accept(Double settingTemp) throws Exception {
                        getMaintainedTemperatureRef().setValue(settingTemp);
                    }
                });
    }

    private static Publisher<DateTime> createDateTimeFlowable() {
        return Flowable.interval(10, TimeUnit.SECONDS)
                .map(new Function<Long, DateTime>() {
                    @Override
                    public DateTime apply(Long aLong) throws Exception {
                        return DateTime.now();
                    }
                });
    }

    private static boolean isDay(DateTime now) {
        return now.getHourOfDay() >= DAY_START_HOUR
                && now.getHourOfDay() < NIGHT_START_HOUR;
    }

    private static DatabaseReference getNightTempReference() {
        return FirebaseDatabase.getInstance().getReference("heating/settings/nightTemp");
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

    private static void updateValue(final ValueType valueType, final double value) {
        final long timestamp = System.currentTimeMillis();
        FirebaseDatabase.getInstance().getReference("heating/monitoring/" + valueType.toString())
                .push()
                .runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        mutableData.child("timestamp").setValue(timestamp);
                        mutableData.child("value").setValue(value);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                    }
                });
    }

    public static void updateTemp(final double temp) {
        updateValue(ValueType.temp, temp);
    }

    private static void setHostOnline() {
        FirebaseDatabase.getInstance().getReference()
                .child("hostOnline")
                .setValue(true);
    }

    public static DatabaseReference getDayTempReference() {
        return FirebaseDatabase.getInstance().getReference("heating/settings/dayTemp");
    }

    public static DatabaseReference getBoilerIsRunRef() {
        return FirebaseDatabase.getInstance().getReference("heating/monitoring/boilerIsRun");
    }

    enum ValueType {
        humidity, temp
    }
}
