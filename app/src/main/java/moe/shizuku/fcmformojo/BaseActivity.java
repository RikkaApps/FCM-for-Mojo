package moe.shizuku.fcmformojo;

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
            FontRequests.DEFAULT_SERIF_FONTS = new FontRequest[]{FontRequest.DEFAULT, FontRequest.NOTO_COLOR_EMOJI};

            FontProviderClient client = FontProviderClient.create(this);
            if (client != null) {
                client.replace(FontRequests.DEFAULT_SERIF_FONTS, "Noto Sans CJK",
                        "sans-serif", "sans-serif-medium");
            }

            sFontProviderInitialized = true;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (onOptionsItemSelected(item.getItemId())) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onOptionsItemSelected(int itemId) {
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return false;
        }
    }
}
