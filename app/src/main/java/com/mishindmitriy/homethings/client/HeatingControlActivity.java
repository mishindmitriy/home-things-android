package com.mishindmitriy.homethings.client;

import android.databinding.DataBindingUtil;
import android.os.Bundle;

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
    }

    @Override
    public void updateData(MonitoringData monitoringData) {
        binding.temp.setText(
                String.format("%.2f C", monitoringData.getTemp())
        );
        binding.humidity.setText(
                String.format("%.2f %s", monitoringData.getHumidity(), "%")
        );
    }
}
