package com.sagar.screenshift;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.List;

import eu.chainfire.libsuperuser.Shell;

public class ScreenShiftService extends Service {
    public static final String ACTION_START = "action_start";
    public static final String ACTION_STOP = "action_stop";
    public static final String ACTION_TOGGLE = "action_toggle";
    public static final String EXTRA_SEND_BROADCAST = "send_broadcast";

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private void postNotification(String text) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this);
        Intent intent = new Intent(this, ScreenShiftService.class);
        intent.setAction(ACTION_TOGGLE);
        intent.putExtra(EXTRA_SEND_BROADCAST, true);
        PendingIntent pi = PendingIntent.getService(this, 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        Notification notification = notificationBuilder.setContentTitle("Screen shift").
                setSmallIcon(R.mipmap.ic_launcher).setContentText(text).
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
            boolean sendBroadcast = intent.getBooleanExtra(EXTRA_SEND_BROADCAST, false);
            if(ACTION_START.equals(action)) {
                start(sendBroadcast);
            } else if(ACTION_STOP.equals(action)) {
                stop(sendBroadcast);
            } else if(ACTION_TOGGLE.equals(action)) {
                toggle(sendBroadcast);
            }
        }
        return START_STICKY;
    }

    private void toggle(boolean sendBroadcast) {
        if(SharedPreferencesHelper.getBoolPreference(this, SharedPreferencesHelper.KEY_MASTER_SWITCH_ON)) {
            stop(sendBroadcast);
        } else {
            start(sendBroadcast);
        }
    }

    private void start(boolean sendBroadcast){
        new AsyncTask<Boolean, Void, Boolean>() {
            private boolean mSendBroadcast;
            @Override
            protected Boolean doInBackground(Boolean... booleans) {
                if(!Shell.SU.available()) return false;
                List<String> results = Shell.SU.run("wm overscan 0,0,0,450");
                for (String result : results){
                    Log.i("ScreenShiftService", result);
                }
                SharedPreferencesHelper.setPreference(ScreenShiftService.this,
                        SharedPreferencesHelper.KEY_MASTER_SWITCH_ON, true);
                mSendBroadcast = booleans[0];
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                postNotification("Display in custom mode");
                if(!result){
                    Toast.makeText(ScreenShiftService.this,
                            "Couldn't acquire root permissions", Toast.LENGTH_SHORT).show();
                    stop(true);
                }
                if(mSendBroadcast){
                    LocalBroadcastManager.getInstance(ScreenShiftService.this)
                            .sendBroadcast(new Intent(ACTION_START));
                }
            }
        }.execute(sendBroadcast);
    }

    private void stop(boolean sendBroadcast){
        new AsyncTask<Boolean, Void, Void>() {
            private boolean mSendBroadcast;
            @Override
            protected Void doInBackground(Boolean... booleans) {
                List<String> results = Shell.SU.run("wm overscan reset");
                for (String result : results){
                    Log.i("ScreenShiftService", result);
                }
                SharedPreferencesHelper.setPreference(ScreenShiftService.this,
                        SharedPreferencesHelper.KEY_MASTER_SWITCH_ON, false);
                mSendBroadcast = booleans[0];
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                postNotification("Display in default mode");
                if(mSendBroadcast){
                    LocalBroadcastManager.getInstance(ScreenShiftService.this)
                            .sendBroadcast(new Intent(ACTION_STOP));
                }
            }
        }.execute(sendBroadcast);
    }
}
