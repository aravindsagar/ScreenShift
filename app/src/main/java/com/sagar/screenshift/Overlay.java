package com.sagar.screenshift;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by aravind on 23/9/14.
 * A base class for managing overlay windows.
 */
public abstract class Overlay {
    private static final String LOG_TAG = Overlay.class.getSimpleName();

    private LayoutInflater inflater;
    private WindowManager windowManager;
    private Context context;
    private View layout;
    private WindowManager.LayoutParams params;

    private boolean isAdded = false;

    public Overlay(Context context, WindowManager windowManager){
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.windowManager = windowManager;
    }

    protected abstract WindowManager.LayoutParams buildLayoutParams();

    /**
     * Gets the layout to be shown by the window manager.
     * Recycling of already inflated view must be taken care of by the implementation. Else memory leaks can occur!
     * @return layout to be shown by the window manager.
     */
    protected abstract View buildLayout();

    /**
     * Adds the view specified by buildLayout() to the windowManager passed during initialization
     */
    public void execute(){
        layout = buildLayout();
        if(isAdded) return;
        layout.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        if(params == null){
            params = buildLayoutParams();
        }
        try {
            windowManager.addView(layout, params);
            isAdded = true;
            layout.invalidate();
        } catch (IllegalStateException e){
            isAdded = false;
        }
    }

    /**
     * Removes the view specified by buildLayout() from the windowManager passed during initialization
     */
    public void remove(){
        if(!isAdded) return;
        try {
            windowManager.removeView(layout);
            isAdded = false;
        } catch (Exception e){
            //Do nothing
        }
    }

    protected int getDisplayWidth(){
        DisplayMetrics displaymetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.widthPixels;
    }

    protected int getDisplayHeight(){
        DisplayMetrics displaymetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.heightPixels;
    }


    protected int getFullScreenSystemUiVisibility(){
        int systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            systemUiVisibility |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        }
        return systemUiVisibility;
    }

    public LayoutInflater getInflater() {
        return inflater;
    }

    public Context getContext() {
        return context;
    }
}
