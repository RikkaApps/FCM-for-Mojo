package moe.shizuku.fcmformojo.model.openqq;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.support.annotation.Keep;

import java.lang.ref.WeakReference;
import java.util.List;

import moe.shizuku.fcmformojo.notification.UserIcon;

@Keep
public class Discuss extends BaseEntity {

    private List<User> member;

    public List<User> getMember() {
        return member;
    }

    @Override
    public Drawable loadIcon(Context context) {
        if (icon == null || icon.get() == null) {
            Bitmap bitmap = UserIcon.getDefault(context, (int) id, true);
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
        dest.writeTypedList(this.member);
    }

    protected Discuss(Parcel in) {
        readFromParcel(in);
        this.member = in.createTypedArrayList(User.CREATOR);
    }

    public static final Creator<Discuss> CREATOR = new Creator<Discuss>() {
        @Override
        public Discuss createFromParcel(Parcel source) {
            return new Discuss(source);
        }

        @Override
        public Discuss[] newArray(int size) {
            return new Discuss[size];
        }
    };
}
