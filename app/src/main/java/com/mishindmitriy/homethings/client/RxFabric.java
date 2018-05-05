package com.mishindmitriy.homethings.client;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.mishindmitriy.homethings.MonitoringData;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

import static com.mishindmitriy.homethings.FirebaseHelper.createQueryValueObservable;
import static com.mishindmitriy.homethings.FirebaseHelper.getDayTempReference;
import static com.mishindmitriy.homethings.FirebaseHelper.getHostOnlineRef;
import static com.mishindmitriy.homethings.FirebaseHelper.getMonitoringReference;
import static com.mishindmitriy.homethings.FirebaseHelper.getNightTempReference;

/**
 * Created by Dmitry on 30.09.17.
 */

public class RxFabric {
    public static final int LIMIT = 60;

    public static Flowable<Boolean> createHostOnlineFlowable() {
        return Flowable.create(new FlowableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(final FlowableEmitter<Boolean> e) throws Exception {
                final DatabaseReference ref = getHostOnlineRef();
                final ValueEventListener listener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null && dataSnapshot.exists()) {
                            e.onNext(dataSnapshot.getValue(Boolean.class));
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        e.onError(databaseError.toException());
                    }
                };
                ref.addValueEventListener(listener);
                e.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        ref.removeEventListener(listener);
                    }
                });
            }
        }, BackpressureStrategy.LATEST);
    }

    public static Observable<MonitoringData> createMonitoringObservable() {
        return createQuerySingleEventObservable(getMonitoringReference()
                .orderByChild("timestamp")
                .limitToLast(LIMIT))
                .filter(new Predicate<DataSnapshot>() {
                    @Override
                    public boolean test(DataSnapshot dataSnapshot) throws Exception {
                        return dataSnapshot.exists();
                    }
                })
                .map(new Function<DataSnapshot, MonitoringData>() {
                    @Override
                    public MonitoringData apply(DataSnapshot dataSnapshot) throws Exception {
                        return dataSnapshot.getValue(MonitoringData.class);
                    }
                });
    }


    private static Observable<DataSnapshot> createQuerySingleEventObservable(final Query query) {
        return Observable.create(new ObservableOnSubscribe<DataSnapshot>() {
            @Override
            public void subscribe(final ObservableEmitter<DataSnapshot> e) throws Exception {
                final ValueEventListener listener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //e.onNext(dataSnapshot);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        e.onError(databaseError.toException());
                    }
                };
                final ChildEventListener childListener = new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        e.onNext(dataSnapshot);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };
                query.addListenerForSingleValueEvent(listener);
                query.addChildEventListener(childListener);
                e.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        query.removeEventListener(listener);
                        query.removeEventListener(childListener);
                    }
                });
            }
        })
                .observeOn(Schedulers.newThread());
    }

    public static Observable<Double> createSettingDayTempObservable() {
        return createQueryValueObservable(getDayTempReference())
                .map(new Function<DataSnapshot, Double>() {
                    @Override
                    public Double apply(DataSnapshot dataSnapshot) throws Exception {
                        if (dataSnapshot.exists()) return dataSnapshot.getValue(double.class);
                        else return 18.0;
                    }
                });
    }

    public static Observable<Double> createSettingNightTempObservable() {
        return createQueryValueObservable(getNightTempReference())
                .map(new Function<DataSnapshot, Double>() {
                    @Override
                    public Double apply(DataSnapshot dataSnapshot) throws Exception {
                        if (dataSnapshot.exists()) return dataSnapshot.getValue(double.class);
                        else return 18.0;
                    }
                });
    }


}
