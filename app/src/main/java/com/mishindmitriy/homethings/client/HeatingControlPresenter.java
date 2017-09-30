package com.mishindmitriy.homethings.client;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;


/**
 * Created by Dmitry on 30.09.17.
 */
@InjectViewState
public class HeatingControlPresenter extends MvpPresenter<HeatingControlView> {
    public HeatingControlPresenter() {
        createMonitoringFlowable()
                .subscribe(new Consumer<MonitoringData>() {
                    @Override
                    public void accept(MonitoringData monitoringData) throws Exception {
                        getViewState().updateData(monitoringData);
                    }
                });
    }

    private Flowable<MonitoringData> createMonitoringFlowable() {
        return Flowable.create(new FlowableOnSubscribe<MonitoringData>() {
            @Override
            public void subscribe(final FlowableEmitter<MonitoringData> e) throws Exception {
                final DatabaseReference ref = FirebaseHelper.getHeatingMonitoringReference();
                final ValueEventListener listener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot != null && dataSnapshot.exists()) {
                            MonitoringData data = new MonitoringData();
                            data.setTemp(dataSnapshot.child("temp").getValue(double.class));
                            data.setTemp(dataSnapshot.child("humidity").getValue(double.class));
                            e.onNext(data);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };
                ref.addValueEventListener(listener);
                e.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {

                    }
                });
                //ref.removeEventListener(listener);
            }
        }, BackpressureStrategy.LATEST);
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
