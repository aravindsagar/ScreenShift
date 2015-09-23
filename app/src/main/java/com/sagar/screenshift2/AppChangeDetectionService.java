package com.sagar.screenshift2;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.sagar.screenshift2.data_objects.App;
import com.sagar.screenshift2.data_objects.Profile;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import static com.sagar.screenshift2.PreferencesHelper.KEY_MASTER_SWITCH_ON;

public class AppChangeDetectionService extends Service {
    private static final String PACKAGE_NAME = AppChangeDetectionService.class.getPackage().getName();
    private static final String LOG_TAG = AppChangeDetectionService.class.getSimpleName();

    public static final String ACTION_CLEAR_PREVIOUS_PACKAGE = PACKAGE_NAME + ".CLEAR_PREVIOUS_PACKAGE";
    public static final String ACTION_START_SERVICE = PACKAGE_NAME + ".START_SERVICE";
    public static final String ACTION_STOP_SERVICE = PACKAGE_NAME + ".STOP_SERVICE";

    private String previousPackageName = "";

    private OnForegroundAppChangedListener mOnForegroundAppChangedListener;

    private AppLockThread mAppLockThread;

    ActivityManager mActivityManager;
    UsageStatsManager mUsageStatsManager;

    public AppChangeDetectionService() {
    }

    public static Intent getServiceIntent(Context context, final String action){
        Intent intent = new Intent(context, AppChangeDetectionService.class);
        if(action != null && !action.isEmpty()) {
            intent.setAction(action);
        }
        return intent;
    }

    @Override
    public void onCreate() {
        startThread();
        mOnForegroundAppChangedListener = new OnForegroundAppChangedListenerImpl(this);
        mActivityManager = (ActivityManager) getBaseContext().getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            mUsageStatsManager = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
        }
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null) {
            String action = intent.getAction();
            if (action != null && !action.isEmpty()) {
                if (action.equals(ACTION_CLEAR_PREVIOUS_PACKAGE)) {
                    previousPackageName = "";
                } else if (action.equals(ACTION_START_SERVICE)) {
                    Log.d(LOG_TAG, "Starting app detection");
                    startThread();
                } else if (action.equals(ACTION_STOP_SERVICE)) {
                    stopThread();
                }
            }
        }
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    private void stopThread(){
        if(mAppLockThread != null) {
            mAppLockThread.interrupt();
            mAppLockThread = null;
        }
    }

    private void startThread(){
        stopThread();
        mAppLockThread = new AppLockThread();
        mAppLockThread.start();
    }

    private class AppLockThread extends Thread {

        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }

                String foregroundTaskPackageName = "";
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                    long time = System.currentTimeMillis();
                    List<UsageStats> appList = mUsageStatsManager
                            .queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  time - 1000*100, time);
                    if (appList != null && appList.size() > 0) {
                        SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
                        for (UsageStats usageStats : appList) {
                            mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                        }
                        if (!mySortedMap.isEmpty()) {
                            foregroundTaskPackageName = mySortedMap
                                    .get(mySortedMap.lastKey()).getPackageName();
                        }
                    }
                } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    List<ActivityManager.RunningAppProcessInfo> tasks =
                            mActivityManager.getRunningAppProcesses();
                    foregroundTaskPackageName = tasks.get(0).processName;
                } else {
                    RunningTaskInfo foregroundTaskInfo = mActivityManager.getRunningTasks(1).get(0);
                    foregroundTaskPackageName = foregroundTaskInfo.topActivity.getPackageName();
                }

                if(foregroundTaskPackageName.equals("") ||
                        foregroundTaskPackageName.equals(previousPackageName)){
                    continue;
                }

                previousPackageName = foregroundTaskPackageName;

                if(mOnForegroundAppChangedListener != null){
                    mOnForegroundAppChangedListener.onForegroundAppChanged(
                            foregroundTaskPackageName);
                }
            }
        }
    }

    public interface OnForegroundAppChangedListener {
        void onForegroundAppChanged(String packageName);
    }

    public class OnForegroundAppChangedListenerImpl implements OnForegroundAppChangedListener {

        private Context mContext;
        private Profile mDefaultProfile;

        public OnForegroundAppChangedListenerImpl(Context context) {
            mContext = context;
        }

        @Override
        public void onForegroundAppChanged(String packageName) {
            Log.d("AppChangeDetection", "Foreground app changed: " + packageName);
            if (!PreferencesHelper.getBoolPreference(mContext, KEY_MASTER_SWITCH_ON)) {
                return;
            }

            Profile appProfile = App.getProfileForApp(mContext, packageName);
            if (appProfile != null) {
                // This app has a specific profile set.
                if(mDefaultProfile == null) {
                    // Saving current values if previous app was using 'default' values. If not,
                    // the previous app would have backed this up.
                    mDefaultProfile = Profile.fromSavedDefaultValues(mContext);
                }
                appProfile.saveAsCurrent(mContext);
            } else if (mDefaultProfile != null) {
                // No profile set for this app. Loading 'default' values if required.
                mDefaultProfile.saveAsCurrent(mContext);
                mDefaultProfile = null;
            }
        }
    }
}
