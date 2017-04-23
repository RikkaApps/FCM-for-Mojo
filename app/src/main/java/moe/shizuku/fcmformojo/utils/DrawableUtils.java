package moe.shizuku.fcmformojo.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;

/**
 * Created by Rikka on 2017/4/22.
 */

public class DrawableUtils {

    public static Bitmap toBitmap(Context context, @DrawableRes int drawable) {
        return toBitmap(context.getDrawable(drawable));
    }

    public static Bitmap toBitmap(Drawable drawable) {
        if (drawable == null) {
            return null;
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
