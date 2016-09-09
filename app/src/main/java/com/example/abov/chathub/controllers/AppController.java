package com.example.abov.chathub.controllers;

import com.firebase.client.Firebase;


public class AppController extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
    }
}
