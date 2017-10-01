package com.mishindmitriy.homethings.client;

import org.joda.time.DateTime;

/**
 * Created by Dmitry on 30.09.17.
 */

public class HeatingData {
    private double temp;
    private double humidity;
    private boolean hostIsOnline;
    private DateTime lastUpdate;

    public DateTime getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(DateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public double getTemp() {
        return temp;
    }

    public void setTemp(double temp) {
        this.temp = temp;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public void setHostIsOnline(Boolean hostIsOnline) {
        this.hostIsOnline = hostIsOnline;
    }

    public boolean hostIsOnline() {
        return hostIsOnline;
    }
}
