## 主要功能

    从源下载文件，然后按照自定义模板保存到指定路径，完全组件化插件化

## 快速部署

### docker

###  

## 组件

- Trigger: 触发器，触发处理器执行
- Source: 从源转换成SourceItem
- SourceFilter: 过滤SourceItem标题的规则
- Provider: 根据sourceItem提供对应路径上的变量
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
  variable-provider:
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
- webhook: webhook触发 (在路上了.jpg)
- dynamic: 动态自适应触发 (在路上了.jpg)

source

- rss: rss源
- watch: 监听文件路径 (在路上了.jpg)
- files: 文件列表 (在路上了.jpg)

provider

- mikan: Mikan番剧相关（已内置mikan插件）

downloader

- qbittorrent: qBittorrent
- aria2: aria2 (在路上了.jpg)
- transmission: transmission (在路上了.jpg)

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
    providers:
      - mikan #创建器为mikan类型名字为mikan
    downloader: qbittorrent #下载器为qbittorrent类型名字为qbittorrent
    mover: qbittorrent #移动器为qbittorrent类型名字为qbittorrent
    save-path: /mnt/bangumi #最终保存路径
    options: #处理器选项
      #完成后运行
      run-after-completion: #命名完成后执行的任务
        - touchItemDirectory #touchItemDirectory为内置的任务
        - http:telegram-message-webhook #http为内置的任务类型，telegram-message-webhook为任务名字
      #可用变量具体看provider能提供什么
      file-save-path-pattern: "{name}/Season {season}/" #文件路径保存路径模板
      #可用变量具体看provider能提供什么
      filename-pattern: "{name-cn} - S{season}E{episode}" #文件名模板
      #全局过滤非必填
      blacklist-regex: #SourceItem过滤的正则列表
        - '720(?i)P'
        - '中文配音'
      #重命名间隔时间,参考java.time.Duration的格式
      rename-task-interval: PT2M #每1分钟检查一次是否有需要重命名的任务
      renameTimesThreshold: 2 #重命名次数阈值，超过阈值的任务将不再执行重命名
      downloadCategory: Bangumi
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

尽可能让程序启动时就暴露出处理器组件兼容性的错误，而不是在运行时才暴露出来