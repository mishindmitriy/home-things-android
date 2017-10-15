package com.mishindmitriy.homethings.client;

import com.arellomobile.mvp.MvpView;
import com.arellomobile.mvp.viewstate.strategy.SingleStateStrategy;
import com.arellomobile.mvp.viewstate.strategy.StateStrategyType;

import java.util.List;

/**
 * Created by Dmitry on 30.09.17.
 */
@StateStrategyType(SingleStateStrategy.class)
public interface HeatingControlView extends MvpView {
    void updateTemperatureData(List<MonitoringData> monitoringData);

    void setHostOnline(Boolean online);

    void updateHumidityData(List<MonitoringData> monitoringData);

    void updateSettingDayTemp(int dayTemperature);

    void updateSettingNightTemp(int nightTemperature);
}
