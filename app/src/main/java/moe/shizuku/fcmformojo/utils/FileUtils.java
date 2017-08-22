package moe.shizuku.fcmformojo.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;

/**
 * Created by Rikka on 2017/4/22.
 */

public class FileUtils {

    public static File getExternalStoragePublicFile(String type, String path, String filename) {
        return new File(Environment.getExternalStoragePublicDirectory(type).getAbsolutePath() + "/" + path, filename);
    }

    public static File getCacheFile(Context context, String filename) {
        if (context.getExternalCacheDir() != null) {
            return new File(context.getExternalCacheDir(), filename);
        } else {
            return new File(context.getCacheDir(), filename);
        }
    }

    public static File getInternalCacheFile(Context context, String filename) {
        return new File(context.getCacheDir(), filename);
    }
}
