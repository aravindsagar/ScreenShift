package com.sagar.screenshift;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import eu.chainfire.libsuperuser.Shell;

import static com.sagar.screenshift.PreferencesHelper.KEY_DENSITY_ENABLED;
import static com.sagar.screenshift.PreferencesHelper.KEY_DENSITY_VALUE;
import static com.sagar.screenshift.PreferencesHelper.KEY_DISPLAY_HEIGHT;
import static com.sagar.screenshift.PreferencesHelper.KEY_DISPLAY_WIDTH;
import static com.sagar.screenshift.PreferencesHelper.KEY_OVERSCAN_BOTTOM;
import static com.sagar.screenshift.PreferencesHelper.KEY_OVERSCAN_ENABLED;
import static com.sagar.screenshift.PreferencesHelper.KEY_OVERSCAN_LEFT;
import static com.sagar.screenshift.PreferencesHelper.KEY_OVERSCAN_RIGHT;
import static com.sagar.screenshift.PreferencesHelper.KEY_OVERSCAN_TOP;
import static com.sagar.screenshift.PreferencesHelper.KEY_RESOLUTION_ENABLED;
import static com.sagar.screenshift.PreferencesHelper.KEY_RESOLUTION_HEIGHT;
import static com.sagar.screenshift.PreferencesHelper.KEY_RESOLUTION_WIDTH;

