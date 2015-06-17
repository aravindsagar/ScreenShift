package com.sagar.screenshift;

import android.animation.Animator;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.sagar.screenshift.profileDb.ProfileDbContract.ProfileEntry;

import static com.sagar.screenshift.PreferencesHelper.KEY_DENSITY_ENABLED;
import static com.sagar.screenshift.PreferencesHelper.KEY_DENSITY_VALUE;
import static com.sagar.screenshift.PreferencesHelper.KEY_MASTER_SWITCH_ON;
import static com.sagar.screenshift.PreferencesHelper.KEY_OVERSCAN_BOTTOM;
import static com.sagar.screenshift.PreferencesHelper.KEY_OVERSCAN_ENABLED;
import static com.sagar.screenshift.PreferencesHelper.KEY_OVERSCAN_LEFT;
import static com.sagar.screenshift.PreferencesHelper.KEY_OVERSCAN_RIGHT;
import static com.sagar.screenshift.PreferencesHelper.KEY_OVERSCAN_TOP;
import static com.sagar.screenshift.PreferencesHelper.KEY_RESOLUTION_ENABLED;
import static com.sagar.screenshift.PreferencesHelper.KEY_RESOLUTION_HEIGHT;
import static com.sagar.screenshift.PreferencesHelper.KEY_RESOLUTION_WIDTH;


public class MainActivity extends AppCompatActivity implements DialogFragments.DialogListener {

    /*private static final float ENABLED_ALPHA = 1f;
    private static final float DISABLED_ALPHA = 0.7f;*/

    private static boolean onPostResumeRunDialog = false;

