package com.sagar.screenshift;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class ScreenShiftService extends Service {
    public static final String ACTION_START = "action_start";
    public static final String ACTION_STOP = "action_stop";
    public static final String ACTION_TOGGLE = "action_toggle";

    private boolean isEnabled = false;

    @Override
    public void onCreate() {
        super.onCreate();
        start();
        isEnabled = true;
        postNotification();
    }

    private void postNotification() {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        Intent intent = new Intent(this, ScreenShiftService.class);
        intent.setAction(ACTION_TOGGLE);
        PendingIntent pi = PendingIntent.getService(this, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = notificationBuilder.setContentTitle("Screen shift").
                setSmallIcon(R.mipmap.ic_launcher).setContentText("Toggle overlay on/off").
                setContentIntent(pi).setPriority(Notification.PRIORITY_LOW).build();
        startForeground(1, notification);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            String action = intent.getAction();
            if(ACTION_START.equals(action)) {
                start();
            } else if(ACTION_STOP.equals(action)) {
                stop();
            } else if(ACTION_TOGGLE.equals(action)) {
                toggle();
            }
        }
        return START_STICKY;
    }

    private void toggle() {
        if(isEnabled) {
            stop();
        } else {
            start();
        }
    }

    private void start(){
        List<String> results = Shell.SU.run("wm overscan 0,0,0,450");
        for (String result : results){
            Log.i("ScreenShiftService", result);
        }
        isEnabled = true;
    }

    private void stop(){
        List<String> results = Shell.SU.run("wm overscan reset");
        for (String result : results){
            Log.i("ScreenShiftService", result);
        }
        isEnabled = false;
    }
}
