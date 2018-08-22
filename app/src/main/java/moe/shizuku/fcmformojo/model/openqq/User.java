package moe.shizuku.fcmformojo.model.openqq;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.support.annotation.Keep;

import java.lang.ref.WeakReference;

import moe.shizuku.fcmformojo.model.Chat;
import moe.shizuku.fcmformojo.notification.UserIcon;

@Keep
public class User extends BaseEntity {

    private static User sSelf = new User(0,0, null);

    public static User getSelf() {
        return sSelf;
    }

    public static void setSelf(User self) {
        User.sSelf = self;
    }

    @Override
    public Drawable loadIcon(Context context) {
        if (icon == null || icon.get() == null) {
            Bitmap bitmap = UserIcon.getIcon(context, uid, Chat.ChatType.FRIEND);
            Drawable drawable = new BitmapDrawable(context.getResources(), bitmap);
            icon = new WeakReference<>(drawable);
        }
        return icon.get();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
    }

    public User(long id, long uid, String name) {
        super(id, uid, name);
    }

    private User(Parcel in) {
        super();
        readFromParcel(in);
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
