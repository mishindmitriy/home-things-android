package com.mishindmitriy.homethings.client;

import org.joda.time.DateTime;

/**
 * Created by Dmitry on 30.09.17.
 */

public class MonitoringData {
    public double temperature;
    public double maintainedTemperature;
    public double humidity;
    public boolean boilerIsRun;
    public long timestamp;

    public DateTime getJodaTime() {
        return new DateTime(timestamp);
    }
}
