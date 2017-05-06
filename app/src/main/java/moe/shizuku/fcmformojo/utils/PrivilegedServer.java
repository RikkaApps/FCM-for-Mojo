package moe.shizuku.fcmformojo.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import moe.shizuku.fcmformojo.FFMApplication;
import moe.shizuku.privileged.api.IServerInterface;

/**
 * Created by Rikka on 2017/4/28.
 */

public class PrivilegedServer {

    private static class RunningTasksServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            PrivilegedServer.service = IServerInterface.Stub.asInterface(service);

            Log.i("PrivilegedServer", "PrivilegedServer bind " + (service != null));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            service = null;
        }
    }

    public static RunningTasksServiceConnection sServiceConnection = new RunningTasksServiceConnection();
    public static IServerInterface service;

    public static void bind(final Context context) {
        if (service != null) {
            return;
        }

        Intent intent = new Intent("moe.shizuku.privileged.api.BIND");
        intent.setPackage("moe.shizuku.privileged.api");
        if (!context.getPackageManager().queryIntentServices(intent, 0).isEmpty()) {
            try {
                Log.i("PrivilegedServer", "PrivilegedServer try bind");
                context.bindService(intent, PrivilegedServer.sServiceConnection, Context.BIND_AUTO_CREATE);
            } catch (final Exception e) {
                e.printStackTrace();

                FFMApplication.get(context).runInMainTheard(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, "error when bind system plugin service:\n" + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        } else {
            Log.i("PrivilegedServer", "PrivilegedServer not exist");
        }
    }
}
