package moe.shizuku.fcmformojo.service;

import android.Manifest;
import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Single;
import moe.shizuku.fcmformojo.FFMApplication;
import moe.shizuku.fcmformojo.FFMSettings;
import moe.shizuku.fcmformojo.R;
import moe.shizuku.fcmformojo.model.Chat;
import moe.shizuku.fcmformojo.model.Chat.ChatType;
import moe.shizuku.fcmformojo.model.FFMResult;
import moe.shizuku.fcmformojo.model.Message;
import moe.shizuku.fcmformojo.model.openqq.Discuss;
import moe.shizuku.fcmformojo.model.openqq.User;
import moe.shizuku.fcmformojo.model.openqq.Group;
import moe.shizuku.fcmformojo.model.openqq.SendResult;
import moe.shizuku.fcmformojo.notification.UserIcon;
import moe.shizuku.fcmformojo.notification.NotificationBuilder;
import moe.shizuku.fcmformojo.profile.Profile;
import moe.shizuku.fcmformojo.profile.ProfileHelper;
import moe.shizuku.fcmformojo.receiver.FFMBroadcastReceiver;
import moe.shizuku.fcmformojo.utils.FileUtils;
import moe.shizuku.fcmformojo.utils.URLFormatUtils;
import moe.shizuku.support.utils.Settings;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;

import static moe.shizuku.fcmformojo.FFMApplication.FFMService;
import static moe.shizuku.fcmformojo.FFMApplication.OpenQQService;
import static moe.shizuku.fcmformojo.FFMStatic.ACTION_DOWNLOAD_QRCODE;
import static moe.shizuku.fcmformojo.FFMStatic.ACTION_REPLY;
import static moe.shizuku.fcmformojo.FFMStatic.ACTION_RESTART_WEBQQ;
import static moe.shizuku.fcmformojo.FFMStatic.ACTION_UPDATE_ICON;
import static moe.shizuku.fcmformojo.FFMStatic.EXTRA_ALL;
import static moe.shizuku.fcmformojo.FFMStatic.EXTRA_CHAT;
import static moe.shizuku.fcmformojo.FFMStatic.EXTRA_CONTENT;
import static moe.shizuku.fcmformojo.FFMStatic.FILE_PROVIDER_AUTHORITY;
import static moe.shizuku.fcmformojo.FFMStatic.NOTIFICATION_CHANNEL_PROGRESS;
import static moe.shizuku.fcmformojo.FFMStatic.NOTIFICATION_CHANNEL_SERVER;
import static moe.shizuku.fcmformojo.FFMStatic.NOTIFICATION_ID_PROGRESS;
import static moe.shizuku.fcmformojo.FFMStatic.NOTIFICATION_ID_SYSTEM;
import static moe.shizuku.fcmformojo.FFMStatic.REQUEST_CODE_COPY;
import static moe.shizuku.fcmformojo.FFMStatic.REQUEST_CODE_OPEN_SCAN;
import static moe.shizuku.fcmformojo.FFMStatic.REQUEST_CODE_OPEN_URI;
import static moe.shizuku.fcmformojo.FFMStatic.REQUEST_CODE_SEND;

public class FFMIntentService extends IntentService {

    private static final String TAG = "FFMIntentService";

    private static final String URL_UID = "{uid}";
    private static final String URL_HEAD_FRIEND = "https://q1.qlogo.cn/g?b=qq&s=100&nk={uid}";
    private static final String URL_HEAD_GROUP = "http://p.qlogo.cn/gh/{uid}/{uid}/100";

    public FFMIntentService() {
        super("FFMIntentService");
    }

    public static void startUpdateIcon(Context context, boolean all) {
        context.startService(new Intent(context, FFMIntentService.class)
                .setAction(ACTION_UPDATE_ICON)
                .putExtra(EXTRA_ALL, all));
    }

    public static void startReply(Context context, CharSequence content, Chat chat) {
        context.startService(new Intent(context, FFMIntentService.class)
                .setAction(ACTION_REPLY)
                .putExtra(EXTRA_CONTENT, content)
                .putExtra(EXTRA_CHAT, chat));
    }

    public static void startDownloadQrCode(Context context) {
        context.startService(new Intent(context, FFMIntentService.class)
                .setAction(ACTION_DOWNLOAD_QRCODE));
    }

    public static Intent restartIntent(Context context) {
        return new Intent(context, FFMIntentService.class).setAction(ACTION_RESTART_WEBQQ);
    }

