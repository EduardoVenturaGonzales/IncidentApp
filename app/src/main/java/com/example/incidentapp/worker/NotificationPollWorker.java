package com.example.incidentapp.worker;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.*;
import com.example.incidentapp.local.AppDatabase;
import com.example.incidentapp.notifications.NotificationCenter;
import com.example.incidentapp.session.SessionManager;
public class NotificationPollWorker extends Worker{
    public NotificationPollWorker(@NonNull Context c,@NonNull WorkerParameters p){
        super(c,p);
    }
    @NonNull public Result doWork(){
        SessionManager.init(getApplicationContext());
        AppDatabase.init(getApplicationContext());
        NotificationCenter.sincronizar(getApplicationContext());
        return Result.success();
    }
}