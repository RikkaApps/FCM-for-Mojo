package moe.shizuku.fcmformojo.service;

import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.Map;

import moe.shizuku.fcmformojo.FFMApplication;
import moe.shizuku.fcmformojo.model.PushChat;
import moe.shizuku.fcmformojo.notification.NotificationBuilder;
import moe.shizuku.fcmformojo.utils.LocalBroadcast;

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
        String json = null;

        try {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            for (Map.Entry<String, String> e : data.entrySet()) {
                sb.append("\"").append(e.getKey()).append("\"");
                if (e.getValue().startsWith("{")) {
                    sb.append(":").append(e.getValue());

                } else {
                    sb.append(":").append("\"").append(e.getValue()).append("\"");
                }
                sb.append(",");
            }
            if (data.entrySet().size() > 0) {
                sb.deleteCharAt(sb.length() - 1);
            }
            sb.append("}");

            json = sb.toString();

            Log.d(TAG, json);

            PushChat chat = new Gson().fromJson(json, PushChat.class);
            if (chat != null) {
                mNotificationBuilder.addMessage(getApplicationContext(), chat);

                if (chat.isSystem()) {
                    LocalBroadcast.refreshStatus(this);
                }
            }
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "bad json: " + json, e);
        }
    }

    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);
    }
}
