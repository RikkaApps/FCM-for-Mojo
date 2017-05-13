package moe.shizuku.fcmformojo.interceptor;

import android.util.Base64;

import java.io.IOException;

import moe.shizuku.fcmformojo.FFMSettings;
import moe.shizuku.support.utils.Settings;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * OkHttp Interceptor for adding http basic authorization header
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */

public class HttpBasicAuthorizationInterceptor implements Interceptor {

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        String username = Settings.getString(FFMSettings.SERVER_HTTP_USERNAME, null);
        String password = Settings.getString(FFMSettings.SERVER_HTTP_PASSWORD, null);
        if (username != null && username.length() > 0
                || password != null && password.length() > 0) {
            String authorization = "Basic " + Base64.encodeToString((username + ':' + password).getBytes(), Base64.NO_WRAP);
            request = request.newBuilder()
                    .addHeader("Authorization", authorization)
                    .build();
        }
        return chain.proceed(request);
    }
}
