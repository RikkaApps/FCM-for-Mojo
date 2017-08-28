#### 启动方式
使用 `node node/index.js` 来开启，默认占用 3 个端口号 5003 5004 5005。若要修改端口号，请自行修改 `config.js`，其他内容（HTTPS，HTTP 基本验证等的设置方式）请[看这里](https://github.com/RikkaW/FCM-for-Mojo/wiki/%E9%85%8D%E7%BD%AE%E6%96%87%E4%BB%B6%E8%A7%A3%E9%87%8A)。

#### 文件们的作用
* `config.js`

  Node 的 HTTP 服务器的设置。
  
* `client.json`

  客户端 token 和客户端的设置。


* `node` 里面

  Node 的 HTTP 服务器，负责推送、/ffm 相关 API 及将 /openqq 请求转发至对应插件。
  
* `perl` 里面
  
  start.pl：启动 Mojo-Webqq 及需要的插件们的脚本
    
* `plugin` 里面的 `FFM.pm`

  mojo-webqq 的插件，在提交给 mojo-webqq 前需要自己复制到正确的位置，可能是 `/usr/local/share/perl/版本号/Mojo/Webqq/Plugin`。
  

