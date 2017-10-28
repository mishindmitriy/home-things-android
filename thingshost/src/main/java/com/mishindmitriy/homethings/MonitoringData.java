package com.mishindmitriy.homethings;

/**
 * Created by mishindmitriy on 11.10.2017.
 */

public class MonitoringData {
    private double temperature = 0;
    private double humidity = 0;

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }
}
