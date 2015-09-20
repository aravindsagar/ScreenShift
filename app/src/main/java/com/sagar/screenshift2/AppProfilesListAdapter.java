package com.sagar.screenshift2;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.sagar.screenshift2.data_objects.App;
import com.sagar.screenshift2.data_objects.Profile;

import java.util.HashMap;
import java.util.List;

/**
 * Created by aravind on 20/9/15.
 */
public class AppProfilesListAdapter extends ArrayAdapter<App> {

    private int mLayout;
    private HashMap<String, Profile> appProfiles;

    public AppProfilesListAdapter(Context context, int layout, List<App> apps) {
        super(context, layout, apps);
        mLayout = layout;
        appProfiles = App.readAppProfiles(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final App app = getItem(position);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final AppProfileViewHolder viewHolder;
        if(convertView == null){
            convertView = inflater.inflate(mLayout, parent, false);
            viewHolder = new AppProfileViewHolder();
            viewHolder.appIcon = (ImageView) convertView.findViewById(R.id.image_view_app_icon);
            viewHolder.appName = (TextView) convertView.findViewById(R.id.text_view_app_name);
            viewHolder.packageName = (TextView) convertView.findViewById(R.id.text_view_package_name);
            viewHolder.profile = (TextView) convertView.findViewById(R.id.text_view_profile);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (AppProfileViewHolder) convertView.getTag();
        }

        viewHolder.appIcon.setImageDrawable(app.getAppIcon());
        viewHolder.appName.setText(app.getAppName());
        viewHolder.packageName.setText(app.getPackageName());

        Profile profile = appProfiles.get(app.getPackageName());
        if(profile != null) {
            viewHolder.profile.setText(profile.resolutionHeight + "x" + profile.resolutionWidth);
        } else {
            viewHolder.profile.setText("Default");
        }

        return convertView;
    }

    private static class AppProfileViewHolder {
        ImageView appIcon;
        TextView appName, packageName, profile;
    }

}
