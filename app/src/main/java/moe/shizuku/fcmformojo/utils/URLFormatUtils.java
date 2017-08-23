package moe.shizuku.fcmformojo.utils;

import android.support.annotation.CheckResult;

import okhttp3.HttpUrl;

/**
 * Utils to check whether a URL is in a valid format for mojo-qq endpoint
 * @author Haruue Icymoon haruue@caoyue.com.cn
 */

public class URLFormatUtils {

    public static boolean isValidURL(String url) {
        return HttpUrl.parse(url) != null;
    }

    @CheckResult
    public static String addEndSlash(String url) {
        if (!url.endsWith("/")) {
            url += "/";
        }
        return url;
    }

}
