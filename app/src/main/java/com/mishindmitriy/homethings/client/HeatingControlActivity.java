package com.mishindmitriy.homethings.client;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.arellomobile.mvp.MvpAppCompatActivity;
import com.arellomobile.mvp.presenter.InjectPresenter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.mishindmitriy.homethings.client.databinding.ActivityHeatingControlBinding;

import org.joda.time.DateTime;

import java.util.ArrayList;
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
    public void updateMaintainedTemperature(double maintainedTemperature) {
        binding.maintainedTemperature.setVisibility(View.VISIBLE);
        binding.maintainedTemperature.setText(
                String.format("Maintained temp: %.1f ℃", maintainedTemperature)
        );
    }

    @Override
    public void updateBoilerIsRun(boolean boilerIsRun) {
        binding.boilerIsRun.setVisibility(View.VISIBLE);
        binding.boilerIsRun.setText(
                String.format("Boiler is %s", boilerIsRun ? "run" : "not run")
        );
    }

    @Override
    public void updateTemperatureData(final List<MonitoringData> data) {
        updateData(
                binding.temperatureChart,
                binding.temperatureLastUpdate,
                data,
                Color.RED,
                "Temperature",
                "℃"
        );
    }

    private List<Entry> createEntryList(List<MonitoringData> monitoringData) {
        final List<Entry> entries = new ArrayList<>();
        for (int i = 1; i < monitoringData.size(); i++) {
            entries.add(new Entry(i, (float) monitoringData.get(i).getValue()));
        }
        return entries;
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
    public void updateHumidityData(final List<MonitoringData> data) {
        updateData(
                binding.humidityChart,
                binding.humidityLastUpdate,
                data,
                Color.BLUE,
                "Humidity",
                "%"
        );
    }

    private void updateData(LineChart chart,
                            TextView lastUpdate,
                            final List<MonitoringData> data,
                            @ColorInt int color,
                            String label,
                            final String yAxisSymbol) {
        binding.progressBar.setVisibility(View.GONE);
        LineDataSet dataSet = new LineDataSet(createEntryList(data), label);
        dataSet.setColor(color);
        dataSet.setValueTextColor(Color.BLACK);
        chart.getXAxis().setValueFormatter(new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                final DateTime dateTime = data.get((int) value).getJodaTime();
                return DateTime.now().toLocalDate().isEqual(dateTime.toLocalDate())
                        ? dateTime.toString("HH:mm:ss")
                        : dateTime.toString("dd MMMM HH:mm:ss");
            }
        });
        IAxisValueFormatter yFormatter = new IAxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return String.format("%.1f %s", value, yAxisSymbol);
            }
        };
        chart.getAxisLeft().setValueFormatter(yFormatter);
        chart.getAxisRight().setValueFormatter(yFormatter);
        chart.setData(new LineData(dataSet));
        chart.invalidate();
        chart.setVisibility(View.VISIBLE);
        lastUpdate.setVisibility(View.VISIBLE);
        lastUpdate.setText(
                String.format(
                        "last update %s", data.get(data.size() - 1).getJodaTime().toString("dd MMMM HH:mm:ss")
                )
        );
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
}
