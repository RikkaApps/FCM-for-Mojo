# FCM for Mojo
借助 [Mojo-WebQQ](https://github.com/sjdy521/Mojo-Webqq) 实现将 QQ 消息通过 Firebase Cloud Messaging (FCM) 推送至 Android 设备。

专为 Android 7.0 以上设计，充分利用 Android 通知特性（直接回复，捆绑通知等）。

# 快速开始

参考官方推荐的[教程](http://www.coolapk.com/apk/com.swjtu.gcmformojo)

在创建配置文件的时候，稍作修改：找到配置文件中以下对应的几行，并用此处的配置替换掉

```perl
# ... #
$client->load(
'RikkaGCM',
   data => {
        api_url => 'https://fcm.googleapis.com/fcm/send',
        api_key => 'AAAABvjXwsM:APA91bF0X8YKcyTJcUdTLB1lc6Xb-03eIHCLy7PKHCwVYCL6XqEB7eS8o3i0amPOPi-R4i_ldlVtnPcYLtf4DwS4qgTi5Ra8Uyl9pGT02iJDE9Ovc-5dUoNSpgWUUZPn0KN2gJjeYLhO',
        registration_ids => ['输入你自己从 FCM for Mojo-WebQQ 中获取到的令牌']
        # ... #
    }
);
# ... #
```
> 引用自（[Rikka 在 releases 中的说明](https://github.com/RikkaW/FCM-for-Mojo/releases/v0.1.0)）

然后依照原教程继续进行即可

# [添加 HTTP 基本认证](https://github.com/RikkaW/FCM-for-Mojo/pull/4)

为了安全，我们用 OKHTTP 保护 [Mojo-WebQQ](https://github.com/sjdy521/Mojo-Webqq) 的后端，详见上述链接。
详细的 Nginx 配置可以查阅[官方文档](https://nginx.org/en/docs/http/ngx_http_auth_basic_module.html)

在 Linux 下，使用 OpenSSL 生成一个密码：

```bash
openssl passwd
Password:
Verifying - Password:
<生成的密码>
```

将密码复制下来，然后新建一个文件，这里我们假设你将密码文件放在 ```/etc/nginx/``` 下：

```
vi /etc/nginx/passwd
```

```
<用户名>:<生成的密码>
```

以下的 Nginx 配置假设你指定的端口为 5000

```conf
server {
  listen 6000;
  listen [::]:6000; # IPv6

  location / {
    auth_basic <随意取一个名字>;
    auth_basic_user_file /etc/nginx/passwd; # 密码文件
    proxy_pass http://127.0.0.1:5000; # 代理 mojo 端口
  }
}
```

然后在 FCM for Mojo Android 端上配置你的用户名和密码（你输入的密码，而不是生成的）即可
