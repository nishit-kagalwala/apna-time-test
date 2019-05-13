package com.example.apnatimetest.activities;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;

public class StartApplication extends Application {

    @Override
    public void onCreate() {

        super.onCreate();
        //FirebaseApp.initializeApp(getApplicationContext());


    }
}
