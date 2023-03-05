## 主要功能

    从源下载文件，然后按照自定义模板保存到指定路径，完全组件化插件化

## 快速部署

### docker

###  

## 组件

- Trigger: 触发器，触发处理器执行
- Source: 从源转换成SourceItem
- SourceFilter: 过滤SourceItem标题的规则
- Creator: 从SourceItem创建下载任务，放置路径变量
- Downloader: 下载任务
- FileMover: 从下载路径移动文件到目标路径
- RunAfterCompletion: 移动文件完成后执行的任务

### 组件定义

```yaml
components:
  downloader:
    - name: truenas
      type: qbittorrent
      props:
        url: http://truenas:10095
        username: admin
        password: adminadmin
  source:
    - name: mikan
      type: rss
      props:
        url: https://mikanani.me/RSS/Bangumi?bangumiId=2852&subgroupid=583
  source-content-creator:
    - name: mikan
      type: mikan
  file-mover:
    - name: truenas
      type: qbittorrent
  run-after-completion:
    - name: n8n-webhook-send-message
      type: script
      props:
        path: /app/data/script.sh
  trigger:
    - name: 20min
      type: fixed
      props:
        interval: PT20M
        on-start-run-tasks: false
```

#### 内置组件

trigger

- fixed: 定时触发
- cron: cron表达式触发
- webhook: webhook触发
- dynamic: 动态自适应触发

source

- rss: rss源
- watch: 监听文件路径

creator

- mikan: Mikan番剧相关（需安装插件）

downloader

- qbittorrent: qBittorrent
- aria2: aria2
- transmission: transmission

file-mover

- general: 通用移动器，使用文件系统移动文件
- qbittorrent: 使用qBittorrent的API移动文件（保种）
- aria2: 使用aria2的API移动文件（保种）

run-after-completion

- script: 执行脚本

## 处理器

#### 处理器定义

```yaml
processors:
  - name: mikan-bangumi #处理器名字
    trigger: fixed:20min #触发器使用fixed类型名字为20min
    source: rss:mikan #源使用rss类型名字为mikan
    creator: mikan #创建器为mikan类型名字为mikan
    downloader: qbittorrent:truenas #下载器为qbittorrent类型名字为truenas
    mover: qbittorrent:truenas #移动器为qbittorrent类型名字为truenas
    save-path: /mnt/bangumi #保存路径
    options: #处理器选项
      run-after-completion: #命名完成后执行的任务
        - script:n8n-webhook-send-message #组件类型为script(执行脚本)
      file-save-path-pattern: "{origin-name}/Season {season}/" #文件路径保存路径模板
      filename-pattern: "S{season}E{episode} - {version}-{resolution}" #文件名模板
      blacklist-regex: #SourceItem过滤的正则列表
        - 720P
        - 中文配音
      rename-task-interval: PT1M #每1分钟检查一次是否有需要重命名的任务
```

如上配置能够已有的组件实现订阅mikan的rss源，用qbittorrent下载完成后移动到指定模板路径，然后执行脚本

## 插件

项目提供插件的方式来注册自定义组件

插件开发步骤

- 1.创建一个可以打包为Jar的工程
- 2.导入sdk依赖
- 3.实现`xyz.shoaky.sourcedownloader.sdk.Plugin`接口
- 4.编写组件和提供组件的提供者
- 5.在`resource/META-INF/services/xyz.shoaky.sourcedownloader.mikan.MikanPlugin`的文件下写入你插件实现的全类名
- 6.打包为Jar放入主应用程序的classpath下
- 7.重启应用

> 如果是使用镜像`/app/plugins`已经是内置的classpath目录直接放入即可，如果是自己使用java命令启动自行加-cp参数，注意-jar启动不能生效.

TODO

- [x] 插件化，提供Source,Grouping,Creator,Downloader的注册方式
- [x] 支持SourceItem包含多个文件, 但下载任务只有一个
- [x] 初步完成mikan插件
- [x] 组件之间的适配性表达(比如Source选了Mikan,那其他几个组件自动填写Downloader一定是某个TorrentDownloader)
- [ ] 增加常用的内置组件aria2, transmission等等...
- [ ] 组件文档自动生成
- [ ] 当Processor命名规则变化了，旧文件更新为新的规则(只有可用变量都适配时)
- [ ] webui, 模板功能

小需求

- [x] 修改文件夹创建时间
- [ ] 处理器模式
- [ ] sourceItem日期过滤
- [ ] windows下路径特殊字符处理