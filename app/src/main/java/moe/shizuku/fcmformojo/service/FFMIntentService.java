package moe.shizuku.fcmformojo.service;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import moe.shizuku.fcmformojo.FFMApplication;
import moe.shizuku.fcmformojo.FFMSettings;
import moe.shizuku.fcmformojo.R;
import moe.shizuku.fcmformojo.api.OpenQQService;
import moe.shizuku.fcmformojo.model.Chat;
import moe.shizuku.fcmformojo.model.Chat.ChatType;
import moe.shizuku.fcmformojo.model.Friend;
import moe.shizuku.fcmformojo.model.Group;
import moe.shizuku.fcmformojo.model.SendResult;
import moe.shizuku.fcmformojo.notification.ChatIcon;
import moe.shizuku.fcmformojo.notification.NotificationBuilder;
import moe.shizuku.fcmformojo.profile.Profile;
import moe.shizuku.fcmformojo.receiver.NotificationReceiver;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

import static moe.shizuku.fcmformojo.FFMStatic.ACTION_DOWNLOAD_QRCODE;
import static moe.shizuku.fcmformojo.FFMStatic.ACTION_REPLY;
import static moe.shizuku.fcmformojo.FFMStatic.ACTION_UPDATE_ICON;
import static moe.shizuku.fcmformojo.FFMStatic.EXTRA_CHAT;
import static moe.shizuku.fcmformojo.FFMStatic.EXTRA_CONTENT;
import static moe.shizuku.fcmformojo.FFMStatic.EXTRA_URL;
import static moe.shizuku.fcmformojo.FFMStatic.NOTIFICATION_CHANNEL_PROGRESS;
import static moe.shizuku.fcmformojo.FFMStatic.NOTIFICATION_CHANNEL_SERVER;
import static moe.shizuku.fcmformojo.FFMStatic.NOTIFICATION_ID_PROGRESS;
import static moe.shizuku.fcmformojo.FFMStatic.NOTIFICATION_ID_SYSTEM;
import static moe.shizuku.fcmformojo.FFMStatic.REQUEST_CODE_OPEN_URI;
import static moe.shizuku.fcmformojo.FFMStatic.REQUEST_CODE_OPEN_SCAN;


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

    public static void startDownloadQrCode(Context context, String url) {
        Intent intent = new Intent(context, FFMIntentService.class);
        intent.setAction(ACTION_DOWNLOAD_QRCODE);
        intent.putExtra(EXTRA_URL, url);
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
        } else if (ACTION_DOWNLOAD_QRCODE.equals(action)) {
            String url = intent.getStringExtra(EXTRA_URL);
            handleDownloadQrCode(url);
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
            List<Friend> friends = retrofit.create(OpenQQService.class).getFriendsInfo().execute().body();
            List<Group> groups = retrofit.create(OpenQQService.class).getGroupsInfo().execute().body();

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
                boolean succeeded = save(client, url, file, true);

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
                boolean succeeded = save(client, url, file, true);

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

    private boolean save(OkHttpClient client, String url, File file, boolean round) {
        try {
            if (!file.exists()) {
                //noinspection ResultOfMethodCallIgnored
                file.getParentFile().mkdirs();
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            }
            OutputStream os = new FileOutputStream(file);

            return save(client, url, os, round);
        } catch (IOException e) {
            e.printStackTrace();

            return false;
        }
    }

    private boolean save(OkHttpClient client, String url, OutputStream os, boolean round) {
        try {
            Request request = new Request.Builder()
                    .get()
                    .url(url)
                    .build();
            okhttp3.Response headResponse = client.newCall(request).execute();

            Bitmap bitmap = BitmapFactory.decodeStream(headResponse.body().byteStream());

            if (round) {
                Bitmap roundBitmap = ChatIcon.clipToRound(this, bitmap);

                roundBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);

                bitmap.recycle();
                roundBitmap.recycle();
            } else {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                bitmap.recycle();
            }

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

        OpenQQService service = retrofit.create(OpenQQService.class);
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

    private void handleDownloadQrCode(String url) {
        Profile profile = FFMSettings.getProfile();
        NotificationManager notificationManager = getSystemService(NotificationManager.class);

        NotificationCompat.Builder builder = FFMApplication.get(this).getNotificationBuilder().createBuilder(this, null)
                .setChannelId(NOTIFICATION_CHANNEL_SERVER)
                .setColor(getColor(R.color.colorServerNotification))
                .setSmallIcon(R.drawable.ic_noti_download_24dp)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[0])
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true);

        NotificationCompat.Action action = null;

        Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(url));
        if (intent.resolveActivity(getPackageManager()) != null) {
            PendingIntent pendingIntent = PendingIntent.getActivity(this, REQUEST_CODE_OPEN_URI, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            action = new NotificationCompat.Action.Builder(R.drawable.ic_noti_open_24dp, getString(R.string.open_in_browser), pendingIntent)
                    .build();
        }

        OkHttpClient client = new OkHttpClient();

        try {
            DocumentFile pickedDir = FFMSettings.getDownloadDir(this);

            if (pickedDir != null) {
                DocumentFile newFile = pickedDir.findFile("webqq-qrcode.png");
                if (newFile != null) {
                    newFile.delete();
                }
                DocumentFile file = pickedDir.createFile("image/png", "webqq-qrcode");

                OutputStream os = getContentResolver().openOutputStream(file.getUri());

                if (save(client, url, os, false)) {
                    builder.setContentTitle(getString(R.string.notification_qr_code_downloaded))
                            .setContentText(getString(R.string.notification_tap_open_qq, getString(profile.getDisplayName())))
                            .setContentIntent(PendingIntent.getBroadcast(this, REQUEST_CODE_OPEN_SCAN, NotificationReceiver.openScanIntent(), 0));

                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                            .setData(Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "/FFM/webqq-qrcode.png"))));

                } else {
                    builder.setContentTitle(getString(R.string.notification_cannot_download))
                            .setContentText("可能是由于网络问题。")
                            .addAction(action);
                }
            } else {
                builder.setContentTitle(getString(R.string.notification_cannot_download))
                        .setContentText("可能是由于没有权限。")
                        .addAction(action);
            }
        } catch (Exception ignored) {
            builder.setContentTitle(getString(R.string.notification_cannot_download))
                    .setContentText("可能是由于没有权限。")
                    .addAction(action);
        }

        notificationManager.notify(NOTIFICATION_ID_SYSTEM, builder.build());
    }
}
