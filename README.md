# 注意项目文档和代码功能当前阶段没有完全同步, 遇到使用问题请留言


## 主要功能

从源下载文件，然后按照自定义模板保存到指定路径，完全组件化

## 快速部署

### docker

###    

## 处理器

#### 处理器定义

指定组件`rss:mikan`其中`rss`为组件类型`mikan`为组件名称, 如果类型和名称都一样可写成`rss`来指定

```yaml
processors:
   - name: mikan-bangumi #处理器名字
     triggers:
        - fixed:20min #触发器使用fixed类型名字为20min
     source: rss:mikan #源使用rss类型名字为mikan
     providers:
        - mikan #创建器为mikan类型名字为mikan
     downloader: qbittorrent #下载器为qbittorrent类型名字为qbittorrent
     mover: qbittorrent #移动器为qbittorrent类型名字为qbittorrent
     save-path: /mnt/bangumi #最终保存路径
     options: #处理器选项
```

## 例子说明

### 例子1

#### 目标自动下载mikan订阅的番剧，文件路径使用bangumi上的官方标题而不是解析文件得到的名称(因为文件名不规范会有罗马音 中文等混杂着)

1. 处理器每20分钟触发一次，或`webhook`手动触发
2. `rss:mikan`将RSS转换成`SourceItem`
    ```json
    {
        "title": "【喵萌Production】★04月新番★[偶像大师 灰姑娘女孩 U149 / The iDOLM@STER Cinderella Girls U149][01][WebRip][1080p][简日双语][招募翻译]",
        "link": "https://mikanani.me/Home/Episode/eae4f403a0cd4fdb6dad0b4dfcedd6f96b6c883d",
        "date": "2023-04-09T00:54:00",
        "contentType": "application/x-bittorrent",
        "downloadUri": "https://mikanani.me/Download/20230409/eae4f403a0cd4fdb6dad0b4dfcedd6f96b6c883d.torrent"
    }
    ```
3. 过滤掉标题中包含 720P 和中文配音的内容（使用 SpEl 表达式）
4. 使用`mikan`提供的命名变量，从`link`中爬取到 bangumi 的 subject 信息。
5. 提交下载任务到 qbittorrent。
6. 下载完成后，重命名文件并使用 qbittorrent 的 API 将文件移动到指定的存放路径。
7. 当上述任务完成后，执行 touchItemDirectory 和 http:telegram-message-webhook 组件中的逻辑。

```yaml
  - name: mikan-bangumi
    triggers:
       - fixed:20min
       - webhook
    source: rss:mikan
    providers:
       - "mikan"
    downloader: qbittorrent
    mover: qbittorrent
    savePath: /mnt/bangumi
    options:
       runAfterCompletion:
          - touchItemDirectory
          - http:telegram-message-webhook
       fileSavePathPattern: "{name}/Season {season}/"
       filenamePattern: "{nameCn} - S{season}E{episode}"
       #每2分钟重命名任务
       renameTaskInterval: PT2M
       #重命名2次失败后不会再处理
       renameTimesThreshold: 2
       #下载分类(具体看下载器实现)
       downloadCategory: Bangumi
       #关闭metadata变量（具体请看TODO link）
       provideMetadataVariables: false
       #SourceItem级过滤 不能包含(and的关系)
       itemExpressionExclusions:
          - "#title matches '.*720(?!)P.*'"
          - "#title matches '.*中文配音.*'"
       #SourceItem级过滤 包含(or的关系)
       itemExpressionInclusions:
       #SourceItem下的文件过滤 不能包含(and的关系)
       fileExpressionExclusions:
       #SourceItem下的文件过滤 包含(or的关系)
       fileExpressionInclusions:
```

### 例子2

#### 目标简单处理旧番

1. 处理器`webhook`触发
2. `SourceItem`为系统文件
3. 使用`anitom`和`season`的`provider`提供的命名变量, 会从文件名中解析命名变量
4. `systemFile`该组件同时也实现了`downloader`但是文件已经是存在的所以下载的操作实际不用做任何事情
5. 移动文件则是常规的

> 选项中关闭了处理记录的保存意味着每次都会处理，不会过滤之前已经处理过`SourceItem`

```yaml
  - name: '旧番手动整理'
    triggers:
       - webhook
    source: systemFile:animeTempPath
    downloader: systemFile:animeTempPath
    providers:
       - "anitom"
       - "season"
    mover: general
    savePath: /mnt/temp-media/anime
    options:
       fileSavePathPattern: '{parent}/Season {season}'
       filenamePattern: '{animeTitle} S{season}E{episodeNumber} - {source}'
       saveContent: false
       fileExpressionExclusions:
          - "#filename matches '.*torrent.*'"
```

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
