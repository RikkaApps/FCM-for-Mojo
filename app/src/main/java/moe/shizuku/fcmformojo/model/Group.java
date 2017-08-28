package moe.shizuku.fcmformojo.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.Keep;

import java.lang.ref.WeakReference;

import moe.shizuku.fcmformojo.notification.ChatIcon;

/**
 * Created by rikka on 2017/7/29.
 */

@Keep
public class Group {

    private long uid;
    private String name;
    private WeakReference<Drawable> icon = new WeakReference<>(null);

    public long getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public WeakReference<Drawable> getIcon() {
        return icon;
    }

    public Drawable loadIcon(Context context) {
        if (icon == null || icon.get() == null) {
            Bitmap bitmap = ChatIcon.getIcon(context, uid, Chat.ChatType.GROUP);
            Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);
            icon = new WeakReference<>(drawable);
        }
        return icon.get();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Group group = (Group) o;

        return uid == group.uid;
    }

    @Override
    public int hashCode() {
        return (int) (uid ^ (uid >>> 32));
    }

    @Override
    public String toString() {
        return "Group{" +
                "uid=" + uid +
                ", name='" + name + '\'' +
                '}';
    }
}
