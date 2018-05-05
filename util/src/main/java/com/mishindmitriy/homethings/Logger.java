package com.mishindmitriy.homethings;

import android.util.Log;

/**
 * Created by Dmitry on 14.10.2017.
 */

public class Logger {
    public static void l(String s) {
        Log.d("testtt", s);
        //FirebaseHelper.getLogReference().push().setValue(s);
    }
}
