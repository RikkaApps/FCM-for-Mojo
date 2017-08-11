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
import android.support.v4.app.RemoteInput;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import moe.shizuku.fcmformojo.FFMApplication;
import moe.shizuku.fcmformojo.R;
import moe.shizuku.fcmformojo.api.WebQQService;
import moe.shizuku.fcmformojo.model.Chat;
import moe.shizuku.fcmformojo.model.Chat.ChatType;
import moe.shizuku.fcmformojo.model.Friend;
import moe.shizuku.fcmformojo.model.Group;
import moe.shizuku.fcmformojo.model.SendResult;
import moe.shizuku.fcmformojo.notification.ChatIcon;
import moe.shizuku.fcmformojo.notification.NotificationBuilder;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static moe.shizuku.fcmformojo.FFMStatic.ACTION_REPLY;
import static moe.shizuku.fcmformojo.FFMStatic.ACTION_UPDATE_ICON;
import static moe.shizuku.fcmformojo.FFMStatic.EXTRA_CHAT;
import static moe.shizuku.fcmformojo.FFMStatic.EXTRA_CONTENT;
import static moe.shizuku.fcmformojo.FFMStatic.NOTIFICATION_CHANNEL_PROGRESS;
import static moe.shizuku.fcmformojo.FFMStatic.NOTIFICATION_ID_PROGRESS;
import static moe.shizuku.fcmformojo.FFMStatic.NOTIFICATION_INPUT_KEY;


public class FFMIntentService extends IntentService {

    private static final String TAG = "FFMIntentService";

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

    public static void startReply(Context context, CharSequence content, Chat chat) {
        Intent intent = new Intent(context, FFMIntentService.class);
        intent.setAction(ACTION_REPLY);
        intent.putExtra(EXTRA_CONTENT, content);
        intent.putExtra(EXTRA_CHAT, chat);
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
        } else if (ACTION_REPLY.equals(action)) {
            CharSequence content = intent.getCharSequenceExtra(EXTRA_CONTENT);
            Chat chat = intent.getParcelableExtra(EXTRA_CHAT);
            handleReply(content, chat);
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

            if (friends == null || groups == null) {
                notificationManager.cancel(NOTIFICATION_ID_PROGRESS);
                return;
            }
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

    private void handleReply(CharSequence content, Chat chat) {
        final long id = chat.getId();
        int type = chat.getType();

        if (content == null || id == 0) {
            return;
        }

        Log.d("Reply", "try reply to " + id + " " + content.toString());

        Retrofit retrofit = FFMApplication.getRetrofit(this);

        WebQQService service = retrofit.create(WebQQService.class);
        Call<SendResult> call;
        switch (type) {
            case ChatType.FRIEND:
                call = service.sendFriendMessage(id, content.toString());
                break;
            case ChatType.GROUP:
                call = service.sendGroupMessage(id, content.toString());
                break;
            case ChatType.DISCUSS:
                call = service.sendDiscussMessage(id, content.toString());
                break;
            case ChatType.SYSTEM:
            default:
                return;
        }

        try {
            Response<SendResult> response = call.execute();
            if (response.isSuccessful()) {
                final SendResult result = response.body();

                if (response.body().getCode() != 0) {
                    FFMApplication.get(this).runInMainThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(FFMIntentService.this,
                                    result.getStatus(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        } catch (final Throwable t) {
            t.printStackTrace();

            FFMApplication.get(this).runInMainThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(FFMIntentService.this, t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        }

        NotificationBuilder nb = FFMApplication.get(this).getNotificationBuilder();
        nb.clearMessages(id);
    }
}
