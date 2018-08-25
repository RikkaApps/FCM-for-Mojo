package moe.shizuku.fcmformojo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

import moe.shizuku.fcmformojo.compat.ShizukuCompat;
import moe.shizuku.fcmformojo.utils.ClipboardUtils;

public class Donate {

    public static void showDonate(Context context) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.dialog_donate_title)
                .setMessage(R.string.dialog_donate_message)
                .setPositiveButton(R.string.dialog_donate_ok, (dialogInterface, i) -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW,
                            Uri.parse(BuildConfig.DONATE_ALIPAY_URL));
                    ShizukuCompat.findAndStartActivity(context, intent, "com.eg.android.AlipayGphone");
                })
                .setNegativeButton(R.string.dialog_donate_no, (dialogInterface, i) -> Toast.makeText(context, "QAQ", Toast.LENGTH_SHORT).show())
                .setNeutralButton(R.string.dialog_donate_copy, (dialogInterface, i) -> ClipboardUtils.put(context, "rikka@xing.moe"))
                .show();
    }
}
