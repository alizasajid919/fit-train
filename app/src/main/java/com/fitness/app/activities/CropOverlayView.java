package com.fitness.app.activities;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

public class CropOverlayView extends View {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public CropOverlayView(Context context) {
        super(context);
    }

    public CropOverlayView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CropOverlayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Save the canvas layer to apply transfer modes
        int sc = canvas.saveLayer(0, 0, getWidth(), getHeight(), null);
        
        // 1. Draw solid semi-transparent dark overlay
        paint.setColor(0xB3000000); // 70% black
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, 0, getWidth(), getHeight(), paint);
        
        // 2. Cut out the transparent circular window in the center (1:1 aspect ratio)
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        int size = Math.min(getWidth(), getHeight()) * 4 / 5;
        int radius = size / 2;
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        canvas.drawCircle(cx, cy, radius, paint);
        
        // Clear transfer mode and restore the canvas
        paint.setXfermode(null);
        canvas.restoreToCount(sc);
        
        // 3. Draw premium white circle border
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        canvas.drawCircle(cx, cy, radius, paint);
    }
}
