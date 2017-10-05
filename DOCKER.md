# FCM for Mojo (Docker)

## 目录
1. [安装 Docker](#安装-docker)
2. [获取 Docker 镜像](#获取-Docker-镜像)
3. [运行 Docker](#运行-docker)
4. [终止 Docker](#终止-docker)

## 安装 Docker

Docker 安装与运行因发行版而异，请多加搜索[相关信息](https://docs.docker.com)。

### 方法一：快速安装 Docker（推荐，来自 Docker 官方）

```
wget -qO- https://get.docker.com/ | sh
```

### 方法二：手动安装 Docker

参阅 Docker 手册 [安装章节](https://docs.docker.com/engine/installation/#supported-platforms)。

## 获取 Docker 镜像

### 方法一：快速获取 Docker 镜像（较旧，快速）

```
docker pull kotomeinyan/fcm-for-mojo
```

### 方法二：手动创建 Docker 镜像（较新，慢速）

```
docker build --pull --rm -t kotomeinyan/fcm-for-mojo github.com/RikkaW/FCM-for-Mojo
```

**TIPS:**  
安装指定版本的服务端
```
docker build --pull --rm -t kotomeinyan/fcm-for-mojo github.com/RikkaW/FCM-for-Mojo#<version_tag>
```

## 运行 Docker

### 初次运行调试

```
docker run -it -e USER=<your_username> -e PASSWD=<your_password> -p <port>:5005 kotomeinyan/fcm-for-mojo
```

### 守护进程运行

```
docker run -d -e USER=<your_username> -e PASSWD=<your_password> -v ~/client.json:/data/server/client.json -p <port>:5005 kotomeinyan/fcm-for-mojo
```

参数说明：  
USER 与 PASSWD 为 html 基础验证所需字段，默认为`rikka:rikka`。  
PORT 为外网端口，如`-p 5005:5005`即可在外网打开5005端口通信，如需 SSL 验证，务必使用`-p 127.0.0.1:5005:5005`只开放内网访问，并借助其它反向代理程序实现。  
保留设备 ID 列表可以采用 Docker 内建的文件挂载，如`-v ~/client.json:/data/server/client.json`即可用当前用户主目录下的 client.json 替换 Docker 中的空 client.json 以保留设备信息。

**请确保外网端口在防火墙上是开启的**

## 终止 Docker

使用 Docker ps 指令获取正在运行的 Docker 容器列表

```
docker ps
```

停止想要终止运行的 Docker 容器

```
docker stop <container_id>
```

使用 Docker images 指令获取当前存放的 Docker 镜像列表

```
docker images
```

删除不再需要的 Docker 镜像（服务端更新后）

```
docker rmi <image_id>
```

## 遇到错误？

```
docker logs <container_id>
```

将输出发在[issue](https://github.com/RikkaW/FCM-for-Mojo/issues/new)，并在标题附上`[Docker]`字样，我们会帮助您解决问题。