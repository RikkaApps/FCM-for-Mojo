package moe.shizuku.fcmformojo.utils;

import android.view.View;

import androidx.annotation.Px;

public class ViewUtils {

    public static void setPaddingVertical(View v, @Px int paddingVertical) {
        v.setPaddingRelative(v.getPaddingStart(), paddingVertical, v.getPaddingEnd(), paddingVertical);
    }
}
