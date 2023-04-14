# 注意项目文档和代码功能当前阶段没有完全同步, 遇到使用问题请留言

## 主要功能

从定义的源下载文件，按照自定义模板路径保存，完全组件化。

## 快速部署

### docker

编写config.yaml文件到/app/data目录下，然后运行镜像

```shell
run -p 8080:8080 -v /Users/shoaky/temp/downloads:/app/data shoaky009/source-downloader:latest
```

配置详情查看[Wiki](https://github.com/shoaky009/source-downloader/wiki)