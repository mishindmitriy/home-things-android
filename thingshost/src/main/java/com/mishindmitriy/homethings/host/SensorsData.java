package com.mishindmitriy.homethings.host;

/**
 * Created by mishindmitriy on 11.10.2017.
 */

public class SensorsData {
    private double temperature = 0;
    private double humidity = 0;
    private double pressure = 0;
    private int ppm = 0;

    public double getPressure() {
        return pressure;
    }

    public void setPressure(double pressure) {
        this.pressure = pressure;
    }

    public int getPpm() {
        return ppm;
    }

    public void setPpm(int ppm) {
        this.ppm = ppm;
    }

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
