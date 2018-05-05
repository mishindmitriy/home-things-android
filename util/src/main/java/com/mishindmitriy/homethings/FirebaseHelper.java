package com.mishindmitriy.homethings;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Cancellable;

public class FirebaseHelper {
    public static DatabaseReference getHostOnlineRef() {
        return FirebaseDatabase.getInstance().getReference("hostOnline");
    }

    public static DatabaseReference getNightTempReference() {
        return FirebaseDatabase.getInstance().getReference("heating/settings/nightTemp");
    }

    public static DatabaseReference getDayTempReference() {
        return FirebaseDatabase.getInstance().getReference("heating/settings/dayTemp");
    }

    public static DatabaseReference getMonitoringReference() {
        return FirebaseDatabase.getInstance().getReference("heating/monitoring/");
    }

    public static DatabaseReference getLogReference() {
        return FirebaseDatabase.getInstance().getReference("host-log");
    }

    public static Observable<DataSnapshot> createQueryValueObservable(final Query query) {
        return Observable.create(new ObservableOnSubscribe<DataSnapshot>() {
            @Override
            public void subscribe(final ObservableEmitter<DataSnapshot> e) throws Exception {
                final ValueEventListener listener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        e.onNext(dataSnapshot);
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

    public static void pushData(final MonitoringData data) {
        Logger.l("push data: " + data);
        FirebaseHelper.getHostOnlineRef().setValue(true);
        getMonitoringReference()
                .push()
                .runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {
                        mutableData.setValue(data);
                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                        if (databaseError != null) {
                            Logger.l(databaseError.toString());
                        }
                    }
                });
    }
}
