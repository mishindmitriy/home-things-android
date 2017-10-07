package com.mishindmitriy.homethings.client;

import org.joda.time.DateTime;

/**
 * Created by Dmitry on 30.09.17.
 */

public class MonitoringData {
    private double value;
    private long timestamp;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public DateTime getJodaTime() {
        return new DateTime(timestamp);
    }
}
