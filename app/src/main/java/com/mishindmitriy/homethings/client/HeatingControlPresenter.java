package com.mishindmitriy.homethings.client;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.BehaviorSubject;


/**
 * Created by Dmitry on 30.09.17.
 */
@InjectViewState
public class HeatingControlPresenter extends MvpPresenter<HeatingControlView> {
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final BehaviorSubject<Double> settingDayTempSubject = BehaviorSubject.create();
    private final BehaviorSubject<Double> settingNightTempSubject = BehaviorSubject.create();


    private void subscribeToLocalTempValueAndSync() {
        compositeDisposable.add(
                settingDayTempSubject
                        .debounce(500, TimeUnit.MILLISECONDS)
                        .distinctUntilChanged()
                        .subscribe(new Consumer<Double>() {
                            @Override
                            public void accept(final Double temp) throws Exception {
                                FirebaseHelper.getSettingDayTempRef()
                                        .runTransaction(new Transaction.Handler() {
                                            @Override
                                            public Transaction.Result doTransaction(MutableData mutableData) {
                                                mutableData.setValue(temp);
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
                        .debounce(500, TimeUnit.MILLISECONDS)
                        .distinctUntilChanged()
                        .subscribe(new Consumer<Double>() {
                            @Override
                            public void accept(final Double integer) throws Exception {
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
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Double>() {
                            @Override
                            public void accept(Double temperature) throws Exception {
                                getViewState().updateSettingDayTemp(temperature);
                            }
                        })
        );
        compositeDisposable.add(
                FirebaseHelper.createSettingNightTempObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Double>() {
                            @Override
                            public void accept(Double temperature) throws Exception {
                                getViewState().updateSettingNightTemp(temperature);
                            }
                        })
        );
    }

    public void setDayTemperature(double dayTemperature) {
        settingDayTempSubject.onNext(dayTemperature);
    }

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        compositeDisposable.add(
                FirebaseHelper.createHostOnlineFlowable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Boolean>() {
                            @Override
                            public void accept(Boolean online) throws Exception {
                                getViewState().setHostOnline(online);
                            }
                        })
        );
        subscribeToFirebaseTempValues();
        subscribeToLocalTempValueAndSync();
        compositeDisposable.add(
                FirebaseHelper.createMonitoringObservable()
                        .buffer(FirebaseHelper.LIMIT, 1)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<List<MonitoringData>>() {
                            @Override
                            public void accept(List<MonitoringData> monitoringData) throws Exception {
                                getViewState().updateMonitoringData(monitoringData);
                            }
                        })
        );
    }

    @Override
    public void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }

    public void setNightTemperature(double nightTemperature) {
        settingNightTempSubject.onNext(nightTemperature);
    }
}
