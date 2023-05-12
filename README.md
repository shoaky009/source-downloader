# 注意项目文档和代码功能当前阶段没有完全同步, 遇到使用问题请留言

![Kotlin](https://img.shields.io/badge/Kotlin-1.8.20-blueviolet)
![JRE](https://img.shields.io/badge/JRE-17--20-orange)
[![GitHub License](https://img.shields.io/github/license/shoaky009/source-downloader)](https://github.com/shoaky009/source-downloader/blob/main/LICENSE)

## 主要功能

从定义的源下载文件，按照自定义模板路径保存，插件式注册自定义组件。

## 快速部署

### docker

编写config.yaml挂载到容器/app/data下，然后运行镜像

```shell
docker run -p 8080:8080 -v /path/source-downloader:/app/data shoaky009/source-downloader:latest
```

```shell
# 设置代理
docker run -p 8080:8080 -e "JAVA_OPTS=-Dhttp.proxyHost={your-host} -Dhttp.proxyPort={your-port} -Dhttps.proxyHost={your-host} -Dhttps.proxyPort={your-port}" shoaky009/source-downloader:dev
```

config.yaml配置详情查看[文档](https://github.com/shoaky009/source-downloader/wiki)