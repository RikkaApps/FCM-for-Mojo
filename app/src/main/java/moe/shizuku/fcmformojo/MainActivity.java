package moe.shizuku.fcmformojo;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

import moe.shizuku.fcmformojo.api.WebQQService;
import moe.shizuku.fcmformojo.model.Friend;
import moe.shizuku.fcmformojo.utils.FileUtils;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends FragmentActivity {

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new SettingsFragment())
                    .commit();
        }
    }

    private synchronized void updateProgress() {
        if (mProgressDialog == null || !mProgressDialog.isShowing()) {
            return;
        }

        mProgressDialog.setProgress(mProgressDialog.getProgress() + 1);

        if (mProgressDialog.getProgress() >= mProgressDialog.getMax()) {
            mProgressDialog.dismiss();
        }
    }

    public void refreshHeads() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            return;
        }

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle(R.string.update_friend_face);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setProgress(0);
        mProgressDialog.setMax(1);
        mProgressDialog.setCancelable(false);
        mProgressDialog.show();

        final OkHttpClient client = new OkHttpClient();

        Retrofit retrofit = FFMApplication.get(this).getRetrofit();
        retrofit.create(WebQQService.class).getFriendsInfo().enqueue(new Callback<List<Friend>>() {
            @Override
            public void onResponse(Call<List<Friend>> call, Response<List<Friend>> response) {
                if (response.code() != 200) {
                    updateProgress();
                    return;
                }

                mProgressDialog.setMax(response.body().size());
                for (final Friend f : response.body()) {
                    refreshHead(client, f.getUid());
                }
            }

            @Override
            public void onFailure(Call<List<Friend>> call, Throwable t) {
                updateProgress();
            }
        });
    }

    private void refreshHead(final OkHttpClient client, final long uid) {
        Request request = new Request.Builder()
                .get()
                .url(String.format(Locale.ENGLISH, "http://ptlogin2.qq.com/getface?appid=1006102&uin=%d&imgtype=3", uid))
                .build();

        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                updateProgress();
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                if (response.code() != 200) {
                    updateProgress();
                    return;
                }

                String s = response.body().string().replace("\\", "");
                if (!s.contains("http")) {
                    updateProgress();
                    return;
                }
                s = s.substring(s.indexOf("http"), s.length() - "\"});".length());

                client.newCall(new Request.Builder().url(s).build()).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(okhttp3.Call call, IOException e) {
                        updateProgress();
                    }

                    @Override
                    public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                        InputStream is = response.body().byteStream();

                        Bitmap bitmap = BitmapFactory.decodeStream(is);
                        if (bitmap == null) {
                            updateProgress();
                            return;
                        }

                        File file = FileUtils.getCacheFile(getApplicationContext(), "/head/" + uid);
                        if (!file.exists()) {
                            //noinspection ResultOfMethodCallIgnored
                            file.getParentFile().mkdirs();
                            //noinspection ResultOfMethodCallIgnored
                            file.createNewFile();
                        }

                        OutputStream os = new FileOutputStream(file);

                        final RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                        drawable.setAntiAlias(true);
                        drawable.setCircular(true);
                        drawable.setBounds(0, 0, bitmap.getWidth(), bitmap.getHeight());

                        bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bitmap);
                        drawable.draw(canvas);

                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);

                        bitmap.recycle();

                        updateProgress();
                    }
                });
            }
        });
    }
}
