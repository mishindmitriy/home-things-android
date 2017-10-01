package com.mishindmitriy.homethings.client;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.DateTime;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.functions.Cancellable;

/**
 * Created by Dmitry on 30.09.17.
 */

public class FirebaseHelper {
    private static DatabaseReference getHeatingMonitoringReference() {
        return FirebaseDatabase.getInstance().getReference("heating/monitor");
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

    public static Flowable<HeatingData> createMonitoringFlowable() {
        return Flowable.create(new FlowableOnSubscribe<HeatingData>() {
            @Override
            public void subscribe(final FlowableEmitter<HeatingData> e) throws Exception {
                final DatabaseReference ref = FirebaseHelper.getHeatingMonitoringReference();
                final ValueEventListener listener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null && dataSnapshot.exists()) {
                            HeatingData data = new HeatingData();
                            data.setTemp(dataSnapshot.child("temp").getValue(double.class));
                            data.setTemp(dataSnapshot.child("humidity").getValue(double.class));
                            data.setLastUpdate(
                                    new DateTime(
                                            dataSnapshot.child("lastUpdate").getValue(long.class)
                                    )
                            );
                            e.onNext(data);
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
}
