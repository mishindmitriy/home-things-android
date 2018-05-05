package com.mishindmitriy.homethings.host;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.PeripheralManager;
import com.google.gson.Gson;
import com.mishindmitriy.homethings.FirebaseHelper;
import com.mishindmitriy.homethings.Logger;
import com.mishindmitriy.homethings.MonitoringData;

import org.reactivestreams.Publisher;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.flowables.ConnectableFlowable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Function3;
import io.reactivex.functions.Predicate;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

import static com.mishindmitriy.homethings.Config.PASCAL_FACTOR;

public class MainActivity extends Activity {
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .readTimeout(15, TimeUnit.SECONDS)
            .connectTimeout(15, TimeUnit.SECONDS)
            .build();
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Gpio gpio;

    private Flowable<SensorsData> createIntervalSensorsObservable() {
        final int timeout = 20;
        return Flowable.interval(timeout, TimeUnit.SECONDS)
                .flatMap(new Function<Long, Publisher<String>>() {
                    @Override
                    public Publisher<String> apply(Long aLong) throws Exception {
                        return createSensorsDataObservable()
                                .timeout(timeout, TimeUnit.SECONDS)
                                .onErrorReturnItem("");
                    }
                })
                .filter(new Predicate<String>() {
                    @Override
                    public boolean test(String s) throws Exception {
                        return !TextUtils.isEmpty(s);
                    }
                })
                .map(new Function<String, SensorsData>() {
                    @Override
                    public SensorsData apply(String s) throws Exception {
                        Logger.l("json from esp: " + s);
                        try {
                            return new Gson().fromJson(s, SensorsData.class);
                        } catch (NumberFormatException e) {
                            return new SensorsData();
                        }
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupGPIO();
        startMonitoring();
    }

    private void setupGPIO() {
        try {
            gpio = PeripheralManager.getInstance().openGpio("BCM4");
            gpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);
            gpio.setActiveType(Gpio.ACTIVE_HIGH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Flowable<String> createSensorsDataObservable() {
        return Flowable.create(new FlowableOnSubscribe<String>() {
            @Override
            public void subscribe(final FlowableEmitter<String> emitter) throws Exception {
                final Call call = httpClient.newCall(
                        new Request.Builder()
                                .get()
                                .url("http://192.168.1.131")
                                .build()
                );
                Logger.l("request esp data");
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Logger.l("request esp failed");
                        e.printStackTrace();
                        if (!emitter.isCancelled()) {
                            emitter.onError(e);
                            emitter.onComplete();
                        }
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        Logger.l("esp onResponse success " + response.isSuccessful());
                        if (response.isSuccessful()) {
                            ResponseBody body = response.body();
                            try {
                                if (body != null && !emitter.isCancelled()) {
                                    emitter.onNext(body.string());
                                    emitter.onComplete();
                                }
                            } finally {
                                if (body != null) {
                                    body.close();
                                }
                            }
                        } else {
                            if (!emitter.isCancelled()) {
                                emitter.onError(new IOException());
                                emitter.onComplete();
                            }
                        }
                    }
                });
                emitter.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        Logger.l("esp request canceled");
                        call.cancel();
                    }
                });
            }
        }, BackpressureStrategy.LATEST);
    }

    private void startMonitoring() {
        final Flowable<SensorsData> sensorsDataFlowable = createIntervalSensorsObservable()
                .onBackpressureLatest()
                .publish()
                .autoConnect();

        final Flowable<Double> settingTempFlowable = RxFabric.createSettingTempFlowable()
                .onBackpressureLatest()
                .publish()
                .autoConnect();

        compositeDisposable.add(
                Flowable.combineLatest(
                        sensorsDataFlowable,
                        settingTempFlowable,
                        createBoilerControlFlowable(sensorsDataFlowable, settingTempFlowable)
                                .doOnNext(new Consumer<Boolean>() {
                                    @Override
                                    public void accept(Boolean needBoilerRun) throws Exception {
                                        runBoiler(needBoilerRun);
                                    }
                                }),
                        new Function3<SensorsData, Double, Boolean, MonitoringData>() {
                            @Override
                            public MonitoringData apply(SensorsData sensorsData, Double maintainedTemperature, Boolean boilerIsRun) throws Exception {
                                MonitoringData data = new MonitoringData();
                                data.boilerIsRun = boilerIsRun;
                                data.maintainedTemperature = maintainedTemperature;
                                data.temperature = sensorsData.getTemperature();
                                data.humidity = sensorsData.getHumidity();
                                data.pressure = sensorsData.getPressure() * PASCAL_FACTOR;
                                data.ppm = sensorsData.getPpm();
                                data.timestamp = System.currentTimeMillis();
                                return data;
                            }
                        }
                )
                        .subscribe(new Consumer<MonitoringData>() {
                            @Override
                            public void accept(MonitoringData data) throws Exception {
                                FirebaseHelper.pushData(data);
                            }
                        })
        );
    }

    private Flowable<Boolean> createBoilerControlFlowable(Flowable<SensorsData> sensorsDataFlowable,
                                                          Flowable<Double> settingTempFlowable) {
        return ConnectableFlowable.combineLatest(
                createTempMonitoringObservable(sensorsDataFlowable)
                        .buffer(5, 1)
                        .map(new Function<List<Double>, Double>() {
                            @Override
                            public Double apply(List<Double> doubles) throws Exception {
                                double sum = 0;
                                for (Double d : doubles) {
                                    sum += d;
                                }
                                return sum / doubles.size();
                            }
                        }),
                settingTempFlowable,
                new BiFunction<Double, Double, Boolean>() {
                    @Override
                    public Boolean apply(Double realTemp, Double settingTemp) throws Exception {
                        Logger.l("real average temp from 5 values " + realTemp + "; maintained temp " + settingTemp);
                        return realTemp < settingTemp;
                    }
                }
        ).distinctUntilChanged();
    }

    private void runBoiler(boolean needBoilerRun) {
        Logger.l("need boiler run " + needBoilerRun);
        try {
            gpio.setValue(needBoilerRun);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Flowable<Double> createTempMonitoringObservable(Flowable<SensorsData> monitoringDataFlowable) {
        return monitoringDataFlowable
                .map(new Function<SensorsData, Double>() {
                    @Override
                    public Double apply(SensorsData data) throws Exception {
                        return data.getTemperature();
                    }
                })
                .filter(new Predicate<Double>() {
                    @Override
                    public boolean test(Double aDouble) throws Exception {
                        return aDouble > 0;
                    }
                });
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.dispose();
        if (gpio != null) {
            try {
                gpio.close();
                gpio = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        super.onDestroy();
    }
}
