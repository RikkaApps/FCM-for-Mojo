# FCM for Mojo
借助 [Mojo-WebQQ](https://github.com/sjdy521/Mojo-Webqq) 实现将 QQ 消息通过 Firebase Cloud Messaging (FCM) 推送至 Android 设备。
专为 Android 7.0 以上设计，充分利用 Android 通知特性（直接回复，捆绑通知等）。

[English](/README_en.md)

## 配置方法

### 服务端

#### Docker
通过 Docker 方式安装 FFM 十分简单.按照 [kotomei 的教程](https://github.com/kotomei/fcm-for-mojo/blob/master/README.md)，你只需几分钟就可以搞定。

#### 自行配置

##### 依赖

Mojo-Webqq：直接根据[官方教程](https://github.com/sjdy521/Mojo-Webqq#安装方法)即可

Node.js：自己编译安装，或者直接[使用包管理器](https://nodejs.org/en/download/package-manager)

##### 下载服务端

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

#### 安全性

##### HTTP 基本认证

HTTP 基本认证通过 [http-auth 模块](https://github.com/http-auth/http-auth) 实现，在[这里](https://github.com/http-auth/http-auth#configurations)可以看到所有可用选项，下文只说明最简单的配置方法。

通过 OpenSSL，用密码来生成一个 MD5

```Shell
$ openssl passwd
Password:
Verifying - Password:
<MD5>
```

复制好 MD5，然后创建一个包含以下内容的文件：

```
<用户名>:<密码>
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