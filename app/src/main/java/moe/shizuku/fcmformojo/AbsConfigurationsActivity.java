package moe.shizuku.fcmformojo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.MenuItem;

/**
 * Created by rikka on 2017/8/28.
 */

public abstract class AbsConfigurationsActivity extends BaseActivity {

    public abstract void uploadConfigurations();

    public abstract boolean isConfigurationsChanged();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_upload:
                uploadConfigurations();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (isConfigurationsChanged()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.config_not_uploaded_title)
                    .setMessage(R.string.config_not_uploaded_message)
                    .setPositiveButton(R.string.config_not_uploaded_upload, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            uploadConfigurations();

                            finish();
                        }
                    })
                    .setNegativeButton(R.string.config_not_uploaded_exit, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .setNeutralButton(android.R.string.cancel, null)
                    .show();

            return;
        }

        super.onBackPressed();
    }
}
