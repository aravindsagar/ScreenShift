package com.sagar.screenshift2;

import android.app.Activity;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.sagar.screenshift2.data_objects.App;
import com.sagar.screenshift2.data_objects.Profile;
import com.sagar.screenshift2.profileDb.ProfileDbContract.AppProfileEntry;

import java.util.List;

import static com.sagar.screenshift2.PreferencesHelper.KEY_APP_PROFILE_INFO_SHOWN;

public class ProfilesActivity extends AppCompatActivity implements DialogFragments.DialogListener {

    private ListView appsListView;
    private ProgressBar progressBar;
    private List<App> apps;
    private String clickedAppPackage;
    private Profile[] profiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profiles);

        appsListView = (ListView)    findViewById(R.id.list_view_app_profiles);
        progressBar  = (ProgressBar) findViewById(R.id.progress_bar_app_profiles);

        setProgressBarIndeterminateVisibility(true);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setIndeterminate(true);

        showInfoIfRequired();

        new GetAppsAsyncTask().execute();

        appsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                clickedAppPackage = apps.get(position).getPackageName();
                profiles = Profile.getAllProfiles(ProfilesActivity.this);
                if (profiles != null && profiles.length > 0) {
                    String[] itemStrings = new String[profiles.length + 1];
                    itemStrings[0] = getString(R.string.text_default);
                    for (int i = 1; i <= profiles.length; i++) {
                        Profile profile = profiles[i-1];
                        itemStrings[i] = profile.name + " " + profile.resolutionWidth + "x" +
                                profile.resolutionHeight;
                    }
                    Bundle bundle = new Bundle();
                    bundle.putStringArray(DialogFragments.KEY_LIST_ITEM_STRINGS, itemStrings);
                    DialogFragment dialogFragment = new DialogFragments.LoadProfileDialog();
                    dialogFragment.setArguments(bundle);
                    dialogFragment.show(getSupportFragmentManager(), "loadProfileDialog");
                }
            }
        });
    }

    private void showInfoIfRequired(){
        if(PreferencesHelper.getBoolPreference(this, KEY_APP_PROFILE_INFO_SHOWN)) {
            return;
        }
        showInfo();
        PreferencesHelper.setPreference(this, KEY_APP_PROFILE_INFO_SHOWN, true);
    }

    private void showInfo() {
        new AlertDialog.Builder(this).setTitle(R.string.density_reboot_test_title)
                .setMessage(getString(R.string.info_per_app_profiles))
                .setPositiveButton(getString(R.string.got_it), null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profiles, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_help) {
            showInfo();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPositiveButton(DialogFragment fragment, String result) {

    }

    @Override
    public void onNegativeButton(DialogFragment fragment) {

    }

    @Override
    public void onItemClick(DialogFragment fragment, int i) {
        if(fragment instanceof DialogFragments.LoadProfileDialog) {
            getContentResolver().delete(AppProfileEntry.CONTENT_URI,
                    AppProfileEntry.COLUMN_PACKAGE_NAME + " = ? ",
                    new String[]{clickedAppPackage});
            if(i>0) {
                i--;
                ContentValues values = new ContentValues();
                values.put(AppProfileEntry.COLUMN_PACKAGE_NAME, clickedAppPackage);
                values.put(AppProfileEntry.COLUMN_PROFILE_ID, profiles[i].id);
                getContentResolver().insert(AppProfileEntry.CONTENT_URI, values);
            }
            ((AppProfilesListAdapter) appsListView.getAdapter()).reloadAppProfiles();
        }
    }

    private Activity getActivity() {
        return this;
    }

    private class GetAppsAsyncTask extends AsyncTask<Void, Void, List<App>> {
        @Override
        protected List<App> doInBackground(Void... params) {
            PackageManager packageManager = getActivity().getPackageManager();
            return App.getAllApps(packageManager);
        }

        @Override
        protected void onPostExecute(List<App> apps) {
            ProfilesActivity.this.apps = apps;
            appsListView.setAdapter(new AppProfilesListAdapter(getActivity(),
                    R.layout.list_item_app_profile, apps));
            getActivity().setProgressBarIndeterminateVisibility(false);
            progressBar.setVisibility(View.GONE);
            super.onPostExecute(apps);
        }
    }
}
