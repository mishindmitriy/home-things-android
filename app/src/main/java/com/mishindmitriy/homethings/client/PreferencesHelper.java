package com.mishindmitriy.homethings.client;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Dmitry on 07.10.2017.
 */

public class PreferencesHelper {
    public static final String KEY_SETTING_DAY_TEMP = "setting_day_temperature";
    private static PreferencesHelper instance;
    private final SharedPreferences preferences;

    private PreferencesHelper(Context context) {
        preferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    public static void init(Context context) {
        instance = new PreferencesHelper(context);
    }

    public static PreferencesHelper get() {
        return instance;
    }

    public void setDayTemperature(int temperature) {
        preferences.edit()
                .putInt(KEY_SETTING_DAY_TEMP, temperature)
                .apply();
    }

    public int getDaySettingTemperature() {
        return preferences.getInt(KEY_SETTING_DAY_TEMP, 0);
    }
}
