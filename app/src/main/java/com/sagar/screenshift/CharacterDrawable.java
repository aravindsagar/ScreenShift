package com.sagar.screenshift;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

/**
 * Created by aravind on 8/10/14.
 * Creates a drawable with a character in the center enclosed in a circular colored region
 */
public class CharacterDrawable extends Drawable {
    public static final int BORDER_LIGHTER = 1000;
    public static final int BORDER_DARKER = 1001;

    private final char character;
    private final Paint textPaint;
    private final Paint borderPaint;
    private final Paint backgroundPaint;
    private static final int STROKE_WIDTH = 4;

    public CharacterDrawable(char character, int color, int borderType) {
        super();
        this.character = character;
        this.textPaint = new Paint();
        this.borderPaint = new Paint();
        this.backgroundPaint = new Paint();

        // text paint settings
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);
        textPaint.setStyle(Paint.Style.FILL);
        textPaint.setTextAlign(Paint.Align.CENTER);

        // border paint settings
        if(borderType == BORDER_LIGHTER) {
            borderPaint.setColor(getLighterShade(color));
        } else {
            borderPaint.setColor(getDarkerShade(color));
        }
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setAntiAlias(true);
        borderPaint.setStrokeWidth(STROKE_WIDTH);

        backgroundPaint.setColor(color);
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setStyle(Paint.Style.FILL);
    }

    public CharacterDrawable(char character, int color){
        this(character, color, BORDER_DARKER);
    }

    @Override
    public void draw(Canvas canvas) {
        //draw background
        canvas.drawCircle(canvas.getWidth() / 2.0f, canvas.getHeight() / 2.0f,
                Math.min(canvas.getWidth() / 2.0f, canvas.getHeight() / 2.0f) - STROKE_WIDTH / 2, backgroundPaint);

        // draw border
        canvas.drawCircle(canvas.getWidth() / 2.0f, canvas.getHeight() / 2.0f,
                Math.min(canvas.getWidth() / 2.0f, canvas.getHeight() / 2.0f) - STROKE_WIDTH / 2, borderPaint);

        // draw text
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        textPaint.setTextSize((int) (2.0 * height / 3.0));
        canvas.drawText(String.valueOf(character), width / 2, height / 2 - ((textPaint.descent() + textPaint.ascent()) / 2),
                textPaint);
    }

    @Override
    public void setAlpha(int alpha) {
        textPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        textPaint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    private static final float SHADE_FACTOR = 0.9f;

    public static int getDarkerShade(int color) {
        return Color.rgb((int) (SHADE_FACTOR * Color.red(color)),
                (int) (SHADE_FACTOR * Color.green(color)),
                (int) (SHADE_FACTOR * Color.blue(color)));
    }

    public static int getLighterShade(int color) {
        return Color.rgb((int)Math.min(0xff, (Color.red(color) / SHADE_FACTOR)),
                (int)Math.min(0xff, (Color.green(color) / SHADE_FACTOR)),
                (int)Math.min(0xff, (Color.blue(color) / SHADE_FACTOR)));
    }
}
