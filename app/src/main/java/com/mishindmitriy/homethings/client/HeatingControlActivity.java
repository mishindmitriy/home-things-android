package com.mishindmitriy.homethings.client;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.mishindmitriy.homethings.client.databinding.ActivityHeatingControlBinding;

public class HeatingControlActivity extends MvpAppCompatActivity implements HeatingControlView {
    @InjectPresenter
    HeatingControlPresenter presenter;
    private ActivityHeatingControlBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_heating_control);
        final int maxTemperature = 30;
        final int minTemperature = 18;
        final int temperatureDelta = maxTemperature - minTemperature;
        binding.dayTempSeekBar.incrementProgressBy(1);
        binding.dayTempSeekBar.setMax(temperatureDelta);
        binding.dayTempSeekBar.setProgress(PreferencesHelper.get().getDaySettingTemperature() - 18);
        binding.dayTempSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    presenter.setDayTemperature(minTemperature + progress);
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

    @Override
    public void updateSettingDayTemp(int settingDayTemp) {
        binding.progressBar.setVisibility(View.GONE);
        binding.settingTemp.setVisibility(View.VISIBLE);
        binding.settingTemp.setText(
                String.format("Setting temp: %d ℃", settingDayTemp)
        );
    }

    @Override
    public void updateFirebaseSettingTemp(double settingDayTemp) {
        binding.progressBar.setVisibility(View.GONE);
        binding.firebaseTemp.setVisibility(View.VISIBLE);
        binding.firebaseTemp.setText(
                String.format("Firebase temp: %.0f ℃", settingDayTemp)
        );
    }

    @Override
    public void updateHumidityData(MonitoringData data) {
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
