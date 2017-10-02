package com.mishindmitriy.homethings.client;

import org.joda.time.DateTime;

/**
 * Created by Dmitry on 30.09.17.
 */

public class HeatingData {
    private double temp;
    private double humidity;
    private long timestamp;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
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

    public DateTime getJodaTime() {
        return new DateTime(timestamp);
    }
}
