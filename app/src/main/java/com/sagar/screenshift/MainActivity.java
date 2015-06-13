package com.sagar.screenshift;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;


public class MainActivity extends AppCompatActivity {

    SwitchCompat masterSwitch;
    CardView resolutionCard, overscanCard, densityCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpToolbar();
        setUpCards();
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Log.w("Screen Shift", "Overscan and density not available in this api level");
            if(overscanCard != null){
                overscanCard.setVisibility(View.GONE);
            }
            if(densityCard != null){
                densityCard.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(ScreenShiftService.ACTION_START);
        filter.addAction(ScreenShiftService.ACTION_STOP);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(receiver, filter);
    }

    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_main);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null) {
            actionBar.setElevation(getResources().getDimension(R.dimen.toolbar_elevation));
        }
        toolbar.setTitle("Screen Shift");
        toolbar.setTitleTextColor(Color.WHITE);
        masterSwitch = new SwitchCompat(this);
        masterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    enableService();
                } else {
                    disableService();
                }
            }
        });
        Toolbar.LayoutParams params = new Toolbar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.END;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            params.setMarginEnd((int) getResources().getDimension(R.dimen.activity_vertical_margin));
        } else {
            params.setMargins(0,0,(int) getResources().getDimension(R.dimen.activity_vertical_margin),0);
        }
        toolbar.addView(masterSwitch, params);
        boolean masterSwitchOn = SharedPreferencesHelper.getBoolPreference(this, SharedPreferencesHelper.KEY_MASTER_SWITCH_ON);
        Log.d("masterSwitchOn", String.valueOf(masterSwitchOn));
        if(masterSwitch.isChecked() == masterSwitchOn){
            if(masterSwitchOn) enableService();
            else disableService();
        } else {
            masterSwitch.setChecked(masterSwitchOn);
        }
    }

    private void setUpCards(){

    }

    private void enableService(){
        SharedPreferencesHelper.setPreference(this, SharedPreferencesHelper.KEY_MASTER_SWITCH_ON, true);
        startService(new Intent(MainActivity.this, ScreenShiftService.class).setAction(ScreenShiftService.ACTION_START));
    }

    private void disableService(){
        SharedPreferencesHelper.setPreference(this, SharedPreferencesHelper.KEY_MASTER_SWITCH_ON, false);
        startService(new Intent(MainActivity.this, ScreenShiftService.class).setAction(ScreenShiftService.ACTION_STOP));
    }

    private void enableResolutionCard(){

    }

    private void enableOverscanCard(){

    }

    private void enableDensity(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {

        }
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, ScreenShiftService.class);
            intent.setAction(ScreenShiftService.ACTION_STOP);
            startService(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(receiver);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("BroadcastReceiver", "Broadcast received");
            if(ScreenShiftService.ACTION_START.equals(intent.getAction())) {
                masterSwitch.setChecked(true);
            } else if(ScreenShiftService.ACTION_STOP.equals(intent.getAction())) {
                masterSwitch.setChecked(false);
            }
        }
    };
}
