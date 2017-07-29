package moe.shizuku.fcmformojo.service;

import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.json.JSONObject;

import java.util.Map;

import moe.shizuku.fcmformojo.BuildConfig;
import moe.shizuku.fcmformojo.FFMApplication;
import moe.shizuku.fcmformojo.FFMSettings;
import moe.shizuku.fcmformojo.model.PushMessage;
import moe.shizuku.fcmformojo.notification.NotificationBuilder;

/**
 * Created by Rikka on 2017/4/19.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    private static final String TAG = "MessagingService";

    private NotificationBuilder mNotificationBuilder;

    @Override
    public void onCreate() {
        super.onCreate();

        mNotificationBuilder = ((FFMApplication) getApplication()).getNotificationBuilder();
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();

        String foreground = null;
        try {
            foreground = FFMApplication.get(this).getForegroundPackage();
        } catch (Exception ignored) {
        }

        if (foreground != null
                && foreground.equals(FFMSettings.getQQPackageName())) {
            mNotificationBuilder.clearMessages();
            return;
        }

        try {
            PushMessage message = new Gson().fromJson(new JSONObject(data).toString(), PushMessage.class);
            if (message != null) {
                Log.d(TAG, message.toString());

                mNotificationBuilder.addMessage(getApplicationContext(), message);
            }
        } catch (JsonSyntaxException e) {
            e.printStackTrace();

            Log.e(TAG, "bad json: " + new JSONObject(data).toString());
        }
    }
}
