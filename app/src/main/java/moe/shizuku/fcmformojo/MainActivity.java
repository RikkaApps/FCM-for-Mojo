package moe.shizuku.fcmformojo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.annotation.Nullable;
import android.text.Html;
import android.util.Log;
import android.widget.Toast;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClient.BillingResponse;
import com.android.billingclient.api.BillingClient.SkuType;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.crashlytics.android.Crashlytics;

import java.util.List;

import moe.shizuku.fcmformojo.compat.ShizukuCompat;
import moe.shizuku.fcmformojo.settings.MainSettingsFragment;
import moe.shizuku.fcmformojo.utils.ClipboardUtils;
import moe.shizuku.support.design.AboutDialogHelper;

public class MainActivity extends BaseActivity implements PurchasesUpdatedListener {

    private static final int REQUEST_CODE = 10000;

    private BillingClient mBillingClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new MainSettingsFragment())
                    .commit();
        }

        checkGoogleServiceFramework();
        requestPermission();
    }

    private void checkGoogleServiceFramework() {
        boolean ok = false;
        try {
            ok = getPackageManager().getApplicationInfo("com.google.android.gsf", 0).enabled;
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        if (!ok) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_no_google_title)
                    .setMessage(R.string.dialog_no_google_message)
                    .setPositiveButton(R.string.dialog_no_google_exit, (dialog, which) -> finish())
                    .setCancelable(false)
                    .show();
        }
    }

    private void requestPermission() {
        try {
            StorageManager sm = getSystemService(StorageManager.class);
            //noinspection ConstantConditions
            StorageVolume volume = sm.getPrimaryStorageVolume();
            Intent intent = volume.createAccessIntent(Environment.DIRECTORY_DOWNLOADS);
            startActivityForResult(intent, REQUEST_CODE);
        } catch (Exception e) {
            //Toast.makeText(this, R.string.cannot_request_permission, Toast.LENGTH_LONG).show();
            Toast.makeText(this, "Can't use Scoped Directory Access.\nFallback to runtime permission.", Toast.LENGTH_LONG).show();
            Log.wtf("FFM", "can't use Scoped Directory Access", e);

            Crashlytics.logException(e);

            // fallback to runtime permission
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                getContentResolver().takePersistableUriPermission(data.getData(),
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                FFMSettings.putDownloadUri(data.getData().toString());
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onOptionsItemSelected(int itemId) {
        switch (itemId) {
            case R.id.action_about:
                AboutDialogHelper.show(new AlertDialog.Builder(this),
                        getDrawable(R.mipmap.product_logo_fcmformojo_launcher_color_48),
                        getString(R.string.app_name),
                        BuildConfig.VERSION_NAME,
                        Html.fromHtml(getString(R.string.about_icon_credits), Html.FROM_HTML_MODE_COMPACT));
                return true;
            case R.id.action_donate:
                onDonateSelected();
                return true;
        }
        return super.onOptionsItemSelected(itemId);
    }

    private void onDonateSelected() {
        boolean isGooglePlay = "com.android.vending"
                .equals(getPackageManager().getInstallerPackageName(BuildConfig.APPLICATION_ID));

        boolean isAlipayInstalled = false;
        try {
            isAlipayInstalled = null != getPackageManager().getApplicationInfo("com.eg.android.AlipayGphone", PackageManager.MATCH_UNINSTALLED_PACKAGES);
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        if (!isGooglePlay && isAlipayInstalled) {
            showDonateAlipay();
        } else {
            showDonateGooglePlay();
        }
    }

    private void showDonateGooglePlay() {
        mBillingClient = BillingClient.newBuilder(this).setListener(this).build();

        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@BillingResponse int billingResponseCode) {
                if (billingResponseCode == BillingResponse.OK) {
                    // The billing client is ready. You can query purchases here.
                    BillingFlowParams flowParams = BillingFlowParams.newBuilder()
                            .setSku("donate_2")
                            .setType(SkuType.INAPP)
                            .build();
                    mBillingClient.launchBillingFlow(MainActivity.this, flowParams);
                }
            }
            @Override
            public void onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        });
    }

    private void showDonateAlipay() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_donate_title)
                .setMessage(R.string.dialog_donate_message)
                .setPositiveButton(R.string.dialog_donate_ok, (dialogInterface, i) -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(BuildConfig.DONATE_ALIPAY_URL));
                    ShizukuCompat.findAndStartActivity(MainActivity.this, intent, "com.eg.android.AlipayGphone");
                })
                .setNegativeButton(R.string.dialog_donate_no, (dialogInterface, i) -> Toast.makeText(MainActivity.this, "QAQ", Toast.LENGTH_SHORT).show())
                .setNeutralButton(R.string.dialog_donate_copy, (dialogInterface, i) -> ClipboardUtils.put(MainActivity.this, "rikka@xing.moe"))
                .show();
    }

    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
        if (responseCode == BillingResponse.OK
                && purchases != null) {
            for (Purchase purchase : purchases) {
                mBillingClient.consumeAsync(purchase.getPurchaseToken(), (responseCode1, purchaseToken) -> {
                });
            }
        }
    }
}
