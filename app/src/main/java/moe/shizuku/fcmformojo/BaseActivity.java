package moe.shizuku.fcmformojo;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import java.io.File;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import moe.shizuku.fontprovider.FontProviderClient;
import moe.shizuku.fontprovider.FontRequest;
import moe.shizuku.fontprovider.FontRequests;

/**
 * Created by rikka on 2017/8/16.
 */

public abstract class BaseActivity extends FragmentActivity {

    private static boolean sFontInitialized = false;

    private static void initializeFont(Context context) {
        if (sFontInitialized || Build.VERSION.SDK_INT < 28) {
            return;
        }
        sFontInitialized = true;

        try {
            if (!new File("/system/fonts/NotoSansCJK-Medium.ttc").exists()
                    && !new File("/system/fonts/NotoSansCJKsc-Medium.ttf").exists()
                    && !new File("/system/fonts/NotoSansCJKsc-Medium.otf").exists()) {
                FontRequests.DEFAULT_SERIF_FONTS = new FontRequest[]{FontRequest.DEFAULT, FontRequest.NOTO_COLOR_EMOJI};

                FontProviderClient client = FontProviderClient.create(context);
                if (client != null) {
                    client.replace(FontRequests.DEFAULT_SERIF_FONTS, "Noto Sans CJK",
                            "sans-serif", "sans-serif-medium");
                }
            }
        } catch (Throwable tr) {
            tr.printStackTrace();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        initializeFont(this);

        super.onCreate(savedInstanceState);
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
