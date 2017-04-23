package moe.shizuku.fcmformojo.model;

import android.support.annotation.Keep;

/**
 * Created by Rikka on 2017/4/21.
 */

@Keep
public class SendResult {

    private String status;
    private int id;
    private int code;

    public String getStatus() {
        return status;
    }

    public int getId() {
        return id;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String toString() {
        return "SendResult{" +
                "status='" + status + '\'' +
                ", id=" + id +
                ", code=" + code +
                '}';
    }
}