    private NotificationManager mNm;

    @Override
    public void onCreate() {
        super.onCreate();

        mNm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null) {
            return;
        }
        final String action = intent.getAction();
        if (ACTION_UPDATE_ICON.equals(action)) {
            handleUpdateIcon(intent.getBooleanExtra(EXTRA_ALL, false));
        } else if (ACTION_REPLY.equals(action)) {
            CharSequence content = intent.getCharSequenceExtra(EXTRA_CONTENT);
            Chat chat = intent.getParcelableExtra(EXTRA_CHAT);
            handleReply(content, chat);
        } else if (ACTION_DOWNLOAD_QRCODE.equals(action)) {
            handleDownloadQrCode();
        } else if (ACTION_RESTART_WEBQQ.equals(action)) {
            handleRestart();
        }
    }

    private void handleRestart() {
        // TODO notify user if error
        FFMResult result = FFMService
                .restart()
                .blockingGet();

        mNm.cancel(NOTIFICATION_ID_SYSTEM);
    }

    private void handleUpdateIcon(boolean all) {
        long nextNotifyTime;

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_PROGRESS)
                .setColor(getColor(R.color.colorNotification))
                .setContentTitle(getString(R.string.notification_fetching_list))
                .setProgress(100, 0, true)
                .setOngoing(true)
                .setShowWhen(true)
                .setOnlyAlertOnce(true)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setWhen(System.currentTimeMillis());

        startForeground(NOTIFICATION_ID_PROGRESS, builder.build());

        List<User> users = null;
        List<Group> groups = null;
        List<Discuss> discusses = null;

        try {
            users = OpenQQService.getFriendsInfo().blockingGet();
            groups = Build.VERSION.SDK_INT >= 28 ? OpenQQService.getGroupsInfo().blockingGet() : OpenQQService.getGroupsBasicInfo().blockingGet();
            discusses = OpenQQService.getDiscussesInfo().blockingGet();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (users == null || groups == null || discusses == null) {
            stopForeground(true);
            return;
        }

        List<Pair<String, Integer>> uids = new ArrayList<>();

        for (User user : users) {
            if (user.getUid() == 0)
                continue;

            Pair<String, Integer> uid = new Pair<>(Long.toString(user.getUid()), ChatType.FRIEND);
            if (!uids.contains(uid))
                uids.add(uid);
        }

        for (Group group : groups) {
            if (group.getUid() != 0)
                uids.add(new Pair<>(Long.toString(group.getUid()), ChatType.GROUP));

            if (group.getMember() == null)
                continue;

            for (User user : group.getMember()) {
                if (user.getUid() == 0)
                    continue;

                Pair<String, Integer> uid = new Pair<>(Long.toString(user.getUid()), ChatType.FRIEND);
                if (!uids.contains(uid))
                    uids.add(uid);
            }
        }

        for (Discuss group : discusses) {
            if (group.getMember() == null)
                continue;

            for (User user : group.getMember()) {
                if (user.getUid() == 0)
                    continue;

                Pair<String, Integer> uid = new Pair<>(Long.toString(user.getUid()), ChatType.FRIEND);
                if (!uids.contains(uid))
                    uids.add(uid);
            }
        }

        if (!all) {
            for (Pair<String, Integer> uid : new ArrayList<>(uids)) {
                File file = UserIcon.getIconFile(this, Long.valueOf(uid.first), uid.second);
                if (file.exists())
                    uids.remove(uid);
            }
        }

        int count = uids.size();

        builder.setContentTitle(getString(R.string.notification_fetching));
        builder.setContentText(getString(R.string.notification_fetching_progress, 0, count));
        builder.setProgress(count, 0, false);

        mNm.notify(NOTIFICATION_ID_PROGRESS, builder.build());
        nextNotifyTime = System.currentTimeMillis() + 1000;

        OkHttpClient client = new OkHttpClient();

        int current = 0;
        for (Pair<String, Integer> uid : uids) {
            current++;

            builder.setContentText(getString(R.string.notification_fetching_progress, current, count));
            builder.setProgress(count, current, false);

            if (System.currentTimeMillis() > nextNotifyTime || count == current) {
                mNm.notify(NOTIFICATION_ID_PROGRESS, builder.build());
                nextNotifyTime = System.currentTimeMillis() + 1000;
            }

            File file = UserIcon.getIconFile(this, Long.valueOf(uid.first), uid.second);
            String url = uid.second == ChatType.FRIEND
                    ? URL_HEAD_FRIEND.replace(URL_UID, uid.first)
                    : URL_HEAD_GROUP.replace(URL_UID, uid.first);

            try {
                downloadAvatarImageUrl(client, url, file);
                Log.d(TAG, "" + uid.first);
            } catch (IOException e) {
                Log.w(TAG, "" + uid.first, e);
            }
        }

        stopForeground(true);
    }

    private boolean downloadAvatarImageUrl(OkHttpClient client, String url, File file) throws IOException {
        if (file.exists()) {
            return true;
        }

        if (!file.exists()) {
            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
            //noinspection ResultOfMethodCallIgnored
            file.createNewFile();
        }
        OutputStream os = new FileOutputStream(file);

        return downloadImageUrl(client, url, os, true);
    }

    private boolean downloadImageUrl(OkHttpClient client, String url, OutputStream os, boolean round) throws IOException {
        Request request = new Request.Builder()
                .get()
                .url(url)
                .build();
        okhttp3.Response headResponse = client.newCall(request).execute();
        ResponseBody body = headResponse.body();
        if (body == null) {
            return false;
        }

        Bitmap bitmap = BitmapFactory.decodeStream(body.byteStream());
        if (bitmap == null) {
            return false;
        }

        if (round) {
            Bitmap roundBitmap = UserIcon.clipToRound(this, bitmap);

            roundBitmap.compress(Bitmap.CompressFormat.PNG, 100, os);

            bitmap.recycle();
            roundBitmap.recycle();
        } else {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
            bitmap.recycle();
        }

        return true;
    }

    private void handleReply(CharSequence content, Chat chat) {
        final long id = chat.getId();
        int type = chat.getType();

        if (content == null || id == 0) {
            return;
        }

        Log.d("Reply", "try reply to " + id + " " + content.toString());

        Single<SendResult> call;
        switch (type) {
            case ChatType.FRIEND:
                call = OpenQQService.sendFriendMessage(id, content.toString());
                break;
            case ChatType.GROUP:
                call = OpenQQService.sendGroupMessage(id, content.toString());
                break;
            case ChatType.DISCUSS:
                call = OpenQQService.sendDiscussMessage(id, content.toString());
                break;
            case ChatType.SYSTEM:
            default:
                return;
        }

        try {
            final SendResult result = call.blockingGet();

            if (result != null) {
                if (result.getCode() == 0) {
                    NotificationBuilder nb = FFMApplication.get(this).getNotificationBuilder();
                    nb.addMessage(this, chat.getUniqueId(), Message.createSelfMessage(content.toString(), System.currentTimeMillis() / 1000));
                    return;
                }

                FFMApplication.get(this).runInMainThread(() -> Toast.makeText(FFMIntentService.this,
                        result.getStatus(), Toast.LENGTH_LONG).show());
            }
        } catch (final Throwable t) {
            t.printStackTrace();

            FFMApplication.get(this).runInMainThread(() -> Toast.makeText(FFMIntentService.this, t.getMessage(), Toast.LENGTH_LONG).show());
        }

        NotificationBuilder nb = FFMApplication.get(this).getNotificationBuilder();
        nb.clearMessages(chat.getUniqueId());
    }

    private void handleDownloadQrCode() {
        String url = URLFormatUtils.addEndSlash(FFMSettings.getBaseUrl()) + "ffm/get_qr_code";
        Profile profile = FFMSettings.getProfile();

        NotificationCompat.Builder builder = FFMApplication.get(this).getNotificationBuilder().createBuilder(this, null)
                .setChannelId(NOTIFICATION_CHANNEL_SERVER)
                .setColor(getColor(R.color.colorServerNotification))
                .setSmallIcon(R.drawable.ic_noti_download_24dp)
                .setAutoCancel(true)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setVibrate(new long[0]);
        }

        NotificationCompat.Action fallbackAction;

        Uri authUri = Uri.parse(url);

        String username = Settings.getString(FFMSettings.SERVER_HTTP_USERNAME, null);
        String password = Settings.getString(FFMSettings.SERVER_HTTP_PASSWORD, null);

        if (username != null && username.length() > 0
                || password != null && password.length() > 0) {
            authUri = authUri.buildUpon().encodedAuthority(String.format("%1$s:%2$s@%3$s", username, password, authUri.getAuthority())).build();
        }

        Intent intent = new Intent(Intent.ACTION_VIEW).setData(authUri);
        PendingIntent viewIntent = PendingIntent
                .getActivity(this, REQUEST_CODE_OPEN_URI, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent copyIntent = PendingIntent
                .getBroadcast(this, REQUEST_CODE_COPY, FFMBroadcastReceiver.copyToClipboardIntent(authUri.toString()), PendingIntent.FLAG_UPDATE_CURRENT);

        if (intent.resolveActivity(getPackageManager()) != null) {
            fallbackAction = new NotificationCompat.Action.Builder(R.drawable.ic_noti_open_24dp, getString(R.string.notification_action_open_in_browser), viewIntent)
                    .build();
        } else {
            // 这个人没有浏览器..
            fallbackAction = new NotificationCompat.Action.Builder(R.drawable.ic_noti_copy_24dp, getString(R.string.notification_action_copy_to_clipboard), copyIntent)
                    .build();
        }

        boolean installed = ProfileHelper.installed(this, profile);
        OutputStream os = null;
        Uri uri = null;
        try {
            // 当前用户没有安装选择的 QQ 时下载到私有位置
            if (!installed) {
                File file = FileUtils.getInternalCacheFile(this, "webqq-qrcode.png");
                if (file.exists() || file.createNewFile()) {
                    uri = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, file);
                    os = new FileOutputStream(file);
                }
            } else {
                DocumentFile pickedDir = FFMSettings.getDownloadDir(this);

                if (pickedDir != null) {
                    DocumentFile newFile = pickedDir.findFile("webqq-qrcode.png");
                    if (newFile != null) {
                        newFile.delete();
                    }
                    DocumentFile file = pickedDir.createFile("image/png", "webqq-qrcode");

                    //noinspection ConstantConditions
                    uri = file.getUri();
                    os = getContentResolver().openOutputStream(uri);
                } else {
                    // 退回到运行时权限的情况
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        File file = FileUtils.getExternalStoragePublicFile(Environment.DIRECTORY_DOWNLOADS, "FFM", "webqq-qrcode.png");
                        if (file.exists() || file.createNewFile()) {
                            uri = FileProvider.getUriForFile(this, FILE_PROVIDER_AUTHORITY, file);
                            os = new FileOutputStream(file);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }


        if (os == null) {
            builder.setContentTitle(getString(R.string.notification_cannot_download))
                    .setContentText(getString(R.string.notification_permission_issue))
                    .addAction(fallbackAction);
        } else {
            OkHttpClient client = FFMApplication.getOkHttpClient();

            boolean res;
            try {
                res = downloadImageUrl(client, url, os, false);
            } catch (IOException e) {
                Log.w(TAG, e);

                builder.setContentTitle(getString(R.string.notification_cannot_download))
                        .setContentText(getString(R.string.notification_network_issue, e.getCause()))
                        .addAction(fallbackAction);

                mNm.notify(NOTIFICATION_ID_SYSTEM, builder.build());

                return;
            }

            if (res) {
                builder.setContentTitle(getString(R.string.notification_qr_code_downloaded))
                        .setContentText(getString(R.string.notification_tap_open_qq, getString(profile.getDisplayName())))
                        .setContentIntent(PendingIntent.getBroadcast(this, REQUEST_CODE_OPEN_SCAN, FFMBroadcastReceiver.openScanIntent(), PendingIntent.FLAG_UPDATE_CURRENT));

                // 当前用户没有安装选择的 QQ 时增加发送 action 和浏览器打开 action
                if (!installed) {
                    Intent sendIntent = new Intent(Intent.ACTION_SEND)
                            .putExtra(Intent.EXTRA_STREAM, uri)
                            .setType("image/*");
                    sendIntent = Intent.createChooser(sendIntent, getString(R.string.dialog_title_send_via));
                    if (sendIntent.resolveActivity(getPackageManager()) != null) {
                        NotificationCompat.Action sendAction = new NotificationCompat.Action.Builder(
                                R.drawable.ic_noti_send_24dp,
                                getString(R.string.notification_action_send),
                                PendingIntent.getActivity(this, REQUEST_CODE_SEND, sendIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                                .build();
                        builder.addAction(sendAction);
                        builder.addAction(fallbackAction);
                    }
                } else {
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                            .setData(Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "/FFM/webqq-qrcode.png"))));
                }
            } else {
                builder.setContentTitle(getString(R.string.notification_cannot_download))
                        .setContentText(getString(R.string.notification_network_issue, ""))
                        .addAction(fallbackAction);
            }
        }

        mNm.notify(NOTIFICATION_ID_SYSTEM, builder.build());
    }
}
