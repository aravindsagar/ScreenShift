package com.sagar.screenshift;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;

/**
 * Created by aravind on 13/10/14.
 * Class to store pictures for environment and users
 */
public class Picture {
    private static final String PACKAGE_NAME = Picture.class.getPackage().getName();

    /**
     * Picture type color: A circle with a specific color with a letter written at the center will
     * be shown. The color is is stored in backgroundColor. The character to be drawn should be
     * passed in to get the bitmap,else it will use a '?' instead
     */
    public static final String PICTURE_TYPE_COLOR = PACKAGE_NAME + ".PICTURE_TYPE.COLOR";

    /**
     * A built in picture is used. Background color should be stored in backgroundColor,
     * and drawable name in drawableName variable
     */
    public static final String PICTURE_TYPE_BUILT_IN = PACKAGE_NAME + ".PICTURE_TYPE.BUILT_IN";

    /**
     * Custom picture, selected by the user from gallery. Image from database will be stored to image[]
     */
    public static final String PICTURE_TYPE_CUSTOM = PACKAGE_NAME + ".PICTURE_TYPE.CUSTOM";

    public static final int CROP_MODE_CENTER = 200;
    public static final int CROP_MODE_CUT_CORNERS = 201;

    private String pictureType;
    private String backgroundColor;
    private String drawableName;
    private byte[] image;
    private int borderType;

    public Picture(String pictureType, String backgroundColor, String drawableName, byte[] image){
        this(pictureType, backgroundColor, drawableName, image, CharacterDrawable.BORDER_DARKER);
    }

    public Picture(String pictureType, String backgroundColor, String drawableName, byte[] image, int borderType){
        this.backgroundColor = backgroundColor;
        this.pictureType = pictureType;
        this.image = image;
        this.borderType = borderType;
        this.drawableName = drawableName;
    }

    public static Bitmap getCroppedBitmap(Bitmap bitmap, int borderColor){
        return getCroppedBitmap(bitmap, borderColor, CROP_MODE_CUT_CORNERS);
    }

    public static  Bitmap getCroppedBitmap(Bitmap bitmap, int borderColor, int mode) {
        Bitmap output;

        final int radius;
        switch (mode){
            case CROP_MODE_CENTER:
                radius = (int) (Math.sqrt(bitmap.getWidth() * bitmap.getWidth() + bitmap.getHeight() * bitmap.getHeight()))/2;
                break;
            case CROP_MODE_CUT_CORNERS:
            default:
                radius = Math.min(bitmap.getWidth(), bitmap.getHeight()) / 2;
        }
        output = Bitmap.createBitmap(radius * 2,
                radius * 2, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect srcRect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final Rect rect = new Rect(radius - bitmap.getWidth()/2, radius - bitmap.getHeight()/2,
                radius + bitmap.getWidth()/2, radius + bitmap.getHeight()/2);
        paint.setColor(color);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);

        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        canvas.drawCircle(radius, radius, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, srcRect, rect, paint);
        //Bitmap _bmp = Bitmap.createScaledBitmap(output, 60, 60, false);
        //return _bmp;
        Paint borderPaint = new Paint();
        final int STROKE_WIDTH = radius/10;
        borderPaint.setColor(borderColor);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setAntiAlias(true);
        borderPaint.setStrokeWidth(STROKE_WIDTH);
        canvas.drawCircle(canvas.getWidth()/2.0f, canvas.getHeight()/2.0f,
                radius - STROKE_WIDTH/2, borderPaint);
        return output;
    }

    public String getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(String backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public String getPictureType() {
        return pictureType;
    }

    public void setPictureType(String pictureType) {
        this.pictureType = pictureType;
    }

    public Drawable getDrawable(Character c, Context context){
        if(pictureType.equals(PICTURE_TYPE_COLOR)){
            return new CharacterDrawable(c, Integer.parseInt(backgroundColor), borderType);
        } else if(pictureType.equals(PICTURE_TYPE_BUILT_IN)){
            Resources resources = context.getResources();
            final int resourceId = resources.getIdentifier(drawableName, "drawable",
                    context.getPackageName());
            return resources.getDrawable(resourceId);
        } else if(pictureType.equals(PICTURE_TYPE_CUSTOM)){
            return byteToDrawable(image, context);
        }
        return null;
    }

    public void setDrawable(BitmapDrawable drawable){
        image = drawableToByteArray(drawable);
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

    public String getDrawableName() {
        return drawableName;
    }

    public void setDrawableName(String drawableName) {
        this.drawableName = drawableName;
    }

    public static class PictureTouchListener implements View.OnTouchListener{
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    ImageView view = (ImageView) v;
                    //overlay is black with transparency of 0x77 (119)
                    view.getDrawable().setColorFilter(0x77777777, PorterDuff.Mode.SRC_ATOP);
                    view.setAlpha(0.5f);
                    view.invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL: {
                    ImageView view = (ImageView) v;
                    //clear the overlay
                    view.getDrawable().clearColorFilter();
                    view.setAlpha(1.0f);
                    view.invalidate();
                    break;
                }
            }

            return false;
        }
    }

    public static byte[] drawableToByteArray(Drawable d) {

        if (d != null) {
            Bitmap imageBitmap = ((BitmapDrawable) d).getBitmap();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);

            return baos.toByteArray();
        } else
            return null;

    }


    public static Drawable byteToDrawable(byte[] data, Context context) {

        if (data == null)
            return null;
        else
            return new BitmapDrawable(context.getResources(), BitmapFactory.decodeByteArray(data, 0, data.length));
    }
}
