package com.sagar.screenshift;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Date;

import static com.sagar.screenshift.PreferencesHelper.KEY_LAST_BOOT_TIME;
import static com.sagar.screenshift.PreferencesHelper.KEY_MASTER_SWITCH_ON;
import static com.sagar.screenshift.PreferencesHelper.KEY_SET_ON_BOOT;
import static com.sagar.screenshift.ScreenShiftService.ACTION_START;
import static com.sagar.screenshift.ScreenShiftService.ACTION_STOP;
import static com.sagar.screenshift.ScreenShiftService.EXTRA_POST_NOTIFICATION;

/**
 * Created by aravind on 16/6/15.
 */
public class BootCompleteReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("BootCompleteReceiver", "Boot complete. :)");
        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
            long lastBootTime = PreferencesHelper.getLongPreference(context, KEY_LAST_BOOT_TIME, -1);
            long currentBootTime = new Date().getTime();
            if(lastBootTime != -1) {
                if(currentBootTime - lastBootTime <= 1000*60*10) {
                    context.startService(new Intent(context, ScreenShiftService.class)
                            .setAction(ACTION_STOP).putExtra(EXTRA_POST_NOTIFICATION, false));
                    PreferencesHelper.setPreference(context, KEY_SET_ON_BOOT, false);
                }
            }
            PreferencesHelper.setPreference(context, KEY_LAST_BOOT_TIME, currentBootTime);
            if(PreferencesHelper.getBoolPreference(context, KEY_SET_ON_BOOT, true) && PreferencesHelper.getBoolPreference(context, KEY_MASTER_SWITCH_ON)) {
                context.startService(new Intent(context, ScreenShiftService.class)
                        .setAction(ACTION_START));
            } else {
                context.startService(new Intent(context, ScreenShiftService.class)
                        .setAction(ACTION_STOP));
            }
        }
    }
}
