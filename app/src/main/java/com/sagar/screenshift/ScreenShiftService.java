package com.sagar.screenshift;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.view.WindowManager;

public class ScreenShiftService extends Service {
    public static final String ACTION_START_OVERLAY = "action_start_overlay";
    public static final String ACTION_STOP_OVERLAY = "action_stop_overlay";

    private ScreenShiftOverlay overlay;

    @Override
    public void onCreate() {
        super.onCreate();
        WindowManager windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        overlay = new ScreenShiftOverlay(this, windowManager);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            String action = intent.getAction();
            if(ACTION_START_OVERLAY.equals(action)) {
                overlay.execute();
            } else if(ACTION_STOP_OVERLAY.equals(action)) {
                overlay.remove();
            }
        }
        return START_STICKY;
    }
}
