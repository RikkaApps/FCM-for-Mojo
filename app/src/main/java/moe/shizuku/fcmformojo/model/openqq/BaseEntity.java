package moe.shizuku.fcmformojo.model.openqq;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Keep;

import java.lang.ref.WeakReference;

@Keep
public abstract class BaseEntity implements Parcelable {

    protected long id;
    protected long uid;
    protected String name;
    protected WeakReference<Drawable> icon = new WeakReference<>(null);

    public BaseEntity() {
    }

    public BaseEntity(long id, long uid, String name) {
        this.id = id;
        this.uid = uid;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public long getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public WeakReference<Drawable> getIcon() {
        return icon;
    }

    public abstract Drawable loadIcon(Context context);

    @Override
    public int hashCode() {
        return (int) (uid ^ (uid >>> 32));
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeLong(this.uid);
        dest.writeString(this.name);
    }

    public void readFromParcel(Parcel in) {
        this.id = in.readLong();
        this.uid = in.readLong();
        this.name = in.readString();
    }
}
