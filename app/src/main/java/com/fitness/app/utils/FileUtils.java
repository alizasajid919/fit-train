package com.fitness.app.utils;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtils {

    /**
     * Copies a content URI to app-private storage to prevent SecurityExceptions on app restart.
     */
    public static String copyUriToInternalStorage(Context context, Uri uri, String destFileName) {
        try {
            File dir = new File(context.getFilesDir(), "transformation_photos");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File destFile = new File(dir, destFileName);
            // Delete old file if exists
            if (destFile.exists()) {
                destFile.delete();
            }

            InputStream is = context.getContentResolver().openInputStream(uri);
            if (is == null) return uri.toString();

            OutputStream os = new FileOutputStream(destFile);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            return Uri.fromFile(destFile).toString();
        } catch (Exception e) {
            e.printStackTrace();
            return uri.toString();
        }
    }
    
    /**
     * Safely deletes all transformation photos from internal storage.
     */
    public static void clearInternalTransformationPhotos(Context context) {
        try {
            File dir = new File(context.getFilesDir(), "transformation_photos");
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    for (File f : files) {
                        f.delete();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
