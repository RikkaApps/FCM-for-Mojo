#### 启动方式
使用 `start.sh` 来开启，默认占用 3 个端口号 5003 5004 5005。若要修改端口号，请自行修改 `start.sh`。

#### 文件们的作用
* `node` 里面

  Node.js 的 http 服务器，负责将请求们转发至 mojo-webqq 的插件以及处理停止和重启 mojo-webqq 的请求。
  
* `perl` 里面
  
  start.pl：启动 mojo-webqq 及需要的插件们的脚本
  
  conf.json：配置文件，包括客户端 token 和客户端的设置
  
* `start.sh`

  启动脚本
  
* `plugin` 里面的 `FFM.pm`

  mojo-webqq 的插件，在提交给 mojo-webqq 前需要自己复制到正确的位置，可能是 `/usr/local/share/perl/版本号/Mojo/Webqq/Plugin`。