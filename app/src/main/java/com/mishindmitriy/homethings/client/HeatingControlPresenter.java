package com.mishindmitriy.homethings.client;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.BehaviorSubject;


/**
 * Created by Dmitry on 30.09.17.
 */
@InjectViewState
public class HeatingControlPresenter extends MvpPresenter<HeatingControlView> {
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private final BehaviorSubject<Integer> settingTempSubject = BehaviorSubject.create();
    private final Disposable firstLoadSettingTemperatureDisposable;
    private Disposable firebaseSyncDisposable;

    public HeatingControlPresenter() {
        compositeDisposable.add(
                settingTempSubject
                        .startWith(PreferencesHelper.get().getDaySettingTemperature())
                        .distinctUntilChanged()
                        .doOnNext(new Consumer<Integer>() {
                            @Override
                            public void accept(Integer temperature) throws Exception {
                                PreferencesHelper.get().setDayTemperature(temperature);
                            }
                        })
                        .subscribe(new Consumer<Integer>() {
                            @Override
                            public void accept(Integer temperature) throws Exception {
                                getViewState().updateSettingDayTemp(temperature);
                            }
                        })
        );
        firstLoadSettingTemperatureDisposable = FirebaseHelper.createSettingDayTempObservable()
                .subscribe(new Consumer<Double>() {
                    @Override
                    public void accept(Double settingDayTemp) throws Exception {
                        settingTempSubject.onNext((int) Math.round(settingDayTemp));
                        firstLoadSettingTemperatureDisposable.dispose();
                        startUploadTemp();
                    }
                });
        compositeDisposable.add(firstLoadSettingTemperatureDisposable);
    }

    private void startUploadTemp() {
        firebaseSyncDisposable = settingTempSubject
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
                });
        compositeDisposable.add(firebaseSyncDisposable);
    }

    public void setDayTemperature(int temp) {
        firstLoadSettingTemperatureDisposable.dispose();
        if (firebaseSyncDisposable == null) startUploadTemp();
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
}
