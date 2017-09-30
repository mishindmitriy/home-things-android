package com.mishindmitriy.homethings.client;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Dmitry on 30.09.17.
 */

public class FirebaseHelper {
    public static DatabaseReference getHeatingMonitoringReference() {
        return FirebaseDatabase.getInstance().getReference("heating/monitor");
    }
}
