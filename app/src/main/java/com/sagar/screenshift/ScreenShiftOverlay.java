package com.sagar.screenshift;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import eu.chainfire.libsuperuser.Shell;

/**
 * Created by aravind on 11/6/15.
 */
public class ScreenShiftOverlay extends Overlay {

    private View layout;

    public ScreenShiftOverlay(Context context, WindowManager windowManager) {
        super(context, windowManager);
    }

    @Override
    protected WindowManager.LayoutParams buildLayoutParams() {
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT,
                50, WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                        WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.x = 0;
        params.y = 0;
        params.gravity = Gravity.TOP | Gravity.START;
        return params;
    }

    @Override
    protected View buildLayout() {
        if(layout == null){
            layout = getInflater().inflate(R.layout.overlay_screen_shift, null, false);
//            layout.setBackgroundColor(Color.WHITE);
            layout.setOnTouchListener(touchListener);
        }
        return layout;
    }

    View.OnTouchListener touchListener = new View.OnTouchListener() {

        float downX, downY;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            float x = motionEvent.getRawX(), y = motionEvent.getRawY();
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    downX = x;
                    downY = y;
                    break;
                case MotionEvent.ACTION_UP:
                    sendSwipe(downX, x, downY, y, motionEvent.getEventTime() - motionEvent.getDownTime());
                    break;
            }
            return true;
        }

        private void sendSwipe(final float x1, final float x2, final float y1, final float y2, final long duration){
            new AsyncTask<Void, Void, Void>(){
                @Override
                protected Void doInBackground(Void... voids) {
                    if(Shell.SU.available()){
                        Shell.SU.run("input swipe " + x1 + " " + (y1+100) + " " + x2 + " " + (y2+100) + " " + duration);
                    }
                    return null;
                }
            }.execute();
        }
    };
}
