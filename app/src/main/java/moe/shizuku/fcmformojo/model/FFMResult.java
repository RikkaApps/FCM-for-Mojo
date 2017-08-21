package moe.shizuku.fcmformojo.model;

import android.support.annotation.Keep;

/**
 * Created by rikka on 2017/8/15.
 */

@Keep
public class FFMResult {

    private int code;
    private String message;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
