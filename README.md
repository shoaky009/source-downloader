# 注意项目文档和代码功能当前阶段没有完全同步, 遇到使用问题请留言

# 暂时请勿部署在不安全的网络环境下，API并未做认证处理

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-blueviolet)
![JRE](https://img.shields.io/badge/JRE-21-orange)
[![GPLv3 license](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://github.com/shoaky009/source-downloader/blob/main/LICENSE)
![Test](https://github.com/shoaky009/source-downloader/actions/workflows/test.yml/badge.svg?branch=main)
[![Codecov](https://codecov.io/gh/shoaky009/source-downloader/branch/main/graph/badge.svg)](https://codecov.io/gh/shoaky009/source-downloader)

## 主要功能
- 所有具体的行为由对应组件实现, 通过配置组合不同的组件适应不同的资源处理
- 自定义触发下载规则
- 丰富的变量过滤，支持多个纬度和阶段过滤
- 只下载需要处理的文件, 不同的源一定程度上防止重复下载
- 模板路径保存(内置部份变量，也可以由特定类型的组件提供额外的)
- 文件替换规则
- 应对不同场景额外的选项功能
- 插件式注册自定义组件

## 快速部署

### docker

编写config.yaml挂载到容器/app/data下，然后运行镜像

```shell
docker run -p 8080:8080 -v /path/source-downloader:/app/data shoaky009/source-downloader:latest
```

```shell
# 设置代理
docker run -p 8080:8080 -e "JAVA_OPTS=-Dhttp.proxyHost={your-host} -Dhttp.proxyPort={your-port} -Dhttps.proxyHost={your-host} -Dhttps.proxyPort={your-port}" shoaky009/source-downloader:latest
```

`config.yaml`配置详情查看[文档](https://github.com/shoaky009/source-downloader/wiki)
或[示例](examples/config-example.yaml)

## 从场景出发快速开始

- [自动追蜜柑新番](examples/mikan-bangumi.yaml)
- [整理本地动画](examples/anime-local.yaml)
- [远程动画订阅](examples/anime-remote.yaml)
- [电报频道下载](examples/telegram.yaml)

## [API 文档](docs/api.md)