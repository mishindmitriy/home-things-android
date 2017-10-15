package com.mishindmitriy.homethings.client;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Dmitry on 07.10.2017.
 */

public class PreferencesHelper {
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
}
