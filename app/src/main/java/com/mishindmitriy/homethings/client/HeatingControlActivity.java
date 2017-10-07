package com.mishindmitriy.homethings.client;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.transition.TransitionManager;
import android.view.View;
import android.widget.SeekBar;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.mishindmitriy.homethings.client.databinding.ActivityHeatingControlBinding;

public class HeatingControlActivity extends MvpAppCompatActivity implements HeatingControlView {
    public static final int MAX_TEMPERATURE = 30;
    public static final int MIN_TEMPERATURE = 18;
    @InjectPresenter
    HeatingControlPresenter presenter;
    private ActivityHeatingControlBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_heating_control);
        final int temperatureDelta = MAX_TEMPERATURE - MIN_TEMPERATURE;
        binding.dayTempSeekBar.incrementProgressBy(1);
        binding.dayTempSeekBar.setMax(temperatureDelta);
        binding.dayTempSeekBar.setProgress(PreferencesHelper.get().getDaySettingTemperature() - MIN_TEMPERATURE);
        binding.dayTempSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    presenter.setDayTemperature(MIN_TEMPERATURE + progress);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    @Override
    public void updateTemperatureData(MonitoringData data) {
        beginTransaction();
        binding.progressBar.setVisibility(View.GONE);
        binding.temperature.setVisibility(View.VISIBLE);
        binding.temperature.setText(
                String.format("Temp: %.1f ℃", data.getValue())
        );
        binding.temperatureLastUpdate.setVisibility(View.VISIBLE);
        binding.temperatureLastUpdate.setText(
                String.format(
                        "last update %s", data.getJodaTime().toString("dd MMMM HH:mm:ss")
                )
        );
    }

    private void beginTransaction() {
        TransitionManager.beginDelayedTransition(binding.mainLayout);
    }

    @Override
    public void updateSettingDayTemp(int settingDayTemp) {
        beginTransaction();
        binding.settingTemp.setVisibility(View.VISIBLE);
        binding.dayTempSeekBar.setVisibility(View.VISIBLE);
        binding.settingTemp.setText(
                String.format("Setting temp: %d ℃", settingDayTemp)
        );
        binding.dayTempSeekBar.setProgress(settingDayTemp - MIN_TEMPERATURE);
    }

    @Override
    public void updateHumidityData(MonitoringData data) {
        beginTransaction();
        binding.progressBar.setVisibility(View.GONE);
        binding.humidity.setVisibility(View.VISIBLE);
        binding.humidity.setText(
                String.format("Humidity: %.0f %s", data.getValue(), "%")
        );
        binding.humidityLastUpdate.setVisibility(View.VISIBLE);
        binding.humidityLastUpdate.setText(
                String.format(
                        "last update %s", data.getJodaTime().toString("dd MMMM HH:mm:ss")
                )
        );
    }

    @Override
    public void setHostOnline(Boolean online) {
        binding.hostOnline.setText(
                String.format(
                        "Things host is %s",
                        online ? "online" : "offline"
                )
        );
    }
}
