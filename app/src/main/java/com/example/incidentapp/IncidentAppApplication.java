package com.example.incidentapp;

import android.app.Application;

import com.example.incidentapp.local.AppDatabase;
import com.example.incidentapp.notifications.NotificationCenter;
import com.example.incidentapp.session.SessionManager;
import com.example.incidentapp.worker.SyncScheduler;

public class IncidentAppApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SessionManager.init(getApplicationContext());
        AppDatabase.init(getApplicationContext());
        NotificationCenter.crearCanal(getApplicationContext());
        SyncScheduler.programar(getApplicationContext());
    }
}
