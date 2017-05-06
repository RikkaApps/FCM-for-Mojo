# FCM for Mojo
借助 [Mojo-WebQQ](https://github.com/sjdy521/Mojo-Webqq) 实现将 QQ 消息通过 Firebase Cloud Messaging (FCM) 推送至 Android 设备。

专为 Android 7.0 以上设计，充分利用 Android 通知特性（直接回复，捆绑通知等）。

# 开始搭建
## Linux
首先，参考官方推荐的 [GCM-for-Mojo 搭建教程](https://gist.github.com/kotomei/5367a003cd16d05e075c21a7f360b09a) 

到 **“3.设定”** 时，稍作修改（[Rikka 在 releases 中的说明](https://github.com/RikkaW/FCM-for-Mojo/releases/v0.1.0)）
```perl
# 省略引入模块部分
# 以下为 GCM 推送
$client->load(
'RikkaGCM',
   data => {
        api_url => 'https://fcm.googleapis.com/fcm/send',
        api_key => 'AAAABvjXwsM:APA91bF0X8YKcyTJcUdTLB1lc6Xb-03eIHCLy7PKHCwVYCL6XqEB7eS8o3i0amPOPi-R4i_ldlVtnPcYLtf4DwS4qgTi5Ra8Uyl9pGT02iJDE9Ovc-5dUoNSpgWUUZPn0KN2gJjeYLhO',
        registration_ids => ['输入你自己从 FCM for Mojo-WebQQ 中获取到的令牌']
        # 省略群组忽略规则
    }
);
# 省略端口和回复功能配置
```

最后，回到教程第四步即可
