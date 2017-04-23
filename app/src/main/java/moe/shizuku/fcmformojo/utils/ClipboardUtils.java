package moe.shizuku.fcmformojo.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;

/**
 * Created by Rikka on 2017/4/21.
 */

public class ClipboardUtils {

    public static boolean put(Context context, CharSequence str) {
        try {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("label", str);
            clipboard.setPrimaryClip(clip);
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}
