package com.sagar.screenshift2;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.sagar.screenshift2.data_objects.App;

import java.util.List;

public class ProfilesActivity extends AppCompatActivity implements DialogFragments.DialogListener {

    private ListView appsListView;
    private ProgressBar progressBar;

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

        new GetAppsAsyncTask().execute();
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
        if (id == R.id.action_settings) {
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
            appsListView.setAdapter(new AppProfilesListAdapter(getActivity(),
                    R.layout.list_item_app_profile, apps));
            getActivity().setProgressBarIndeterminateVisibility(false);
            progressBar.setVisibility(View.GONE);
            super.onPostExecute(apps);
        }
    }
}
