package com.mishindmitriy.homethings.client;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Function;

/**
 * Created by Dmitry on 30.09.17.
 */

public class FirebaseHelper {
    public static Query getTempMonitoringReference() {
        return FirebaseDatabase.getInstance().getReference("heating/monitoring/temp")
                .orderByChild("timestamp")
                .limitToLast(1);
    }

    public static Query getHumidityMonitoringReference() {
        return FirebaseDatabase.getInstance().getReference("heating/monitoring/humidity")
                .orderByChild("timestamp")
                .limitToLast(1);
    }

    private static DatabaseReference getHostOnlineRef() {
        return FirebaseDatabase.getInstance().getReference("hostOnline");
    }


    public static Flowable<Boolean> createHostOnlineFlowable() {
        return Flowable.create(new FlowableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(final FlowableEmitter<Boolean> e) throws Exception {
                final DatabaseReference ref = FirebaseHelper.getHostOnlineRef();
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

    public static Observable<MonitoringData> createMonitoringObservable(final Query query) {
        return createQueryObservable(query)
                .map(new Function<DataSnapshot, MonitoringData>() {
                    @Override
                    public MonitoringData apply(DataSnapshot dataSnapshot) throws Exception {
                        return dataSnapshot.getChildren()
                                .iterator().next()
                                .getValue(MonitoringData.class);
                    }
                });
    }

    private static Observable<DataSnapshot> createQueryObservable(final Query query) {
        return Observable.create(new ObservableOnSubscribe<DataSnapshot>() {
            @Override
            public void subscribe(final ObservableEmitter<DataSnapshot> e) throws Exception {
                final ValueEventListener listener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null && dataSnapshot.exists()) {
                            e.onNext(dataSnapshot);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        e.onError(databaseError.toException());
                    }
                };
                query.addValueEventListener(listener);
                e.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        query.removeEventListener(listener);
                    }
                });
            }
        });
    }

    public static Observable<Double> createSettingDayTempObservable() {
        return createQueryObservable(getSettingDayTempRef())
                .map(new Function<DataSnapshot, Double>() {
                    @Override
                    public Double apply(DataSnapshot dataSnapshot) throws Exception {
                        return dataSnapshot.getValue(double.class);
                    }
                });
    }

    public static DatabaseReference getSettingDayTempRef() {
        return FirebaseDatabase.getInstance().getReference("heating/settings/dayTemp");
    }
}