public class ScreenShiftService extends Service {
    public static final String ACTION_START = "action_start";
    public static final String ACTION_STOP = "action_stop";
    public static final String ACTION_TOGGLE = "action_toggle";
    public static final String ACTION_SAVE_HEIGHT_WIDTH = "action_save_height_width";
    public static final String EXTRA_SEND_BROADCAST = "send_broadcast";
    public static final String EXTRA_POST_NOTIFICATION = "post_notification";

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
                setContentIntent(pi).build();
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
            boolean postNotification = intent.getBooleanExtra(EXTRA_POST_NOTIFICATION, true);
            Log.d("send_broadcast", sendBroadcast+"");
            Log.d("post_notification", postNotification+"");
            if(ACTION_START.equals(action)) {
                start(sendBroadcast, postNotification);
            } else if(ACTION_STOP.equals(action)) {
                stop(sendBroadcast, postNotification);
            } else if(ACTION_TOGGLE.equals(action)) {
                toggle(sendBroadcast, postNotification);
            } else if(ACTION_SAVE_HEIGHT_WIDTH.equals(action)){
                saveWidthHeight();
            }
        }
        return START_STICKY;
    }

    private void toggle(boolean sendBroadcast, boolean postNotification) {
        if(PreferencesHelper.getBoolPreference(this, PreferencesHelper.KEY_MASTER_SWITCH_ON)) {
            stop(sendBroadcast, postNotification);
        } else {
            start(sendBroadcast, postNotification);
        }
    }

    private void start(boolean sendBroadcast, boolean postNotification){
        new AsyncTask<Boolean, Void, Boolean>() {
            private boolean mSendBroadcast, mPostNotification;
            @Override
            protected Boolean doInBackground(Boolean... booleans) {
                if(!Shell.SU.available()) return false;
                List<String> commands = new ArrayList<>();
                int height, width;
                height = PreferencesHelper.getIntPreference(ScreenShiftService.this, KEY_DISPLAY_HEIGHT, 1280);
                width = PreferencesHelper.getIntPreference(ScreenShiftService.this, KEY_DISPLAY_WIDTH, 768);
                if(PreferencesHelper.getBoolPreference(ScreenShiftService.this, KEY_RESOLUTION_ENABLED)) {
                    height = PreferencesHelper.getIntPreference(ScreenShiftService.this, KEY_RESOLUTION_HEIGHT, -1);
                    width = PreferencesHelper.getIntPreference(ScreenShiftService.this, KEY_RESOLUTION_WIDTH, -1);
                    if(height == -1) height = PreferencesHelper.getIntPreference(ScreenShiftService.this, KEY_DISPLAY_HEIGHT, 1280);
                    if(width == -1) width = PreferencesHelper.getIntPreference(ScreenShiftService.this, KEY_DISPLAY_WIDTH, 768);
                    commands.add("wm size " + width + "x" + height);
                }
                if(PreferencesHelper.getBoolPreference(ScreenShiftService.this, KEY_OVERSCAN_ENABLED)) {
                    int leftOverscan = (int) (PreferencesHelper.getIntPreference(ScreenShiftService.this, KEY_OVERSCAN_LEFT, 0) * width / 100f);
                    int rightOverscan = (int) (PreferencesHelper.getIntPreference(ScreenShiftService.this, KEY_OVERSCAN_RIGHT, 0) * width / 100f);
                    int topOverscan = (int) (PreferencesHelper.getIntPreference(ScreenShiftService.this, KEY_OVERSCAN_TOP, 0) * height / 100f);
                    int bottomOverscan = (int) (PreferencesHelper.getIntPreference(ScreenShiftService.this, KEY_OVERSCAN_BOTTOM, 0) * height / 100f);
                    commands.add("wm overscan " + leftOverscan + "," + topOverscan + "," + rightOverscan + "," + bottomOverscan);
                }
                if(PreferencesHelper.getBoolPreference(ScreenShiftService.this, KEY_DENSITY_ENABLED)) {
                    int density = PreferencesHelper.getIntPreference(ScreenShiftService.this, KEY_DENSITY_VALUE, -1);
                    if(density != -1) {
                        commands.add("wm density " + density);
                    }
                }
                PreferencesHelper.setPreference(ScreenShiftService.this,
                        PreferencesHelper.KEY_MASTER_SWITCH_ON, true);
                List<String> results = Shell.SU.run(commands);
                for (String result : results){
                    Log.i("ScreenShiftService", result);
                }
                mSendBroadcast = booleans[0];
                mPostNotification = booleans[1];
                return true;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                if(mPostNotification) {
                    postNotification("Display in custom mode");
                }
                if(!result){
                    Toast.makeText(ScreenShiftService.this,
                            "Couldn't acquire root permissions", Toast.LENGTH_SHORT).show();
                    stop(true, true);
                }
                if(mSendBroadcast){
                    LocalBroadcastManager.getInstance(ScreenShiftService.this)
                            .sendBroadcast(new Intent(ACTION_START));
                }
            }
        }.execute(sendBroadcast, postNotification);
    }

    private void stop(boolean sendBroadcast, boolean postNotification){
        new AsyncTask<Boolean, Void, Void>() {
            private boolean mSendBroadcast, mPostNotification;
            @Override
            protected Void doInBackground(Boolean... booleans) {
                PreferencesHelper.setPreference(ScreenShiftService.this,
                        PreferencesHelper.KEY_MASTER_SWITCH_ON, false);
                List<String> results = Shell.SU.run(getResetCommands());
                for (String result : results){
                    Log.i("ScreenShiftService", result);
                }
                mSendBroadcast = booleans[0];
                mPostNotification = booleans[1];
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                if(mPostNotification) {
                    postNotification("Display in default mode");
                }
                if(mSendBroadcast){
                    LocalBroadcastManager.getInstance(ScreenShiftService.this)
                            .sendBroadcast(new Intent(ACTION_STOP));
                }
            }
        }.execute(sendBroadcast, postNotification);
    }

    private void saveWidthHeight(){
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                if(PreferencesHelper.getIntPreference(ScreenShiftService.this, KEY_DISPLAY_HEIGHT, -1) == -1
                        || PreferencesHelper.getIntPreference(ScreenShiftService.this, KEY_DISPLAY_WIDTH, -1) == -1) {
                    Shell.SU.run(getResetCommands());
                    DisplaySize displaySize = DisplaySize.getDeviceDisplaySize(ScreenShiftService.this);
                    PreferencesHelper.setPreference(ScreenShiftService.this, KEY_DISPLAY_HEIGHT, displaySize.height);
                    PreferencesHelper.setPreference(ScreenShiftService.this, KEY_DISPLAY_WIDTH, displaySize.width);
                }
                return null;
            }
        }.execute();
    }

    List<String> getResetCommands() {
        List<String> commands = new ArrayList<>();
        commands.add("wm size reset");
        commands.add("wm density reset");
        commands.add("wm overscan reset");
        return commands;
    }

    public static class DisplaySize {
        public int height, width;

        public DisplaySize(int h, int w){
            height = h;
            width = w;
        }
        public static DisplaySize getDeviceDisplaySize(Context context) {
            WindowManager w = (WindowManager) context.getSystemService(WINDOW_SERVICE);
            Display d = w.getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            d.getMetrics(metrics);
// since SDK_INT = 1;
            int widthPixels = metrics.widthPixels;
            int heightPixels = metrics.heightPixels;
// includes window decorations (statusbar bar/menu bar)
            if (Build.VERSION.SDK_INT >= 14 && Build.VERSION.SDK_INT < 17)
                try {
                    widthPixels = (Integer) Display.class.getMethod("getRawWidth").invoke(d);
                    heightPixels = (Integer) Display.class.getMethod("getRawHeight").invoke(d);
                } catch (Exception ignored) {
                }
// includes window decorations (statusbar bar/menu bar)
            if (Build.VERSION.SDK_INT >= 17)
                try {
                    Point realSize = new Point();
                    Display.class.getMethod("getRealSize", Point.class).invoke(d, realSize);
                    widthPixels = realSize.x;
                    heightPixels = realSize.y;
                } catch (Exception ignored) {
                }
            return new DisplaySize(heightPixels, widthPixels);
        }
    }
}
