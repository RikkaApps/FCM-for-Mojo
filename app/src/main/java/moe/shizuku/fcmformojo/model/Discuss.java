package moe.shizuku.fcmformojo.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Keep;

import java.lang.ref.WeakReference;

import moe.shizuku.fcmformojo.notification.ChatIcon;

/**
 * Created by rikka on 2017/9/2.
 */

@Keep
public class Discuss {

    private long id;
    private String name;

    private WeakReference<Drawable> icon = new WeakReference<>(null);

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public WeakReference<Drawable> getIcon() {
        return icon;
    }

    public Drawable loadIcon(Context context) {
        if (icon == null || icon.get() == null) {
            Bitmap bitmap = ChatIcon.getDefault(context, (int) (id % 7), true);
            Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);
            icon = new WeakReference<>(drawable);
        }
        return icon.get();
    }
}
