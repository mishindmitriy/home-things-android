package com.mishindmitriy.homethings.host;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.mishindmitriy.homethings.FirebaseHelper;

import net.danlew.android.joda.JodaTimeAndroid;

/**
 * Created by Dmitry on 22.09.17.
 */

public class HomeThingsApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        JodaTimeAndroid.init(this);
        FirebaseApp.initializeApp(this);
        FirebaseHelper.getHostOnlineRef().onDisconnect().setValue(false);
    }
}
