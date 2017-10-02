package com.mishindmitriy.homethings.client;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;

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
    public void updateHeatingData(HeatingData data) {
        binding.progressBar.setVisibility(View.GONE);
        binding.temp.setVisibility(View.VISIBLE);
        binding.humidity.setVisibility(View.VISIBLE);
        binding.lastUpdate.setVisibility(View.VISIBLE);
        binding.temp.setText(
                String.format("Temp: %.1f C", data.getTemp())
        );
        binding.humidity.setText(
                String.format("Humidity: %.0f %s", data.getHumidity(), "%")
        );
        binding.lastUpdate.setText(
                String.format(
                        "last update %s", data.getJodaTime().toString("dd MMMM HH:mm:ss")
                )
        );
    }

    @Override
    public void setHostOnline(Boolean online) {
        binding.hostOnline.setText(
                String.format(
                        "Heating host is %s",
                        online ? "online" : "offline"
                )
        );
    }
}
