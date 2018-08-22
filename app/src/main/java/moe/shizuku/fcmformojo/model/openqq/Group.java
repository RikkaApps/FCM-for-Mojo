package moe.shizuku.fcmformojo.model.openqq;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.support.annotation.Keep;

import java.lang.ref.WeakReference;
import java.util.List;

import moe.shizuku.fcmformojo.model.Chat;
import moe.shizuku.fcmformojo.notification.UserIcon;

@Keep
public class Group extends BaseEntity {

    private List<User> member;

    public List<User> getMember() {
        return member;
    }

    @Override
    public Drawable loadIcon(Context context) {
        if (icon == null || icon.get() == null) {
            Bitmap bitmap = UserIcon.getIcon(context, uid, Chat.ChatType.GROUP);
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
    public String toString() {
        return "Group{" +
                "uid=" + uid +
                ", name='" + name + '\'' +
                '}';
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

    protected Group(Parcel in) {
        readFromParcel(in);
        this.member = in.createTypedArrayList(User.CREATOR);
    }

    public static final Creator<Group> CREATOR = new Creator<Group>() {
        @Override
        public Group createFromParcel(Parcel source) {
            return new Group(source);
        }

        @Override
        public Group[] newArray(int size) {
            return new Group[size];
        }
    };
}
