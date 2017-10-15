package com.mishindmitriy.homethings.client;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.List;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.BehaviorSubject;


/**
 * Created by Dmitry on 30.09.17.
 */
@InjectViewState
public class HeatingControlPresenter extends MvpPresenter<HeatingControlView> {
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final BehaviorSubject<Integer> settingDayTempSubject = BehaviorSubject.create();
    private final BehaviorSubject<Integer> settingNightTempSubject = BehaviorSubject.create();

    public HeatingControlPresenter() {
        subscribeToFirebaseTempValues();
        subscribeToLocalTempValueAndSync();
        compositeDisposable.add(
                FirebaseHelper.createMaintainedTemperatureObservable()
                        .subscribe(new Consumer<Double>() {
                            @Override
                            public void accept(Double maintainedTemperature) throws Exception {
                                getViewState().updateMaintainedTemperature(maintainedTemperature);
                            }
                        })
        );
        compositeDisposable.add(
                FirebaseHelper.createBoilerIsRunObservable()
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean boilerIsRun) throws Exception {
                                getViewState().updateBoilerIsRun(boilerIsRun);
                            }
                        })
        );
    }

    private void subscribeToLocalTempValueAndSync() {
        compositeDisposable.add(
                settingDayTempSubject
                        .distinctUntilChanged()
                        .subscribe(new Consumer<Integer>() {
                            @Override
                            public void accept(final Integer integer) throws Exception {
                                FirebaseHelper.getSettingDayTempRef()
                                        .runTransaction(new Transaction.Handler() {
                                            @Override
                                            public Transaction.Result doTransaction(MutableData mutableData) {
                                                mutableData.setValue(integer);
                                                return Transaction.success(mutableData);
                                            }

                                            @Override
                                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                                            }
                                        });
                            }
                        })
        );
        compositeDisposable.add(
                settingNightTempSubject
                        .distinctUntilChanged()
                        .subscribe(new Consumer<Integer>() {
                            @Override
                            public void accept(final Integer integer) throws Exception {
                                FirebaseHelper.getSettingNightTempRef()
                                        .runTransaction(new Transaction.Handler() {
                                            @Override
                                            public Transaction.Result doTransaction(MutableData mutableData) {
                                                mutableData.setValue(integer);
                                                return Transaction.success(mutableData);
                                            }

                                            @Override
                                            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {

                                            }
                                        });
                            }
                        })
        );

    }

    private void subscribeToFirebaseTempValues() {
        compositeDisposable.add(
                FirebaseHelper.createSettingDayTempObservable()
                        .subscribe(new Consumer<Double>() {
                            @Override
                            public void accept(Double temperature) throws Exception {
                                getViewState().updateSettingDayTemp(temperature);
                            }
                        })
        );
        compositeDisposable.add(
                FirebaseHelper.createSettingNightTempObservable()
                        .subscribe(new Consumer<Double>() {
                            @Override
                            public void accept(Double temperature) throws Exception {
                                getViewState().updateSettingNightTemp(temperature);
                            }
                        })
        );
    }

    public void setDayTemperature(int dayTemperature) {
        settingDayTempSubject.onNext(dayTemperature);
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        compositeDisposable.add(
                FirebaseHelper.createHostOnlineFlowable()
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean online) throws Exception {
                                getViewState().setHostOnline(online);
                            }
                        })
        );
        compositeDisposable.add(
                FirebaseHelper.createMonitoringObservable(
                        FirebaseHelper.getTempMonitoringReference()
                )
                        .subscribe(new Consumer<List<MonitoringData>>() {
                            @Override
                            public void accept(List<MonitoringData> monitoringData) throws Exception {
                                getViewState().updateTemperatureData(monitoringData);
                            }
                        })
        );
        compositeDisposable.add(
                FirebaseHelper.createMonitoringObservable(
                        FirebaseHelper.getHumidityMonitoringReference()
                )
                        .subscribe(new Consumer<List<MonitoringData>>() {
                            @Override
                            public void accept(List<MonitoringData> monitoringData) throws Exception {
                                getViewState().updateHumidityData(monitoringData);
                            }
                        })
        );
    }

    @Override
    public void attachView(HeatingControlView view) {
        if (getAttachedViews().size() == 0) {
            FirebaseDatabase.getInstance().goOnline();
        }
        super.attachView(view);
    }

    @Override
    public void detachView(HeatingControlView view) {
        super.detachView(view);
        if (getAttachedViews().size() == 0) {
            FirebaseDatabase.getInstance().goOffline();
        }
    }

    @Override
    public void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }

    public void setNightTemperature(int nightTemperature) {
        settingNightTempSubject.onNext(nightTemperature);
    }
}
