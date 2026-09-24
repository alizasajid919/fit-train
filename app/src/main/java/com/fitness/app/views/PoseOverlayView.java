package com.fitness.app.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.List;

public class PoseOverlayView extends View {

    private Pose currentPose;
    private int imageWidth = 480;
    private int imageHeight = 640;
    private boolean isFrontCamera = true;

    private final Paint paintPoint;
    private final Paint paintLine;

    public PoseOverlayView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        paintPoint = new Paint();
        paintPoint.setColor(0xFF2563EB); // Blue accent
        paintPoint.setStyle(Paint.Style.FILL);
        paintPoint.setAntiAlias(true);
        paintPoint.setStrokeWidth(12f);

        paintLine = new Paint();
        paintLine.setColor(0xFF6366F1); // Accent Purple
        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setAntiAlias(true);
        paintLine.setStrokeWidth(6f);
    }

    public void setPose(Pose pose, int imageWidth, int imageHeight, boolean isFrontCamera) {
        this.currentPose = pose;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.isFrontCamera = isFrontCamera;
        invalidate(); // request redraw
    }

    public void clear() {
        this.currentPose = null;
        invalidate();
    }

    public void setSkeletonColor(int color) {
        paintPoint.setColor(color);
        paintLine.setColor(color);
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (currentPose == null) return;

        List<PoseLandmark> landmarks = currentPose.getAllPoseLandmarks();
        if (landmarks.isEmpty()) return;

        float scaleX = (float) getWidth() / imageWidth;
        float scaleY = (float) getHeight() / imageHeight;

        // Draw connections (skeleton lines)
        drawBone(canvas, PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER, scaleX, scaleY);
        drawBone(canvas, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW, scaleX, scaleY);
        drawBone(canvas, PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST, scaleX, scaleY);
        drawBone(canvas, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW, scaleX, scaleY);
        drawBone(canvas, PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST, scaleX, scaleY);

        drawBone(canvas, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP, scaleX, scaleY);
        drawBone(canvas, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP, scaleX, scaleY);
        drawBone(canvas, PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP, scaleX, scaleY);

        drawBone(canvas, PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE, scaleX, scaleY);
        drawBone(canvas, PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE, scaleX, scaleY);
        drawBone(canvas, PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE, scaleX, scaleY);
        drawBone(canvas, PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE, scaleX, scaleY);

        // Draw points (joints keypoints)
        for (PoseLandmark landmark : landmarks) {
            if (landmark == null || landmark.getInFrameLikelihood() <= 0.4f) continue;
            PointF point = landmark.getPosition();
            float x = point.x * scaleX;
            float y = point.y * scaleY;

            // Mirror if using front camera
            if (isFrontCamera) {
                x = getWidth() - x;
            }

            canvas.drawCircle(x, y, 10f, paintPoint);
        }
    }

    private void drawBone(Canvas canvas, int startJoint, int endJoint, float scaleX, float scaleY) {
        PoseLandmark start = currentPose.getPoseLandmark(startJoint);
        PoseLandmark end = currentPose.getPoseLandmark(endJoint);

        if (start != null && end != null && start.getInFrameLikelihood() > 0.4f && end.getInFrameLikelihood() > 0.4f) {
            PointF startPoint = start.getPosition();
            PointF endPoint = end.getPosition();

            float sx = startPoint.x * scaleX;
            float sy = startPoint.y * scaleY;
            float ex = endPoint.x * scaleX;
            float ey = endPoint.y * scaleY;

            if (isFrontCamera) {
                sx = getWidth() - sx;
                ex = getWidth() - ex;
            }

            canvas.drawLine(sx, sy, ex, ey, paintLine);
        }
    }
}
