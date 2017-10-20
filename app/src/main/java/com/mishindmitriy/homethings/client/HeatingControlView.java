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
    void updateMonitoringData(List<MonitoringData> monitoringData);

    void setHostOnline(boolean online);

    void updateSettingDayTemp(double dayTemperature);

    void updateSettingNightTemp(double nightTemperature);

    void updateMaintainedTemperature(double maintainedTemperature);

    void updateBoilerIsRun(boolean boilerIsRun);
}
