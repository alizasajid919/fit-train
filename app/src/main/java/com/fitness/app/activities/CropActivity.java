package com.fitness.app.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.fitness.app.R;

import java.io.File;
import java.io.FileOutputStream;

public class CropActivity extends AppCompatActivity implements View.OnTouchListener {

    private ImageView ivCropImage;
    private Uri sourceUri;

    // Matrix fields for dragging and scaling
    private final Matrix matrix = new Matrix();
    private final Matrix savedMatrix = new Matrix();

    // Touch states
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    // Movement tracking vectors
    private final PointF start = new PointF();
    private final PointF mid = new PointF();
    private float oldDist = 1f;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        ivCropImage = findViewById(R.id.ivCropImage);
        ivCropImage.setOnTouchListener(this);

        String uriStr = getIntent().getStringExtra("image_uri");
        if (uriStr == null) {
            Toast.makeText(this, "No image provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        sourceUri = Uri.parse(uriStr);

        // Load image using Glide to get the bitmap and set it up
        Glide.with(this)
                .asBitmap()
                .load(sourceUri)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        ivCropImage.setImageBitmap(resource);
                        setupInitialMatrix(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        Toast.makeText(CropActivity.this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

        findViewById(R.id.btnCropCancel).setOnClickListener(v -> finish());

        findViewById(R.id.btnCropRotate).setOnClickListener(v -> rotateImage());

        findViewById(R.id.btnCropDone).setOnClickListener(v -> performCrop());
    }

    private void setupInitialMatrix(Bitmap bitmap) {
        // Fit the image inside the crop window initially
        ivCropImage.post(() -> {
            int viewWidth = ivCropImage.getWidth();
            int viewHeight = ivCropImage.getHeight();
            if (viewWidth == 0 || viewHeight == 0) return;

            int imgWidth = bitmap.getWidth();
            int imgHeight = bitmap.getHeight();

            float scale;
            if (imgWidth * viewHeight > viewWidth * imgHeight) {
                scale = (float) viewHeight / (float) imgHeight;
            } else {
                scale = (float) viewWidth / (float) imgWidth;
            }

            matrix.reset();
            matrix.postScale(scale, scale);
            
            // Center the image
            float dx = (viewWidth - imgWidth * scale) / 2f;
            float dy = (viewHeight - imgHeight * scale) / 2f;
            matrix.postTranslate(dx, dy);

            ivCropImage.setImageMatrix(matrix);
        });
    }

    private void rotateImage() {
        matrix.postRotate(90, ivCropImage.getWidth() / 2f, ivCropImage.getHeight() / 2f);
        ivCropImage.setImageMatrix(matrix);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        ImageView view = (ImageView) v;

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    midPoint(mid, event);
                    mode = ZOOM;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
                } else if (mode == ZOOM) {
                    float newDist = spacing(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = newDist / oldDist;
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
                break;
        }

        view.setImageMatrix(matrix);
        return true;
    }

    // Determine the distance between two fingers
    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    // Calculate the mid point of the two fingers
    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2f, y / 2f);
    }

    private void performCrop() {
        try {
            // Get view size and overlay circular crop size
            int viewWidth = ivCropImage.getWidth();
            int viewHeight = ivCropImage.getHeight();
            int size = Math.min(viewWidth, viewHeight) * 4 / 5;
            int left = (viewWidth - size) / 2;
            int top = (viewHeight - size) / 2;

            // 1. Capture the exact visible sub-region drawn inside the circle window
            Bitmap croppedBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(croppedBitmap);
            canvas.translate(-left, -top);
            ivCropImage.draw(canvas);

            // 2. Scale it down to 512x512 for optimal storage size and high-fidelity display
            Bitmap finalBitmap = Bitmap.createScaledBitmap(croppedBitmap, 512, 512, true);
            if (finalBitmap != croppedBitmap) {
                croppedBitmap.recycle();
            }

            // 3. Compress and save to cache file
            File cacheFile = new File(getCacheDir(), "cropped_profile.jpg");
            FileOutputStream out = new FileOutputStream(cacheFile);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            finalBitmap.recycle();

            // 4. Return the cache file Uri as result
            Intent result = new Intent();
            result.putExtra("cropped_uri", Uri.fromFile(cacheFile).toString());
            setResult(RESULT_OK, result);
            finish();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Cropping failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
