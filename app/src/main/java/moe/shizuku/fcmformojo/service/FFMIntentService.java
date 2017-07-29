package moe.shizuku.fcmformojo.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import moe.shizuku.fcmformojo.FFMApplication;
import moe.shizuku.fcmformojo.R;
import moe.shizuku.fcmformojo.api.WebQQService;
import moe.shizuku.fcmformojo.model.Chat.ChatType;
import moe.shizuku.fcmformojo.model.Friend;
import moe.shizuku.fcmformojo.model.Group;
import moe.shizuku.fcmformojo.notification.ChatIcon;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;

import static moe.shizuku.fcmformojo.FFMStatic.NOTIFICATION_CHANNEL_PROGRESS;
import static moe.shizuku.fcmformojo.FFMStatic.NOTIFICATION_ID_PROGRESS;


public class FFMIntentService extends IntentService {

    private static final String TAG = "FFMIntentService";

    private static final String ACTION_UPDATE_ICON = "moe.shizuku.fcmformojo.service.action.UPDATE_ICON";

    private static final String URL_UID = "{uid}";
    private static final String URL_HEAD_FRIEND = "https://q1.qlogo.cn/g?b=qq&s=100&nk={uid}";
    private static final String URL_HEAD_GROUP = "http://p.qlogo.cn/gh/{uid}/{uid}/100";

    public FFMIntentService() {
        super("FFMIntentService");
    }

    public static void startUpdateIcon(Context context, @Nullable ResultReceiver receiver) {
        Intent intent = new Intent(context, FFMIntentService.class);
        intent.setAction(ACTION_UPDATE_ICON);
        intent.putExtra(Intent.EXTRA_RESULT_RECEIVER, receiver);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        final String action = intent.getAction();
        if (ACTION_UPDATE_ICON.equals(action)) {
            ResultReceiver receiver = intent.getParcelableExtra(Intent.EXTRA_RESULT_RECEIVER);
            handleUpdateIcon(receiver);
        }
    }

    private void handleUpdateIcon(ResultReceiver receiver) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_PROGRESS)
                .setColor(getColor(R.color.colorNotification))
                .setContentTitle(getString(R.string.notification_fetching_list))
                .setProgress(100, 0, true)
                .setOngoing(true)
                .setShowWhen(true)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setWhen(System.currentTimeMillis());

        notificationManager.notify(NOTIFICATION_ID_PROGRESS, builder.build());

        Retrofit retrofit = FFMApplication.getRetrofit(this);
        OkHttpClient client = new OkHttpClient();

        try {
            List<Friend> friends = retrofit.create(WebQQService.class).getFriendsInfo().execute().body();
            List<Group> groups = retrofit.create(WebQQService.class).getGroupsInfo().execute().body();

            int count = friends.size() + groups.size();

            Bundle result = null;
            if (receiver != null) {
                result = new Bundle();
                result.putInt("total", friends.size() + groups.size());
                result.putInt("current", 0);

                receiver.send(0, result);
            }

            builder.setContentTitle(getString(R.string.notification_fetching));
            builder.setContentText(getString(R.string.notification_fetching_progress, 0, count));
            builder.setProgress(count, 0, false);
            notificationManager.notify(NOTIFICATION_ID_PROGRESS, builder.build());

            int current = 0;
            for (Friend friend : friends) {
                current++;

                if (receiver != null) {
                    result.putInt("current", current);
                    receiver.send(0, result);
                }

                builder.setContentText(getString(R.string.notification_fetching_progress, current, count));
                builder.setProgress(count, current, false);
                notificationManager.notify(NOTIFICATION_ID_PROGRESS, builder.build());

                long uid = friend.getUid();
                if (uid == 0) {
                    continue;
                }

                File file = ChatIcon.getIconFile(this, uid, ChatType.FRIEND);
                String url = URL_HEAD_FRIEND.replace(URL_UID, Long.toString(uid));
                boolean succeeded = save(client, url, file);

                Log.d(TAG, succeeded + " friend " + uid);
            }

            for (Group group : groups) {
                current++;

                if (receiver != null) {
                    result.putInt("current", current);
                    receiver.send(0, result);
                }

                builder.setContentText(getString(R.string.notification_fetching_progress, current, count));
                builder.setProgress(count, current, false);
                notificationManager.notify(NOTIFICATION_ID_PROGRESS, builder.build());

                long uid = group.getUid();
                if (uid == 0) {
                    continue;
                }

                File file = ChatIcon.getIconFile(this, uid, ChatType.GROUP);
                String url = URL_HEAD_GROUP.replace(URL_UID, Long.toString(uid));
                boolean succeeded = save(client, url, file);

                Log.d(TAG, succeeded + " group " + uid);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (receiver != null) {
            receiver.send(0, null);
        }

        notificationManager.cancel(NOTIFICATION_ID_PROGRESS);
    }

    private boolean save(OkHttpClient client, String url, File file) {
        try {
            Request request = new Request.Builder()
                    .get()
                    .url(url)
                    .build();
            okhttp3.Response headResponse = client.newCall(request).execute();

            Bitmap bitmap = BitmapFactory.decodeStream(headResponse.body().byteStream());

            if (!file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.getParentFile().mkdirs();
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            }
            OutputStream os = new FileOutputStream(file);

            Bitmap roundBitmap = ChatIcon.clipToRound(this, bitmap);

            roundBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);

            bitmap.recycle();
            roundBitmap.recycle();

            return true;
        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }
    }
}
