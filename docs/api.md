## 目录

- [认证](#认证)
- [应用](#应用)
    - [获取应用信息](#获取应用信息)
    - [重载应用](#重载应用)
- [组件](#组件)
    - [查询组件](#查询组件)
    - [创建组件](#创建组件)
    - [删除组件](#删除组件)
    - [重载组件](#重载组件)
    - [获取组件描述](#获取组件描述)(未完成)
- [处理器](#处理器)
    - [查询处理器](#查询处理器)(未完成)
    - [获取处理器](#获取处理器)
    - [创建处理器](#创建处理器)(未完成)
    - [更新处理器](#更新处理器)(未完成)
    - [删除处理器](#删除处理器)(未完成)
    - [重载处理器](#重载处理器)
    - [演练处理器](#演练处理器)
    - [触发处理器](#触发处理器)
    - [提交Item到处理器](#提交Item到处理器)(未完成)
    - [获取处理器状态](#获取处理器状态)(未完成)
- [处理信息](#处理信息)
    - [获取处理信息](#获取处理信息)
    - [查询处理信息](#查询处理信息)
    - [删除处理信息](#删除处理信息)
    - [重新处理](#重新处理)
- [目标路径](#目标路径)
    - [删除目标路径](#删除目标路径)(实验性)

## 认证

暂无

## 应用

GET /api/application/info

### 获取应用信息

> 返回示例

> 200 Response

```json
{
  "group": "string",
  "artifact": "string",
  "name": "string",
  "time": "string",
  "version": "string"
}
```

```bash
curl -X GET "http://localhost:8080/api/application/info"
```

## 重载应用

GET /api/application/reload

重载配置上的所有资源，按顺序销毁处理器->组件->实例，再从开启的处理器按需加载所需的组件和实例

> 返回示例

> 204 Response
>

```bash
curl -X GET "http://localhost:8080/api/application/reload"
```

## 组件

### 创建组件

POST /api/component

> 返回示例

> 201 Response

```json
{
  "type": "string",
  "typeName": "string",
  "name": "string",
  "props": {}
}
```

### 查询组件

GET /api/component

#### 请求参数

| 名称       | 位置    | 类型     | 必选 | 说明   |
|----------|-------|--------|----|------|
| type     | query | string | 否  | 组件类型 |
| typeName | query | string | 否  | 类型名称 |
| name     | query | string | 否  | 组件名称 |

> 返回示例

> 成功

```json
[
  {
    "type": "source",
    "typeName": "telegram",
    "name": "telegram",
    "props": {
      "client": "telegram-client",
      "chats": [
        {
          "chat-id": "-114514"
        }
      ]
    },
    "primary": true
  },
  {
    "type": "downloader",
    "typeName": "telegram",
    "name": "telegram",
    "props": {
      "client": "telegram-client",
      "download-path": "/downloads"
    },
    "stateDetail": {
      "downloaded": 3,
      "downloading": []
    },
    "primary": true
  }
]
```

```bash
curl -X GET "http://localhost:8080/api/component?type=source&typeName=telegram"
```

### 删除组件

DELETE /api/component/{type}/{typeName}/{name}

#### 请求参数

| 名称       | 位置   | 类型     | 必选 | 说明   |
|----------|------|--------|----|------|
| type     | path | string | 是  | none |
| typeName | path | string | 是  | none |
| name     | path | string | 是  | none |

> 返回示例

> 204 Response

```bash
curl -X DELETE "http://localhost:8080/api/component/source/telegram/telegram"
```

### 重载组件

GET /api/component/{type}/{typeName}/{name}/reload

销毁当前组件实例从配置中重建

#### 请求参数

| 名称       | 位置   | 类型     | 必选 | 说明   |
|----------|------|--------|----|------|
| type     | path | string | 是  | none |
| typeName | path | string | 是  | none |
| name     | path | string | 是  | none |

> 返回示例

> 204 Response

```bash
curl -X GET "http://localhost:8080/api/component/source/telegram/telegram/reload"
```

## 处理器

### 获取处理器

GET /api/processor/{processorName}

#### 请求参数

| 名称            | 位置   | 类型     | 必选 | 说明   |
|---------------|------|--------|----|------|
| processorName | path | string | 是  | none |

> 返回示例

> 200 Response

```json
{
  "name": "string",
  "triggers": [
    "string"
  ],
  "source": "string",
  "itemFileResolver": "string",
  "downloader": "string",
  "fileMover": "string",
  "savePath": "string",
  "options": {
    "variableProviders": [
      "string"
    ],
    "savePathPattern": "string",
    "filenamePattern": "string",
    "processListeners": [
      {
        "id": "string",
        "mode": "string"
      }
    ],
    "renameTaskInterval": "string",
    "downloadOptions": {
      "category": "string",
      "tags": [
        "string"
      ],
      "headers": {}
    },
    "renameTimesThreshold": 0,
    "itemExpressionExclusions": [
      "string"
    ],
    "fileReplacementDecider": "string",
    "fileExistsDetector": "string",
    "itemGrouping": [
      {
        "tags": [
          "string"
        ],
        "expressionMatching": "string",
        "itemExpressionExclusions": [
          "string"
        ]
      }
    ],
    "parallelism": 0
  },
  "enabled": true
}
```

```bash
curl -X GET "http://localhost:8080/api/processor/mikan-bangumi"
```

### 重载处理器

GET /api/processor/{processorName}/reload

销毁当前处理器从配置中重建

#### 请求参数

| 名称            | 位置   | 类型     | 必选 | 说明   |
|---------------|------|--------|----|------|
| processorName | path | string | 是  | none |
| body          | body | object | 否  | none |

> 返回示例

> 204 Response

```bash
curl -X GET "http://localhost:8080/api/processor/mikan-bangumi/reload"
```

### 演练处理器

POST /api/processor/{processorName}/dry-run

处理器演练，不会提交下载

> Body 请求参数

```json
{
  "pointer": {
    "latest": [
      2024,
      1,
      8,
      0,
      31,
      38,
      435000000
    ],
    "shows": {}
  },
  "filterProcessed": true
}
```

#### 请求参数

| 名称                | 位置   | 类型      | 必选 | 说明               |
|-------------------|------|---------|----|------------------|
| processorName     | path | string  | 是  | none             |
| body              | body | object  | 否  | none             |
| » pointer         | body | object  | 否  | source的迭代记录，非结构化 |
| » filterProcessed | body | boolean | 否  | 是否过滤已处理的Item     |

> 返回示例

> 成功

```json
[
  {
    "sourceItem": {
      "title": "[VCB-Studio] Kumo desu ga, Nani ka [Ma10p_1080p]",
      "link": "file:///mnt/temp-media/anime-temp/%5BVCB-Studio%5D%20Kumo%20desu%20ga,%20Nani%20ka%20%5BMa10p_1080p%5D/",
      "datetime": "2024-02-18T15:35:31.46533142",
      "contentType": "directory",
      "downloadUri": "file:///mnt/temp-media/anime-temp/%5BVCB-Studio%5D%20Kumo%20desu%20ga,%20Nani%20ka%20%5BMa10p_1080p%5D/",
      "attrs": {
        "size": 26
      },
      "tags": []
    },
    "sharedVariables": {
      "title": "Kumo desu ga, Nani ka?",
      "nativeName": "蜘蛛ですが、なにか？"
    },
    "fileResults": [
      {
        "from": "/mnt/temp-media/anime-temp/[VCB-Studio] Kumo desu ga, Nani ka [Ma10p_1080p]/[VCB-Studio] Kumo desu ga, Nani ka [01][Ma10p_1080p][x265_flac_ac3].mkv",
        "to": "/mnt/temp-media/anime/蜘蛛ですが、なにか？/Season 01/Kumo desu ga, Nani ka？ S01E01.mkv",
        "variables": {
          "season": "01",
          "fileExtension": "mkv",
          "fileName": "[VCB-Studio] Kumo desu ga, Nani ka [01][Ma10p_1080p][x265_flac_ac3]",
          "videoResolution": "1080p",
          "videoTerm": "x265",
          "audioTerm": "ac3",
          "episode": "01",
          "animeTitle": "Kumo desu ga, Nani ka",
          "releaseGroup": "VCB-Studio"
        },
        "tags": [
          "video"
        ],
        "status": "NORMAL"
      }
    ],
    "status": "WAITING_TO_RENAME"
  }
]
```

```bash
curl -X GET http://localhost:8080/api/processor/mikan-bangumi/dry-run

curl -X POST http://localhost:8080/api/processor/mikan-bangumi/dry-run -H "Content-Type: application/json" -d '{"pointer" :{"latest":[2024,1,8,0,31,38,435000000], "shows":{}}}
```

### 触发处理器

GET /api/processor/{processorName}/trigger

#### 请求参数

| 名称            | 位置   | 类型     | 必选 | 说明   |
|---------------|------|--------|----|------|
| processorName | path | string | 是  | none |

> 返回示例

> 202 Response

```bash
curl -X GET "http://localhost:8080/api/processor/mikan-bangumi/trigger"
```

## 处理信息

### 获取处理内容

GET /api/processing-content/{id}

#### 请求参数

| 名称 | 位置   | 类型     | 必选 | 说明   |
|----|------|--------|----|------|
| id | path | string | 是  | none |

> 返回示例

> 200 Response

```json
{
  "id": 111,
  "processorName": "string",
  "itemHash": "string",
  "itemContent": {
    "sourceItem": {
      "title": "string",
      "link": "string",
      "datetime": "string",
      "contentType": "string",
      "downloadUri": "string",
      "attrs": {},
      "tags": [
        "string"
      ]
    },
    "sourceFiles": [
      {
        "fileDownloadPath": "string",
        "sourceSavePath": "string",
        "downloadPath": "string",
        "patternVariables": {
        },
        "fileSavePathPattern": "string",
        "filenamePattern": "string",
        "targetSavePath": "string",
        "targetFilename": "string",
        "attrs": {
          "size": 0
        },
        "tags": [
          "string"
        ],
        "errors": [
          "string"
        ],
        "status": "string"
      }
    ],
    "itemVariables": {
      "name": "string",
      "nameCn": "string",
      "mikanTitle": "string",
      "date": "string",
      "year": "string",
      "month": "string",
      "season": "string"
    }
  },
  "renameTimes": 0,
  "status": "string",
  "modifyTime": "string",
  "createTime": "string"
}
```

```bash
curl -X GET "http://localhost:8080/api/processing-content/111"
```

### 删除处理内容

DELETE /api/processing-content/{id}

#### 请求参数

| 名称 | 位置   | 类型     | 必选 | 说明   |
|----|------|--------|----|------|
| id | path | string | 是  | none |

> 返回示例

> 204 Response

```bash
curl -X DELETE "http://localhost:8080/api/processing-content/111"
```

### 查询处理内容

GET /api/processing-content

#### 请求参数

| 名称               | 位置    | 类型      | 必选 | 说明   |
|------------------|-------|---------|----|------|
| limit            | query | integer | 否  | none |
| maxId            | query | integer | 否  | none |
| processorName    | query | string  | 否  | none |
| status           | query | string  | 否  | none |
| id               | query | string  | 否  | none |
| itemHash         | query | string  | 否  | none |
| itemTitle        | query | string  | 否  | none |
| createTime.begin | query | string  | 否  | none |
| createTime.end   | query | string  | 否  | none |

> 返回示例

> 成功

```json
{
  "contents": [
    {
      "id": 146199,
      "processorName": "mikan-bangumi",
      "itemHash": "630775db789c83f2ba305fdb7e8cef38",
      "itemContent": {
        "sourceItem": {
          "title": "[ANi] Metallic Rouge /  金属口红 - 07 [1080P][Baha][WEB-DL][AAC AVC][CHT][MP4]",
          "link": "https://mikanani.me/Home/Episode/2ff2cc92b594f57e7f62a1c62de95306ce7f6cae",
          "datetime": "2024-02-22T00:56:30.264",
          "contentType": "application/x-bittorrent",
          "downloadUri": "https://mikanani.me/Download/20240222/2ff2cc92b594f57e7f62a1c62de95306ce7f6cae.torrent",
          "attrs": {},
          "tags": []
        },
        "sourceFiles": [
          {
            "fileDownloadPath": "/downloads/[ANi] 金屬口紅 - 07 [1080P][Baha][WEB-DL][AAC AVC][CHT].mp4",
            "sourceSavePath": "/mnt/bangumi",
            "downloadPath": "/downloads",
            "patternVariables": {
              "season": "01",
              "episode": "07"
            },
            "fileSavePathPattern": "{name}/Season {season}/",
            "filenamePattern": "{nameCn} - S{season}E{episode}",
            "targetSavePath": "/mnt/bangumi/メタリックルージュ/Season 01",
            "targetFilename": "金属口红 - S01E07.mp4",
            "attrs": {
              "size": 378876998
            },
            "tags": [],
            "errors": [],
            "status": "NORMAL"
          }
        ],
        "itemVariables": {
          "name": "メタリックルージュ",
          "nameCn": "金属口红",
          "mikanTitle": "金属胭脂",
          "date": "2024-01-10",
          "year": "2024",
          "month": "1",
          "season": "01"
        }
      },
      "renameTimes": 1,
      "status": "RENAMED",
      "modifyTime": "2024-02-22T01:19:46.765",
      "createTime": "2024-02-22T01:09:24.077"
    }
  ],
  "nextMaxId": 146221
}
```

```bash
curl -X GET "http://localhost:8080/api/processing-content"
curl -X GET "http://localhost:8080/api/processing-content?maxId=146221&processorName=mikan-bangumi&status=WAITING_TO_RENAME,FAILURE&itemTitle=test&createTime.begin=2024-01-01T00:00:00&createTime.end=2024-02-01T23:59:59"
```

### 重新处理

POST /api/processing-content/{id}/reprocess

重新处理Item

#### 请求参数

| 名称 | 位置   | 类型     | 必选 | 说明   |
|----|------|--------|----|------|
| id | path | string | 是  | none |

> 返回示例

> 200 Response

## 目标路径

### 删除目标路径记录

DELETE /api/target-path

只删除记录不会对文件操作

> Body 请求参数

```json
[
  "/mnt/bangumi/test/1.mp4",
  "/mnt/bangumi/test2/*"
]
```

#### 请求参数

| 名称   | 位置   | 类型            | 必选 | 说明   |
|------|------|---------------|----|------|
| body | body | array[string] | 否  | none |

> 返回示例

> 204 Response

```bash
curl -X DELETE http://localhost:8080/api/target-path -H "Content-Type: application/json" -d '["/mnt/bangumi/test/1.mp4", "/mnt/bangumi/test2/*"]'
```