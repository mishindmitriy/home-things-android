package com.mishindmitriy.homethings.client;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.SeekBar;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.db.chart.model.LineSet;
import com.mishindmitriy.homethings.client.databinding.ActivityHeatingControlBinding;

import org.joda.time.DateTime;

import java.util.List;

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
        binding.dayTempSeekBar.setProgress(MIN_TEMPERATURE);
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
        binding.nightTempSeekBar.incrementProgressBy(1);
        binding.nightTempSeekBar.setMax(temperatureDelta);
        binding.nightTempSeekBar.setProgress(MIN_TEMPERATURE);
        binding.nightTempSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    presenter.setNightTemperature(MIN_TEMPERATURE + progress);
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
    public void updateMonitoringData(final List<MonitoringData> data) {
        binding.progressBar.setVisibility(View.GONE);
        binding.chart.reset();
        binding.chart.addData(
                createEntryList(data, Field.maintainedTemperature)
                        .setSmooth(true)
                        .setFill(ContextCompat.getColor(this, R.color.maintainedTemperature))
        );
        binding.chart.addData(
                createEntryList(data, Field.temperature)
                        .setColor(ContextCompat.getColor(this, R.color.temperature))
                        .setSmooth(true)
                //.setFill(color)
        );
        binding.chart.addData(
                createEntryList(data, Field.humidity)
                        .setColor(ContextCompat.getColor(this, R.color.humidity))
                        .setSmooth(true)
        );
        binding.chart.addData(
                createEntryList(data, Field.boilerIsRun)
                        .setColor(Color.GRAY)
                        .setSmooth(true)
        );
        binding.chart.show();
        binding.chart.setVisibility(View.VISIBLE);
    }

    private LineSet createEntryList(List<MonitoringData> monitoringData, Field field) {
        final String[] labels = new String[monitoringData.size()];
        final float[] values = new float[monitoringData.size()];
        for (int i = 0; i < monitoringData.size(); i++) {
            switch (field) {
                case humidity:
                    values[i] = (float) monitoringData.get(i).humidity;
                    break;
                case temperature:
                    values[i] = (float) monitoringData.get(i).temperature;
                    break;
                case boilerIsRun:
                    values[i] = monitoringData.get(i).boilerIsRun ? 2 : 0;
                    break;
                case pressure:
                    break;
                case maintainedTemperature:
                    values[i] = (float) monitoringData.get(i).maintainedTemperature;
                    break;
            }
            final DateTime dateTime = monitoringData.get(i).getJodaTime();
            if ((i + 1) % (monitoringData.size() / 8) == 0) {
                labels[i] = DateTime.now().toLocalDate().isEqual(dateTime.toLocalDate())
                        ? dateTime.toString("HH:mm")
                        : dateTime.toString("dd MMMM HH");
            } else {
                labels[i] = "";
            }
        }
        return new LineSet(labels, values);
    }

    @Override
    public void updateSettingDayTemp(double dayTemperature) {
        binding.settingDayTemp.setVisibility(View.VISIBLE);
        binding.dayTempSeekBar.setVisibility(View.VISIBLE);
        binding.settingDayTemp.setText(
                String.format("Day temp: %.1f ℃", dayTemperature)
        );
        binding.dayTempSeekBar.setProgress((int) dayTemperature - MIN_TEMPERATURE);
    }

    @Override
    public void updateSettingNightTemp(double nightTemperature) {
        binding.settingNightTemp.setVisibility(View.VISIBLE);
        binding.nightTempSeekBar.setVisibility(View.VISIBLE);
        binding.settingNightTemp.setText(
                String.format("Night temp: %.1f ℃", nightTemperature)
        );
        binding.nightTempSeekBar.setProgress((int) nightTemperature - MIN_TEMPERATURE);
    }

    @Override
    public void setHostOnline(boolean online) {
        final String offline = "offline";
        SpannableStringBuilder ssb = new SpannableStringBuilder(
                String.format(
                        "Things host is %s",
                        online ? "online" : offline
                )
        );
        if (!online) {
            int index = ssb.toString().indexOf(offline);
            ssb.setSpan(
                    new ForegroundColorSpan(Color.RED),
                    index,
                    index + offline.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
        }
        binding.hostOnline.setText(ssb);
    }

    enum Field {
        temperature, humidity, boilerIsRun, pressure, maintainedTemperature
    }
}
