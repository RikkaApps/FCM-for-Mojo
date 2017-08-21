package moe.shizuku.fcmformojo;

import android.app.Activity;
import android.view.MenuItem;

/**
 * Created by rikka on 2017/8/16.
 */

public class BaseActivity extends Activity {

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
