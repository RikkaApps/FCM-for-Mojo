# FCM for Mojo
Push QQ Messages to Android devices with [Mojo-WebQQ](https://github.com/sjdy521/Mojo-Webqq)
Design for Android 7.0+ specially, full use Android notification feature (Reply in notification, etc...)

# Quick start
View official recommend [guidelines](http://www.coolapk.com/apk/com.swjtu.gcmformojo).

When you creat the configuration file, replace corresponding lines by this:
```perl
# ... #
$client->load(
'RikkaGCM',
   data => {
        api_url => 'https://fcm.googleapis.com/fcm/send',
        api_key => 'AAAABvjXwsM:APA91bF0X8YKcyTJcUdTLB1lc6Xb-03eIHCLy7PKHCwVYCL6XqEB7eS8o3i0amPOPi-R4i_ldlVtnPcYLtf4DwS4qgTi5Ra8Uyl9pGT02iJDE9Ovc-5dUoNSpgWUUZPn0KN2gJjeYLhO',
        registration_ids => ['<Input token from FCM for Mojo-WebQQ>']
        # ... #
    }
);
# ... #
```

> Quote from [Rikka's description in releases](https://github.com/RikkaW/FCM-for-Mojo/releases/v0.1.0)

Then, continue with guidelines.

# Add basic authenticate
For safe reason, use OKHTTP protect [Mojo-WebQQ](https://github.com/sjdy521/Mojo-Webqq) back end, see more in [haruue's pull request](https://github.com/RikkaW/FCM-for-Mojo/pull/4).

For more details about the Nginx configuration, see [Nginx official document](https://nginx.org/en/docs/http/ngx_http_auth_basic_module.html).

Generate a key with OpenSSL in Linux:

```bash
openssl passwd
Password:
Verifying - Password:
<Generated password>
```

Create a file to save your password (Here we suppose you save it in ```/etc/nginx```):
```<Your username>:<Generated password>```

Edit your Nginx configuration (Here we suppose your Mojo Web-QQ port is 5000):
```conf
server {
  listen 6000;
  listen [::]:6000; # IPv6

  location / {
    auth_basic <Any name here>;
    auth_basic_user_file /etc/nginx/passwd; # Password files
    proxy_pass http://127.0.0.1:5000; # Proxy Mojo
  }
}
```
