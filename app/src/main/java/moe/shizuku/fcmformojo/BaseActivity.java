package moe.shizuku.fcmformojo;

import android.content.ServiceConnection;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

import moe.shizuku.fontprovider.FontProviderClient;
import moe.shizuku.fontprovider.FontRequest;
import moe.shizuku.fontprovider.FontRequests;

/**
 * Created by rikka on 2017/8/16.
 */

public class BaseActivity extends FragmentActivity {

    private static boolean sFontProviderInitialized = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!sFontProviderInitialized) {
            // 替换默认 emoji 字体
            FontRequests.setDefaultSansSerifFonts(FontRequest.DEFAULT, FontRequest.NOTO_COLOR_EMOJI);

            // 创建 FontProviderClient
            FontProviderClient.create(this, new FontProviderClient.Callback() {
                @Override
                public boolean onServiceConnected(FontProviderClient client, ServiceConnection serviceConnection) {
                    client.replace("sans-serif", "Noto Sans CJK");
                    client.replace("sans-serif-medium", "Noto Sans CJK");
                    return true;
                }
            });

            sFontProviderInitialized = true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}
