package com.mishindmitriy.homethings.client;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.mishindmitriy.homethings.MonitoringData;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.subjects.BehaviorSubject;

import static com.mishindmitriy.homethings.FirebaseHelper.getDayTempReference;
import static com.mishindmitriy.homethings.FirebaseHelper.getNightTempReference;


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
                        .distinctUntilChanged()
                        .debounce(500, TimeUnit.MILLISECONDS)
                        .subscribe(new Consumer<Double>() {
                            @Override
                            public void accept(final Double temp) throws Exception {
                                getDayTempReference().runTransaction(new Transaction.Handler() {
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
                        .distinctUntilChanged()
                        .debounce(500, TimeUnit.MILLISECONDS)
                        .subscribe(new Consumer<Double>() {
                            @Override
                            public void accept(final Double integer) throws Exception {
                                getNightTempReference().runTransaction(new Transaction.Handler() {
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
                RxFabric.createSettingDayTempObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<Double>() {
                            @Override
                            public void accept(Double temperature) throws Exception {
                                getViewState().updateSettingDayTemp(temperature);
                            }
                        })
        );
        compositeDisposable.add(
                RxFabric.createSettingNightTempObservable()
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
                RxFabric.createHostOnlineFlowable()
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
        Observable<MonitoringData> dataObservable = RxFabric.createMonitoringObservable()
                .publish()
                .autoConnect();
        compositeDisposable.add(
                dataObservable.subscribe(new Consumer<MonitoringData>() {
                    @Override
                    public void accept(MonitoringData data) throws Exception {
                        getViewState().showLastSensorsData(data);
                    }
                })
        );
        compositeDisposable.add(
                dataObservable
                        .buffer(RxFabric.LIMIT, 1)
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
