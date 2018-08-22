package moe.shizuku.fcmformojo.utils;

import android.support.annotation.Px;
import android.view.View;

public class ViewUtils {

    public static void setPaddingVertical(View v, @Px int paddingVertical) {
        v.setPaddingRelative(v.getPaddingStart(), paddingVertical, v.getPaddingEnd(), paddingVertical);
    }
}
