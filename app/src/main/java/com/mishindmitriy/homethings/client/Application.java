package com.mishindmitriy.homethings.client;

import com.google.firebase.FirebaseApp;

import net.danlew.android.joda.JodaTimeAndroid;

/**
 * Created by Dmitry on 30.09.17.
 */

public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
        FirebaseApp.initializeApp(this);
    }
}