    SwitchCompat masterSwitch;
    View cardsLayout;
    CardView resolutionCard, overscanCard, densityCard;
    SwitchCompat resolutionSwitch, densitySwitch, overscanSwitch;
    boolean savedResolutionEnabled, savedOverscanEnabled, savedDensityEnabled;
    FloatingActionButton doneFab, fabBackground;
    EditText widthText, heightText, densityText;
    String savedWidth, savedHeight, savedDensity;
    LinearLayout resolutionInnerLayout, leftOverscanLayout, rightOverscanLayout, topOverscanLayout, bottomOverscanLayout;
    SeekBar leftSeekBar, rightSeekBar, topSeekBar, bottomSeekBar;
    TextView leftOverscanText, rightOverscanText, topOverscanText, bottomOverscanText;
    int savedLeftOverscan, savedRightOverscan, savedTopOverscan, savedBottomOverscan;
    boolean resolutionEnabledChanged, densityEnabledChanged, overscanEnabledChanged, widthChanged,
            heightChanged, leftOverscanChanged, rightOverscanChanged, topOverscanChanged,
            bottomOverscanChanged, densityChanged;
    boolean overrideWarning = false, showTimeout = true;
    Profile[] profiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //TODO add a one-time product tour, backward compatibility, better list of saved profiles
        super.onCreate(savedInstanceState);
        startService(new Intent(this, ScreenShiftService.class).setAction(ScreenShiftService.ACTION_SAVE_HEIGHT_WIDTH));
        setContentView(R.layout.activity_main);
        init(savedInstanceState);
        setUpToolbar();

    }

    private void init(Bundle savedInstanceState) {
        readSavedData();
        setupFAB(savedInstanceState);
        setUpCards(savedInstanceState);
        setUpProfileButtons();
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

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if(onPostResumeRunDialog) {
            Log.d("onPostResume", "Running");
            final Runnable confirmRunnable = new Runnable(){
                @Override
                public void run() {
                    try {
                        Log.d("confirmRunnable", "Running");
                        new DialogFragments.KeepSettingsDialog().show(getSupportFragmentManager(), "keepSettingsDialog");
                        onPostResumeRunDialog = false;
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    }
                }
            };
            new Handler().post(confirmRunnable);
        }
    }

    private void setUpToolbar() {
        showTimeout = false;
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
        boolean masterSwitchOn = PreferencesHelper.getBoolPreference(this, KEY_MASTER_SWITCH_ON);
        Log.d("masterSwitchOn", String.valueOf(masterSwitchOn));
        if(masterSwitch.isChecked() == masterSwitchOn){
            if(masterSwitchOn) enableService();
            else disableService();
        } else {
            masterSwitch.setChecked(masterSwitchOn);
        }
        showTimeout = true;
    }

    private void setUpCards(Bundle savedInstanceState){
        cardsLayout = findViewById(R.id.layout_cards);
        setupResolutionCard();
        setupOverscanCard();
        setupDensityCard();

        CardView optionsCard = (CardView) findViewById(R.id.card_view_options);
//        TextView moreSettingsButton = (TextView) findViewById(R.id.button_more_settings);
        optionsCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, SettingsActivity.class));
            }
        });
        if(savedInstanceState != null) {
            return;
        }
        populateResolutionCard();
        populateOverscanCard();
        populateDensityCard();
    }

    private void setUpProfileButtons(){
        Button loadProfileButton = (Button) findViewById(R.id.button_load_profile),
                saveProfileButton = (Button) findViewById(R.id.button_save_profile);
        loadProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Cursor cursor = getContentResolver().query(ProfileEntry.CONTENT_URI, null, null, null, null);
                if(cursor.moveToFirst()) {
                    profiles = new Profile[cursor.getCount()];
                    String[] itemStrings = new String[cursor.getCount()];
                    for(int i=0; !cursor.isAfterLast(); cursor.moveToNext(), i++){
                        Profile profile = new Profile();
                        profile.isResolutionEnabled = cursor.getInt(cursor.getColumnIndex(ProfileEntry.COLUMN_RESOLUTION_ENABLED)) == 1;
                        profile.isDensityEnabled = cursor.getInt(cursor.getColumnIndex(ProfileEntry.COLUMN_DENSITY_ENABLED)) == 1;
                        profile.isOverscanEnabled = cursor.getInt(cursor.getColumnIndex(ProfileEntry.COLUMN_OVERSCAN_ENABLED)) == 1;
                        profile.resolutionWidth = cursor.getInt(cursor.getColumnIndex(ProfileEntry.COLUMN_RESOLUTION_WIDTH));
                        profile.resolutionHeight = cursor.getInt(cursor.getColumnIndex(ProfileEntry.COLUMN_RESOLUTION_HEIGHT));
                        profile.densityValue = cursor.getInt(cursor.getColumnIndex(ProfileEntry.COLUMN_DENSITY_VALUE));
                        profile.overscanLeft = cursor.getInt(cursor.getColumnIndex(ProfileEntry.COLUMN_OVERSCAN_LEFT));
                        profile.overscanRight = cursor.getInt(cursor.getColumnIndex(ProfileEntry.COLUMN_OVERSCAN_RIGHT));
                        profile.overscanTop = cursor.getInt(cursor.getColumnIndex(ProfileEntry.COLUMN_OVERSCAN_TOP));
                        profile.overscanBottom = cursor.getInt(cursor.getColumnIndex(ProfileEntry.COLUMN_OVERSCAN_BOTTOM));
                        itemStrings[i] = cursor.getString(cursor.getColumnIndex(ProfileEntry.COLUMN_NAME)) + " " + profile.resolutionWidth + "x" + profile.resolutionHeight;
                        profiles[i] = profile;
                    }
                    Bundle bundle = new Bundle();
                    bundle.putStringArray(DialogFragments.KEY_LIST_ITEM_STRINGS, itemStrings);
                    DialogFragment dialogFragment = new DialogFragments.LoadProfileDialog();
                    dialogFragment.setArguments(bundle);
                    dialogFragment.show(getSupportFragmentManager(), "loadProfileDialog");
                }
                cursor.close();
            }
        });
        saveProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DialogFragments.SaveProfileDialog().show(getSupportFragmentManager(), "saveProfileDialog");
            }
        });
    }

    private void setupResolutionCard(){
        resolutionCard = (CardView) cardsLayout.findViewById(R.id.card_view_resolution);
        resolutionCard.setBackgroundColor(Color.WHITE);
        resolutionSwitch = (SwitchCompat) resolutionCard.findViewById(R.id.switch_resolution);
        resolutionInnerLayout = (LinearLayout) resolutionCard.findViewById(R.id.linear_layout_resolution);
        widthText = (EditText) resolutionInnerLayout.findViewById(R.id.edit_text_width);
        heightText = (EditText) resolutionInnerLayout.findViewById(R.id.edit_text_height);

        resolutionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                resolutionEnabledChanged = (b != savedResolutionEnabled);
                setFabVisibilityIfRequired();
                setResolutionCardInnerEnabled(b);
            }
        });
        widthText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                widthChanged = !editable.toString().equals(savedWidth);
                setFabVisibilityIfRequired();
            }
        });
        heightText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                heightChanged = !editable.toString().equals(savedHeight);
                setFabVisibilityIfRequired();
            }
        });
    }

    private void setupOverscanCard(){
        overscanCard = (CardView) cardsLayout.findViewById(R.id.card_view_overscan);
        overscanCard.setBackgroundColor(Color.WHITE);
        overscanSwitch = (SwitchCompat) overscanCard.findViewById(R.id.switch_overscan);
        leftOverscanLayout = (LinearLayout) overscanCard.findViewById(R.id.linear_layout_overscan_left);
        rightOverscanLayout = (LinearLayout) overscanCard.findViewById(R.id.linear_layout_overscan_right);
        topOverscanLayout = (LinearLayout) overscanCard.findViewById(R.id.linear_layout_overscan_top);
        bottomOverscanLayout = (LinearLayout) overscanCard.findViewById(R.id.linear_layout_overscan_bottom);
        leftSeekBar = (SeekBar) leftOverscanLayout.findViewById(R.id.seek_bar_left);
        rightSeekBar = (SeekBar) rightOverscanLayout.findViewById(R.id.seek_bar_right);
        topSeekBar = (SeekBar) topOverscanLayout.findViewById(R.id.seek_bar_top);
        bottomSeekBar = (SeekBar) bottomOverscanLayout.findViewById(R.id.seek_bar_bottom);
        leftOverscanText = (TextView) leftOverscanLayout.findViewById(R.id.text_overscan_left);
        rightOverscanText = (TextView) rightOverscanLayout.findViewById(R.id.text_overscan_right);
        topOverscanText = (TextView) topOverscanLayout.findViewById(R.id.text_overscan_top);
        bottomOverscanText = (TextView) bottomOverscanLayout.findViewById(R.id.text_overscan_bottom);

        overscanSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                overscanEnabledChanged = (b != savedOverscanEnabled);
                setFabVisibilityIfRequired();
                setOverscanCardInnerEnabled(b);
            }
        });
        leftSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int progress = seekBar.getProgress();
                leftOverscanText.setText(String.valueOf(progress));
                leftOverscanChanged = progress != savedLeftOverscan;
                setFabVisibilityIfRequired();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        rightSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int progress = seekBar.getProgress();
                rightOverscanText.setText(String.valueOf(progress));
                rightOverscanChanged = progress != savedRightOverscan;
                setFabVisibilityIfRequired();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        topSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int progress = seekBar.getProgress();
                topOverscanText.setText(String.valueOf(progress));
                topOverscanChanged = progress != savedTopOverscan;
                setFabVisibilityIfRequired();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        bottomSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                int progress = seekBar.getProgress();
                bottomOverscanText.setText(String.valueOf(progress));
                bottomOverscanChanged = progress != savedBottomOverscan;
                setFabVisibilityIfRequired();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void setupDensityCard(){
        densityCard = (CardView) cardsLayout.findViewById(R.id.card_view_density);
        densityCard.setBackgroundColor(Color.WHITE);
        densitySwitch = (SwitchCompat) densityCard.findViewById(R.id.switch_density);
        densityText = (EditText) densityCard.findViewById(R.id.edit_text_density);

        densitySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                densityEnabledChanged = (b != savedDensityEnabled);
                setFabVisibilityIfRequired();
                setDensityCardInnerEnabled(b);
            }
        });
        densityText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                densityChanged = !editable.toString().equals(savedDensity);
                setFabVisibilityIfRequired();
            }
        });
    }

    private void setResolutionCardInnerEnabled(boolean enabled) {
        setViewEnabled(enabled, resolutionInnerLayout, widthText, heightText);
    }

    private void setOverscanCardInnerEnabled(boolean enabled) {
        setViewEnabled(enabled, leftOverscanLayout, rightOverscanLayout, topOverscanLayout, bottomOverscanLayout);
        setViewEnabled(enabled, leftSeekBar, rightSeekBar, topSeekBar, bottomSeekBar);
        setViewEnabled(enabled, leftOverscanText, rightOverscanText, topOverscanText, bottomOverscanText);
    }

    private void setDensityCardInnerEnabled(boolean enabled) {
        setViewEnabled(enabled, densityText);
    }

    Object fabHideAnimatorListener, fabShowAnimatorListener;
    private void setFabVisibilityIfRequired() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            if(!isFabRequired()) {
                if(doneFab.getVisibility() != View.GONE) {
                    fabBackground.animate().translationY(getResources().getDimension(R.dimen.fab_size)
                            + getResources().getDimension(R.dimen.activity_vertical_margin))
                            .translationX(0).scaleX(1).scaleY(1);
                    doneFab.animate().rotation(270).translationY(getResources().getDimension(R.dimen.fab_size)
                            + getResources().getDimension(R.dimen.activity_vertical_margin))
                            .translationX(0)
                            .setListener((Animator.AnimatorListener) fabHideAnimatorListener);
                }
            } else {
                doneFab.setVisibility(View.VISIBLE);
                fabBackground.setVisibility(View.VISIBLE);
                doneFab.animate().rotation(0).translationY(0).setListener((Animator.AnimatorListener) fabShowAnimatorListener);
                fabBackground.animate().translationY(0);
            }
        } else {
            if (!isFabRequired()) {
                if (doneFab.getVisibility() != View.GONE) {
                    doneFab.setVisibility(View.GONE);
                    fabBackground.setVisibility(View.GONE);
                }
            } else {
                doneFab.setVisibility(View.VISIBLE);
                fabBackground.setVisibility(View.VISIBLE);
            }
        }
    }

    private void readSavedData() {
        savedResolutionEnabled = PreferencesHelper.getBoolPreference(this, KEY_RESOLUTION_ENABLED);
        savedOverscanEnabled = PreferencesHelper.getBoolPreference(this, KEY_OVERSCAN_ENABLED);
        savedDensityEnabled = PreferencesHelper.getBoolPreference(this, KEY_DENSITY_ENABLED);
        int width = PreferencesHelper.getIntPreference(this, KEY_RESOLUTION_WIDTH, -1);
        savedWidth = (width==-1)?"":String.valueOf(width);
        int height = PreferencesHelper.getIntPreference(this, KEY_RESOLUTION_HEIGHT, -1);
        savedHeight = (height==-1)?"":String.valueOf(height);
        savedLeftOverscan = PreferencesHelper.getIntPreference(this, KEY_OVERSCAN_LEFT, 0);
        savedRightOverscan = PreferencesHelper.getIntPreference(this, KEY_OVERSCAN_RIGHT, 0);
        savedTopOverscan = PreferencesHelper.getIntPreference(this, KEY_OVERSCAN_TOP, 0);
        savedBottomOverscan = PreferencesHelper.getIntPreference(this, KEY_OVERSCAN_BOTTOM, 0);
        int density = PreferencesHelper.getIntPreference(this, KEY_DENSITY_VALUE, -1);
        savedDensity = (density==-1)?"":String.valueOf(density);

        resolutionEnabledChanged = densityEnabledChanged = overscanEnabledChanged = widthChanged =
                heightChanged = leftOverscanChanged = rightOverscanChanged = topOverscanChanged =
                bottomOverscanChanged = densityChanged = false;
    }

    private void populateDensityCard() {
        setSwitchCheckedAndCallListener(densitySwitch, savedDensityEnabled);
        densityText.setText(savedDensity);
    }

    private void populateOverscanCard() {
        setSwitchCheckedAndCallListener(overscanSwitch, savedOverscanEnabled);
        leftSeekBar.setProgress(savedLeftOverscan);
        rightSeekBar.setProgress(savedRightOverscan);
        topSeekBar.setProgress(savedTopOverscan);
        bottomSeekBar.setProgress(savedBottomOverscan);

    }

    private void populateResolutionCard() {
        setSwitchCheckedAndCallListener(resolutionSwitch, savedResolutionEnabled);
        widthText.setText(savedWidth);
        heightText.setText(savedHeight);
    }

    private void setupFAB(Bundle savedInstanceState) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            fabHideAnimatorListener = new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {}

                @Override
                public void onAnimationEnd(Animator animator) {
                    doneFab.setVisibility(View.GONE);
                    fabBackground.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    onAnimationEnd(animator);
                }

                @Override
                public void onAnimationRepeat(Animator animator) {}
            };
            fabShowAnimatorListener = new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animator) {
                }

                @Override
                public void onAnimationEnd(Animator animator) {
                    fabBackground.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animator) {
                    onAnimationEnd(animator);
                }

                @Override
                public void onAnimationRepeat(Animator animator) {
                }
            };
        }
        doneFab = (FloatingActionButton) findViewById(R.id.fab_done);
        fabBackground = (FloatingActionButton) findViewById(R.id.fab_background);
        if(savedInstanceState == null) {
            Log.d("setupFAB", "savedInstanceState null");
            doneFab.setVisibility(View.INVISIBLE);
            fabBackground.setVisibility(View.INVISIBLE);
        }
        doneFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resolutionCard.setBackgroundColor(Color.WHITE);
                overscanCard.setBackgroundColor(Color.WHITE);
                densityCard.setBackgroundColor(Color.WHITE);
                disableAllCards();
                if (!validateAndSaveData()) {
                    enableAllCards();
                    return;
                }
                float ddsize = getDiagonalDisplaySize();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
