# FCM for Mojo
借助 [Mojo-Webqq](https://github.com/sjdy521/Mojo-Webqq) 实现将 QQ 消息通过 Firebase Cloud Messaging (FCM) 推送至 Android 设备。

## 特点
* 专为 Android 7.0 以上设计，充分利用 Android 通知特性（直接回复，捆绑通知等）。
* 不同于大部分应用接收推送后只在客户端决定是否通知，FFM 服务端可以根据客户端配置决定是否推送，避免非必要唤醒，花费更少电量。
* 支持戳通知直接进入对应聊天（仅 QQ、TIM、QQi、QQL，且可能需要 root 运行的 shizuku）。

## 配置方法

### 服务端

有两种方式可以部署，只要选其中一种即可，推荐稍微麻烦一些但什么都是自己控制的自行配制的方式。

#### 选项 1：Docker（不推荐）
通过 Docker 方式安装 FFM 十分简单.按照 [kotomei 的教程](https://github.com/kotomei/fcm-for-mojo/blob/master/README.md)，你只需几分钟就可以搞定。

#### 选项 2：自行配置（推荐）

##### 依赖

Mojo-Webqq：直接根据[官方教程](https://github.com/sjdy521/Mojo-Webqq#安装方法)即可

Node.js：自己编译安装，或者直接[使用包管理器](https://nodejs.org/en/download/package-manager)

##### 下载（或更新）服务端

需要自行把 <server.zip> 替换为 [latest release](https://github.com/RikkaW/FCM-for-Mojo/releases/latest) 中的 server.zip 的地址

```Shell
mkdir ffm && cd ffm
wget <server.zip>
unzip server.zip && cd node
npm install && cd ..
```

##### 运行

为避免错过二维码扫描通知而不知所措，建议在运行前先完成客户端配置的一部分（填写好服务器 URL）。

```Shell
node node/index.js
```

#### 安全性（可选）

##### HTTP 基本认证

HTTP 基本认证通过 [http-auth 模块](https://github.com/http-auth/http-auth) 实现，在[这里](https://github.com/http-auth/http-auth#configurations)可以看到所有可用选项，下文只说明最简单的配置方法。

创建一个每行的内容是 **用户名:密码** 或是 **用户名:密码 hash** 的文件。

密码 hash 可以用如下的方式生成：

```Shell
$ openssl passwd
Password:
Verifying - Password:
<这里就是密码 hash>
```

编辑 ```config.js```，找到有 ```basic_auth``` 那几行并去掉附近的注释（即 ```/*``` 和 ```*/```）：
```js
	"basic_auth": {
		"file": "<密码文件路径>"
	},
```

##### HTTPS

HTTPS 通过 [https 模块](https://nodejs.org/dist/latest/docs/api/https.html) 实现，在[这里](https://nodejs.org/dist/latest/docs/api/tls.html#tls_tls_createsecurecontext_options)可以看到所有可用选项，下文只说明最简单的配置方法。

编辑 ```config.js```，找到有 ```https``` 的那几行并去掉附近的注释（即 ```/*``` 和 ```*/```）：
```js
	"https": {
		"key": fs.readFileSync("<证书私钥路径>"),
		/* 如果你有 CA 证书，就加上这行
		"ca": fs.readFileSync("<CA 证书路径>"), */
		"cert": fs.readFileSync("<含完整证书链证书（fullchain）或服务端证书（server cert）的路径>")
	}
```

### 客户端

当服务端配置完成后，[下载客户端](https://github.com/RikkaW/FCM-for-Mojo/releases)并根据应用内提示配置（在管理设备里添加正在使用的设备）即可。
