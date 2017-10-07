package com.mishindmitriy.homethings.client;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.BehaviorSubject;


/**
 * Created by Dmitry on 30.09.17.
 */
@InjectViewState
public class HeatingControlPresenter extends MvpPresenter<HeatingControlView> {
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final BehaviorSubject<Integer> settingTempSubject = BehaviorSubject.create();

    public HeatingControlPresenter() {
        compositeDisposable.add(
                settingTempSubject
                        .distinctUntilChanged()
                        .doOnNext(new Consumer<Integer>() {
                            @Override
                            public void accept(Integer temperature) throws Exception {
                                PreferencesHelper.get().setDayTemperature(temperature);
                            }
                        })
                        .startWith(PreferencesHelper.get().getDaySettingTemperature())
                        .subscribe(new Consumer<Integer>() {
                            @Override
                            public void accept(Integer temperature) throws Exception {
                                getViewState().updateSettingDayTemp(temperature);
                            }
                        })
        );
        compositeDisposable.add(
                settingTempSubject
                        .startWith(PreferencesHelper.get().getDaySettingTemperature())
                        .distinctUntilChanged()
                        .debounce(1, TimeUnit.SECONDS)
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
    }

    public void setDayTemperature(int temp) {
        settingTempSubject.onNext(temp);
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
                        .subscribe(new Consumer<MonitoringData>() {
                            @Override
                            public void accept(MonitoringData monitoringData) throws Exception {
                                getViewState().updateTemperatureData(monitoringData);
                            }
                        })
        );
        compositeDisposable.add(
                FirebaseHelper.createMonitoringObservable(
                        FirebaseHelper.getHumidityMonitoringReference()
                )
                        .subscribe(new Consumer<MonitoringData>() {
                            @Override
                            public void accept(MonitoringData monitoringData) throws Exception {
                                getViewState().updateHumidityData(monitoringData);
                            }
                        })
        );
        compositeDisposable.add(
                FirebaseHelper.createSettingDayTempObservable()
                        .subscribe(new Consumer<Double>() {
                            @Override
                            public void accept(Double settingDayTemp) throws Exception {
                                getViewState().updateFirebaseSettingTemp(settingDayTemp);
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
}
