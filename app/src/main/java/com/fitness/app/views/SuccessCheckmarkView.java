package com.fitness.app.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.Nullable;

public class SuccessCheckmarkView extends View {

    private Paint circlePaint;
    private Paint checkPaint;
    private Path checkPath;
    private Path animatedCheckPath;
    private PathMeasure pathMeasure;
    private float progress = 0f;
    private int successColor = 0xFF2E7D32; // Fallback to #2E7D32 (workout_success)

    public SuccessCheckmarkView(Context context) {
        super(context);
        init();
    }

    public SuccessCheckmarkView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SuccessCheckmarkView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Retrieve color from resources if possible
        try {
            successColor = getResources().getColor(getResources().getIdentifier("workout_success", "color", getContext().getPackageName()));
        } catch (Exception e) {
            // Use default
        }

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setColor(successColor);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(12f);
        circlePaint.setStrokeCap(Paint.Cap.ROUND);

        checkPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        checkPaint.setColor(successColor);
        checkPaint.setStyle(Paint.Style.STROKE);
        checkPaint.setStrokeWidth(14f);
        checkPaint.setStrokeCap(Paint.Cap.ROUND);
        checkPaint.setStrokeJoin(Paint.Join.ROUND);

        checkPath = new Path();
        animatedCheckPath = new Path();
        pathMeasure = new PathMeasure();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        
        float cx = w / 2f;
        float cy = h / 2f;
        float r = Math.min(w, h) / 3f;

        // Construct checkmark path inside the circle bounds
        checkPath.reset();
        checkPath.moveTo(cx - r * 0.45f, cy + r * 0.05f);
        checkPath.lineTo(cx - r * 0.1f, cy + r * 0.4f);
        checkPath.lineTo(cx + r * 0.5f, cy - r * 0.3f);
        
        pathMeasure.setPath(checkPath, false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float cx = getWidth() / 2f;
        float cy = getHeight() / 2f;
        float r = Math.min(getWidth(), getHeight()) / 3f;

        // Draw Circle segment by segment
        float circleProgress = Math.min(1f, progress * 2f);
        if (circleProgress > 0f) {
            canvas.drawArc(
                cx - r, cy - r, cx + r, cy + r,
                -90f, 360f * circleProgress,
                false, circlePaint
            );
        }

        // Draw Checkmark segment by segment
        float checkProgress = Math.max(0f, (progress - 0.5f) * 2f);
        if (checkProgress > 0f) {
            animatedCheckPath.reset();
            float length = pathMeasure.getLength();
            pathMeasure.getSegment(0f, length * checkProgress, animatedCheckPath, true);
            canvas.drawPath(animatedCheckPath, checkPaint);
        }
    }

    public void startAnimation(final Runnable onEnd) {
        progress = 0f;
        invalidate();
        
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(600);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(animation -> {
            progress = (float) animation.getAnimatedValue();
            invalidate();
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (onEnd != null) {
                    onEnd.run();
                }
            }
        });
        animator.start();
    }
}
