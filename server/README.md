#### 启动方式
使用 `node node/index.js` 来开启，默认占用 3 个端口号 5003 5004 5005。若要修改端口号，请自行修改 `config.json`，其他内容（HTTPS，HTTP 基本验证等的设置方式）请[看这里](https://github.com/RikkaW/FCM-for-Mojo/wiki/%E9%85%8D%E7%BD%AE%E6%96%87%E4%BB%B6%E8%A7%A3%E9%87%8A)。

#### 文件们的作用
* `config.json`

  Node.js 的 HTTP 服务器的设置。

* `node` 里面

  Node.js 的 HTTP 服务器，负责将请求们转发至 Mojo-Webqq 的插件以及处理停止和重启 Mojo-Webqq 的请求。
  
* `perl` 里面
  
  start.pl：启动 Mojo-Webqq 及需要的插件们的脚本
  
  conf.json：配置文件，包括客户端 token 和客户端的设置
  
* `plugin` 里面的 `FFM.pm`

  mojo-webqq 的插件，在提交给 mojo-webqq 前需要自己复制到正确的位置，可能是 `/usr/local/share/perl/版本号/Mojo/Webqq/Plugin`。