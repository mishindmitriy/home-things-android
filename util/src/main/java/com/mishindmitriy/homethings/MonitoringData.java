package com.mishindmitriy.homethings;

/**
 * Created by Dmitry on 30.09.17.
 */

public class MonitoringData {
    public double temperature;
    public double maintainedTemperature;
    public double humidity;
    public boolean boilerIsRun;
    public long timestamp;
    public int ppm;
    public double pressure;

    @Override
    public String toString() {
        return "temp: " + temperature +
                "; humidity: " + humidity +
                "; maintainedTemperature: " + maintainedTemperature +
                "; boilerIsRun: " + boilerIsRun +
                "; timestamp: " + timestamp +
                "; ppm: " + ppm +
                "; pressure: " + pressure;
    }
}
