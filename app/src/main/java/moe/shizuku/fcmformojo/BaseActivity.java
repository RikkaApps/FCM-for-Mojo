package moe.shizuku.fcmformojo;

import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;

/**
 * Created by rikka on 2017/8/16.
 */

public class BaseActivity extends FragmentActivity {

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
