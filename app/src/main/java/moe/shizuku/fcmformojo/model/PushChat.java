package moe.shizuku.fcmformojo.model;

import android.os.Parcel;
import android.support.annotation.Keep;

/**
 * Created by Rikka on 2017/4/19.
 */

@Keep
public class PushChat extends Chat {

    private final Message message;

    protected PushChat(Parcel in) {
        super(in);
        message = Message.CREATOR.createFromParcel(in);
    }

    @Override
    public Message getLatestMessage() {
        return message;
    }
}