//                    Log.d("MainActivity", getDisplayWidth() + "x" + getDisplayHeight());
                    float fabTop = getDisplayHeight() - getResources().getDimension(R.dimen.fab_size) - 2*getResources().getDimension(R.dimen.activity_vertical_margin);
                    float translationX = getDisplayWidth() / 2 - doneFab.getLeft() - doneFab.getWidth() / 2,
                            translationY = -fabTop + getDisplayHeight() / 2 - doneFab.getHeight() / 2;
                    Log.d("MainActivity", translationX + "x" + translationY);
                    doneFab.animate().translationX(translationX)
                            .translationY(translationY);
                    fabBackground.animate().translationX(translationX)
                            .translationY(translationY)
                            .scaleX(ddsize / doneFab.getWidth()).scaleY(ddsize / doneFab.getHeight());
                } else {
                    Toast.makeText(MainActivity.this, "Settings saved", Toast.LENGTH_SHORT).show();
                }
                Runnable r = new Runnable() {
                    @Override
                    public void run() {
                        if(PreferencesHelper.getBoolPreference(MainActivity.this, KEY_MASTER_SWITCH_ON)) {
                            enableService();
                        }
                        init(null);
                        enableAllCards();
                    }
                };
                Handler handler = new Handler();
                handler.postDelayed(r, 1500);
            }
        });
    }

    private float getDiagonalDisplaySize(){
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        return (float)Math.sqrt(displaymetrics.heightPixels*displaymetrics.heightPixels + displaymetrics.widthPixels*displaymetrics.widthPixels);
    }

    protected int getDisplayWidth(){
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.widthPixels;
    }

    protected int getDisplayHeight(){
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.heightPixels;
    }

    private boolean validateAndSaveData(){
        if(!TextUtils.isDigitsOnly(widthText.getText()) || !TextUtils.isDigitsOnly(heightText.getText()) || !TextUtils.isDigitsOnly(densityText.getText())){
            Toast.makeText(this, "Please enter valid inputs", Toast.LENGTH_SHORT).show();
            return false;
        }
        int width, height, density, leftOverscan, rightOverscan, topOverscan, bottomOverscan;
        ScreenShiftService.DisplaySize displaySize = ScreenShiftService.DisplaySize.getDeviceDisplaySize(this);
        try {
            if(!widthText.getText().toString().isEmpty()) width = Integer.parseInt(widthText.getText().toString());
            else width = displaySize.width;
            if(!heightText.getText().toString().isEmpty()) height = Integer.parseInt(heightText.getText().toString());
            else height = displaySize.height;
        } catch (NumberFormatException e) {
            resolutionCard.setBackgroundColor(getResources().getColor(R.color.color_error_background));
            Toast.makeText(this, "Enter valid resolution values", Toast.LENGTH_SHORT).show();
            return false;
        }
        try {
            if(!densityText.getText().toString().isEmpty()) density = Integer.parseInt(densityText.getText().toString());
            else density = 0;
        } catch (NumberFormatException e) {
            densityCard.setBackgroundColor(getResources().getColor(R.color.color_error_background));
            Toast.makeText(this, "Enter valid density value", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(!overrideWarning && (width < displaySize.width/2 || width > displaySize.width * 2
                || height < displaySize.height/2 || height > displaySize.height * 2)){
            resolutionCard.setBackgroundColor(getResources().getColor(R.color.color_warning_background));
            Bundle bundle = new Bundle();
            bundle.putString(DialogFragments.KEY_WARNING_STRING, "Resolution might make the display unusable. Are you sure you want to continue? Wait for 15 seconds if display is unusable.");
            DialogFragment fragment = new DialogFragments.DisplaySettingsWarningDialog();
            fragment.setArguments(bundle);
            fragment.show(getSupportFragmentManager(), "resolutionWarningDialog");
            return false;
        }
        leftOverscan = leftSeekBar.getProgress();
        rightOverscan = rightSeekBar.getProgress();
        topOverscan = topSeekBar.getProgress();
        bottomOverscan = bottomSeekBar.getProgress();
        if(!overrideWarning && overscanSwitch.isChecked() && (leftOverscan+rightOverscan > 50 || topOverscan+bottomOverscan > 50)){
            overscanCard.setBackgroundColor(getResources().getColor(R.color.color_warning_background));
            Bundle bundle = new Bundle();
            bundle.putString(DialogFragments.KEY_WARNING_STRING, "Overscan might make the display unusable. Are you sure you want to continue? Wait for 15 seconds if display is unusable.");
            DialogFragment fragment = new DialogFragments.DisplaySettingsWarningDialog();
            fragment.setArguments(bundle);
            fragment.show(getSupportFragmentManager(), "overscanWarningDialog");
            return false;
        }
        overrideWarning = false;
        PreferencesHelper.setPreference(this, KEY_RESOLUTION_ENABLED, resolutionSwitch.isChecked());
        PreferencesHelper.setPreference(this, KEY_DENSITY_ENABLED, densitySwitch.isChecked());
        PreferencesHelper.setPreference(this, KEY_OVERSCAN_ENABLED, overscanSwitch.isChecked());
        if(!widthText.getText().toString().isEmpty())
            PreferencesHelper.setPreference(this, KEY_RESOLUTION_WIDTH, width);
        else
            PreferencesHelper.setPreference(this, KEY_RESOLUTION_WIDTH, -1);
        if(!heightText.getText().toString().isEmpty())
            PreferencesHelper.setPreference(this, KEY_RESOLUTION_HEIGHT, height);
        else
            PreferencesHelper.setPreference(this, KEY_RESOLUTION_HEIGHT, -1);
        PreferencesHelper.setPreference(this, KEY_OVERSCAN_LEFT, leftOverscan);
        PreferencesHelper.setPreference(this, KEY_OVERSCAN_RIGHT, rightOverscan);
        PreferencesHelper.setPreference(this, KEY_OVERSCAN_TOP, topOverscan);
        PreferencesHelper.setPreference(this, KEY_OVERSCAN_BOTTOM, bottomOverscan);
        if(!densityText.getText().toString().isEmpty())
            PreferencesHelper.setPreference(this, KEY_DENSITY_VALUE, density);
        else
            PreferencesHelper.setPreference(this, KEY_DENSITY_VALUE, -1);
        return true;
    }

    private void enableService(){
        PreferencesHelper.setPreference(this, KEY_MASTER_SWITCH_ON, true);
        startService(new Intent(MainActivity.this, ScreenShiftService.class).setAction(ScreenShiftService.ACTION_START));
        if(!showTimeout) return;

        onPostResumeRunDialog = true;
    }

    private void disableService(){
        PreferencesHelper.setPreference(this, KEY_MASTER_SWITCH_ON, false);
        startService(new Intent(MainActivity.this, ScreenShiftService.class).setAction(ScreenShiftService.ACTION_STOP));
//        disableAllCards();
    }

    private void enableAllCards(){
        setViewEnabled(true, cardsLayout, densitySwitch, overscanSwitch, resolutionSwitch);
        setDensityCardInnerEnabled(densitySwitch.isChecked());
        setOverscanCardInnerEnabled(overscanSwitch.isChecked());
        setResolutionCardInnerEnabled(resolutionSwitch.isChecked());
    }

    private void disableAllCards(){
        setViewEnabled(false, cardsLayout, densitySwitch, overscanSwitch, resolutionSwitch);
        setDensityCardInnerEnabled(false);
        setOverscanCardInnerEnabled(false);
        setResolutionCardInnerEnabled(false);
    }

    private boolean isFabRequired(){
        return resolutionEnabledChanged || densityEnabledChanged|| overscanEnabledChanged ||
                widthChanged || heightChanged || leftOverscanChanged || rightOverscanChanged ||
                topOverscanChanged || bottomOverscanChanged || densityChanged;

    }

    private void setViewEnabled(boolean enabled, View... views) {
        for(View view: views) {
            view.setEnabled(enabled);
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB*//* && !(view instanceof SeekBar)*//*) {
                if(enabled) {
                    view.setAlpha(ENABLED_ALPHA);
                } else {
                    view.setAlpha(DISABLED_ALPHA);
                }
            }*/
        }
    }

    private void setSwitchCheckedAndCallListener(SwitchCompat switchView, boolean isChecked) {
        if(switchView.isChecked() == isChecked) {
            switchView.setChecked(!isChecked);
        }
        switchView.setChecked(isChecked);
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

    @Override
    public void onPositiveButton(DialogFragment fragment, String result) {
        if(fragment instanceof DialogFragments.SaveProfileDialog){
            String name = result.trim();
            ContentValues values = new ContentValues();
            values.put(ProfileEntry.COLUMN_NAME, name);
            values.put(ProfileEntry.COLUMN_RESOLUTION_ENABLED, resolutionSwitch.isChecked() ? 1 : 0);
            values.put(ProfileEntry.COLUMN_OVERSCAN_ENABLED, overscanSwitch.isChecked() ? 1 : 0);
            values.put(ProfileEntry.COLUMN_DENSITY_ENABLED, densitySwitch.isChecked() ? 1 : 0);
            String width = widthText.getText().toString(),
                    height = heightText.getText().toString(),
                    density = densityText.getText().toString();
            if (!width.isEmpty())
                values.put(ProfileEntry.COLUMN_RESOLUTION_WIDTH, Integer.parseInt(width));
            if (!height.isEmpty())
                values.put(ProfileEntry.COLUMN_RESOLUTION_HEIGHT, Integer.parseInt(height));
            if (!density.isEmpty())
                values.put(ProfileEntry.COLUMN_DENSITY_VALUE, Integer.parseInt(density));
            values.put(ProfileEntry.COLUMN_OVERSCAN_LEFT, leftSeekBar.getProgress());
            values.put(ProfileEntry.COLUMN_OVERSCAN_RIGHT, rightSeekBar.getProgress());
            values.put(ProfileEntry.COLUMN_OVERSCAN_TOP, topSeekBar.getProgress());
            values.put(ProfileEntry.COLUMN_OVERSCAN_BOTTOM, bottomSeekBar.getProgress());
            Cursor existCursor = getContentResolver().query(ProfileEntry.CONTENT_URI,
                    null,
                    ProfileEntry.COLUMN_NAME + " = ? ",
                    new String[]{name},
                    null);
            if (existCursor.moveToFirst()) {
                long id = existCursor.getInt(existCursor.getColumnIndex(ProfileEntry._ID));
                getContentResolver().update(ProfileEntry.buildProfileUriWithId(id), values, null, null);
            } else {
                getContentResolver().insert(ProfileEntry.CONTENT_URI, values);
            }
            existCursor.close();
        } else if(fragment instanceof DialogFragments.DisplaySettingsWarningDialog) {
            overrideWarning = true;
            doneFab.performClick();
        }
    }

    @Override
    public void onNegativeButton(DialogFragment fragment) {
        if(fragment instanceof DialogFragments.KeepSettingsDialog) {
            disableService();
        }
    }

    @Override
    public void onItemClick(DialogFragment fragment, int i) {
        if(fragment instanceof DialogFragments.LoadProfileDialog) {
            Log.d("OnClickListener", "Clicked item " + i);
            if(profiles[i].resolutionWidth != -1)
                widthText.setText(String.valueOf(profiles[i].resolutionWidth));
            if(profiles[i].resolutionHeight != -1)
                heightText.setText(String.valueOf(profiles[i].resolutionHeight));
            if(profiles[i].densityValue != -1)
                densityText.setText(String.valueOf(profiles[i].densityValue));
            leftSeekBar.setProgress(profiles[i].overscanLeft);
            rightSeekBar.setProgress(profiles[i].overscanRight);
            topSeekBar.setProgress(profiles[i].overscanTop);
            bottomSeekBar.setProgress(profiles[i].overscanBottom);
            setSwitchCheckedAndCallListener(resolutionSwitch, profiles[i].isResolutionEnabled);
            setSwitchCheckedAndCallListener(densitySwitch, profiles[i].isDensityEnabled);
            setSwitchCheckedAndCallListener(overscanSwitch, profiles[i].isOverscanEnabled);
            setFabVisibilityIfRequired();
            doneFab.performClick();
        }
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
