package com.mishindmitriy.homethings.client;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.AddToEndSingleStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;
import com.mishindmitriy.homethings.MonitoringData;

import java.util.List;

/**
 * Created by Dmitry on 30.09.17.
 */
@StateStrategyType(AddToEndSingleStrategy.class)
public interface HeatingControlView extends MvpView {
    void updateMonitoringData(List<MonitoringData> monitoringData);

    void setHostOnline(boolean online);

    void updateSettingDayTemp(double dayTemperature);

    void updateSettingNightTemp(double nightTemperature);

    void showLastSensorsData(MonitoringData data);
}
