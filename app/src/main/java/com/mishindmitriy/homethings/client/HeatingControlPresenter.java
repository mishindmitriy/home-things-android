package com.mishindmitriy.homethings.client;

import com.arellomobile.mvp.InjectViewState;
import com.arellomobile.mvp.MvpPresenter;
import com.google.firebase.database.FirebaseDatabase;

import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;


/**
 * Created by Dmitry on 30.09.17.
 */
@InjectViewState
public class HeatingControlPresenter extends MvpPresenter<HeatingControlView> {
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onFirstViewAttach() {
        super.onFirstViewAttach();
        compositeDisposable.add(
                Flowable.combineLatest(
                        FirebaseHelper.createHostOnlineFlowable(),
                        FirebaseHelper.createMonitoringFlowable(),
                        new BiFunction<Boolean, HeatingData, HeatingData>() {
                            @Override
                            public HeatingData apply(Boolean hostIsOnline, HeatingData heatingData) throws Exception {
                                heatingData.setHostIsOnline(hostIsOnline);
                                return heatingData;
                            }
                        }
                )
                        .subscribe(new Consumer<HeatingData>() {
                            @Override
                            public void accept(HeatingData HeatingData) throws Exception {
                                getViewState().updateHeatingData(HeatingData);
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
