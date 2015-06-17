package com.sagar.screenshift;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import static com.sagar.screenshift.PreferencesHelper.KEY_MASTER_SWITCH_ON;
import static com.sagar.screenshift.ScreenShiftService.ACTION_START;
import static com.sagar.screenshift.ScreenShiftService.ACTION_STOP;

/**
 * Created by aravind on 17/6/15.
 */
public class CallStateReceiver extends BroadcastReceiver{
    public static final String ALARM_ALERT_ACTION = "com.android.deskclock.ALARM_ALERT";
    public static final String ALARM_SNOOZE_ACTION = "com.android.deskclock.ALARM_SNOOZE";
    public static final String ALARM_DISMISS_ACTION = "com.android.deskclock.ALARM_DISMISS";
    public static final String ALARM_DONE_ACTION = "com.android.deskclock.ALARM_DONE";

    private static boolean isInCall = false, isAlarmRinging = false, disabledBecauseOfCall = false, enableBecauseOfCall = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("CallStateReceiver", "onReceive");
        if(intent.getAction().equals(TelephonyManager.ACTION_PHONE_STATE_CHANGED)){
            String callState = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            if(callState.equals(TelephonyManager.EXTRA_STATE_RINGING) || callState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                isInCall = true;
            } else if(callState.equals(TelephonyManager.EXTRA_STATE_IDLE)){
                isInCall = false;
            }
        } else if(intent.getAction().equals(ALARM_ALERT_ACTION)){
            isAlarmRinging = true;
        } else if(intent.getAction().equals(ALARM_SNOOZE_ACTION) || intent.getAction().equals(ALARM_DISMISS_ACTION)
                || intent.getAction().equals(ALARM_DONE_ACTION)) {
            isAlarmRinging = false;
        }

        if(isInCall || isAlarmRinging) {
            Log.d("CallStateReceiver", "isInCall");
            String preferenceKey = context.getResources().getString(R.string.key_display_mode_in_call);
            String inCallPreference = PreferencesHelper.getStringPreference(context, preferenceKey, "0");
            Log.d("CallStateReceiver", "inCallPreference " + inCallPreference);
            boolean masterSwitchOn = PreferencesHelper.getBoolPreference(context, KEY_MASTER_SWITCH_ON);
            if("1".equals(inCallPreference) && !masterSwitchOn) {
                enableBecauseOfCall = true;
                context.startService(new Intent(context, ScreenShiftService.class).setAction(ACTION_START));
            } else if("-1".equals(inCallPreference) && masterSwitchOn) {
                disabledBecauseOfCall = true;
                context.startService(new Intent(context, ScreenShiftService.class).setAction(ACTION_STOP));
            }
        } else {
            if(enableBecauseOfCall) {
                enableBecauseOfCall = false;
                context.startService(new Intent(context, ScreenShiftService.class).setAction(ACTION_STOP));
            }
            if(disabledBecauseOfCall) {
                disabledBecauseOfCall = false;
                context.startService(new Intent(context, ScreenShiftService.class).setAction(ACTION_START));
            }
        }
    }
}
