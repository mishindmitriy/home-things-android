package com.mishindmitriy.homethings;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.gson.Gson;

import org.reactivestreams.Publisher;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class MainActivity extends Activity {
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .readTimeout(4, TimeUnit.SECONDS)
            .connectTimeout(4, TimeUnit.SECONDS)
            .build();
    private final PeripheralManagerService peripheralManagerService = new PeripheralManagerService();
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Gpio gpio;

    private void log(String s) {
        Logger.l(s);
    }

    private Flowable<MonitoringData> createDataObservable() {
        return Flowable.interval(10, TimeUnit.SECONDS)
                .flatMap(new Function<Long, Publisher<String>>() {
                    @Override
                    public Publisher<String> apply(Long aLong) throws Exception {
                        return createReadDataObservable();
                    }
                })
                .map(new Function<String, MonitoringData>() {
                    @Override
                    public MonitoringData apply(String s) throws Exception {
                        if (TextUtils.isEmpty(s)) {
                            return new MonitoringData();
                        } else {
                            try {
                                return new Gson().fromJson(s, MonitoringData.class);
                            } catch (NumberFormatException e) {
                                return new MonitoringData();
                            }
                        }
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        compositeDisposable.add(FirebaseHelper.createSettingHostOnlineDisposable());
        startMonitoring();
        setupGPIO();
    }

    private void setupGPIO() {
        try {
            gpio = peripheralManagerService.openGpio("BCM4");
            gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            gpio.setActiveType(Gpio.ACTIVE_HIGH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Publisher<String> createReadDataObservable() {
        return Flowable.create(new FlowableOnSubscribe<String>() {
            @Override
            public void subscribe(final FlowableEmitter<String> emitter) throws Exception {
                final Call call = httpClient.newCall(
                        new Request.Builder()
                                .get()
                                .url("http://192.168.1.131")
                                .build()
                );
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        e.printStackTrace();
                        if (!emitter.isCancelled()) {
                            emitter.onError(e);
                        }
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        log("onResponse success " + response.isSuccessful());
                        if (response.isSuccessful()) {
                            ResponseBody body = response.body();
                            try {
                                if (body != null && !emitter.isCancelled()) {
                                    emitter.onNext(body.string());
                                }
                            } finally {
                                if (body != null) {
                                    body.close();
                                }
                            }
                        } else {
                            if (!emitter.isCancelled()) {
                                emitter.onError(new IOException());
                            }
                        }
                    }
                });
                emitter.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        log("esp request canceled");
                        call.cancel();
                    }
                });
            }
        }, BackpressureStrategy.LATEST)
                .timeout(10, TimeUnit.SECONDS)
                .onErrorReturnItem("");
    }

    private void startMonitoring() {
        Flowable<MonitoringData> monitoringDataFlowable = createDataObservable()
                .onBackpressureLatest()
                .publish()
                .autoConnect();

        compositeDisposable.add(
                createBoilerControlDisposable(monitoringDataFlowable)
        );

        compositeDisposable.add(
                createHumidityObservable(monitoringDataFlowable).subscribe()
        );
    }

    private Disposable createBoilerControlDisposable(Flowable<MonitoringData> monitoringDataFlowable) {
        return ConnectableFlowable.combineLatest(
                createTempMonitoringObservable(monitoringDataFlowable)
                        .buffer(5, 1)
                        .map(new Function<List<Double>, Double>() {
                            @Override
                            public Double apply(List<Double> doubles) throws Exception {
                                log("buffer doubles " + doubles);
                                double sum = 0;
                                for (Double d : doubles) {
                                    sum += d;
                                }
                                return sum / doubles.size();
                            }
                        }),
                FirebaseHelper.createSettingTempObservable(),
                new BiFunction<Double, Double, Boolean>() {
                    @Override
                    public Boolean apply(Double realTemp, Double settingTemp) throws Exception {
                        log("real temp " + realTemp + "; maintained temp " + settingTemp);
                        return realTemp < settingTemp;
                    }
                }
        )
                .doOnNext(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean needBoilerRun) throws Exception {
                        FirebaseHelper.getBoilerIsRunRef().setValue(needBoilerRun);
                        runBoiler(needBoilerRun);
                    }
                })
                .subscribe();
    }

    private void runBoiler(boolean needBoilerRun) {
        log("need boiler run " + needBoilerRun);
        try {
            gpio.setValue(needBoilerRun);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Flowable<Double> createHumidityObservable(Flowable<MonitoringData> monitoringDataFlowable) {
        return monitoringDataFlowable
                .map(new Function<MonitoringData, Double>() {
                    @Override
                    public Double apply(MonitoringData data) throws Exception {
                        return data.getHumidity();
                    }
                })
                .filter(new Predicate<Double>() {
                    @Override
                    public boolean test(Double aDouble) throws Exception {
                        return aDouble > 0;
                    }
                })
                .distinctUntilChanged()
                .doOnNext(new Consumer<Double>() {
                    @Override
                    public void accept(Double humidity) throws Exception {
                        log("humidity " + humidity);
                        FirebaseHelper.updateHumidity(humidity);
                    }
                });
    }

    private Flowable<Double> createTempMonitoringObservable(Flowable<MonitoringData> monitoringDataFlowable) {
        return monitoringDataFlowable
                .map(new Function<MonitoringData, Double>() {
                    @Override
                    public Double apply(MonitoringData data) throws Exception {
                        return data.getTemperature();
                    }
                })
                .filter(new Predicate<Double>() {
                    @Override
                    public boolean test(Double aDouble) throws Exception {
                        return aDouble > 0;
                    }
                })
                .doOnNext(new Consumer<Double>() {
                    @Override
                    public void accept(Double temp) throws Exception {
                        log("temp " + temp);
                        FirebaseHelper.updateTemp(temp);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }
}
