# 广告系统外部对接接口文档 

## 1. 接口概述
本接口供第三方平台调用，通过同步用户视频评分数据（Score）来实时获取系统匹配的广告列表。

* **请求路径**：`http://[服务器IP]:[端口]/[项目名]/api/external/getAds`
* **请求方法**：`POST`
* **数据格式**：`application/json`
* **字符编码**：`UTF-8`

---

## 2. 请求头 (Headers)
| 参数名 | 必选 | 类型 | 说明 | 示例值 |
| :--- | :--- | :--- | :--- | :--- |
| **X-API-Key** | 是 | String | 身份验证密钥 | `123456789abcdef` |
| **Content-Type** | 是 | String | 内容类型 | `application/json` |

---

## 3. 请求参数 (Body)

| 字段名 | 必选 | 类型 | 说明 | 示例 |
| :--- | :--- | :--- | :--- | :--- |
| **tag** | **是** | String | 视频分类标签（由广告系统定义） | `digital` |
| **score** | **是** | Integer | 用户评分，可选值：`1`, `2`, `3` | `3` |
| **platform** | **是** | String | 来源平台标识 | `video` |
| `anonymousUserId` | 否 | String | 用户唯一识别码(UUID) | `u_88291` |
| `limit` | 否 | Integer | 返回广告数量上限，默认 `10` | `5` |

**请求示例：**
```json
{
  "tag": "digital",
  "score": 3,
  "platform": "video",
  "anonymousUserId": "user_123456789",
  "limit": 5
}
```

---

## 4. 响应结果

### 4.1 成功字段说明（ads 数组项）

| 字段名        | 类型    | 说明 |
|--------------|---------|------|
| adId         | Integer | 广告唯一 ID |
| title        | String  | 广告标题 |
| description  | String  | 广告简短描述 |
| textContent  | String  | 广告文本详情内容 |
| imageUrl     | String  | 图片素材链接 |
| videoUrl     | String  | 视频素材链接 |
| targetUrl    | String  | 点击后跳转的落地页地址 |
| category     | String  | 广告所属分类名称 |

---

### 4.2 响应示例（HTTP 200）

```json
{
  "success": true,
  "ads": [
    {
      "adId": 1024,
      "title": "新款智能手机",
      "description": "极速体验，限时特惠",
      "textContent": "点击了解更多产品参数及优惠券...",
      "imageUrl": "https://cdn.example.com/ad/pic01.jpg",
      "videoUrl": "https://cdn.example.com/ad/video01.mp4",
      "targetUrl": "https://item.jd.com/12345.html",
      "category": "数码产品"
    }
  ]
}
```

---

### 4.3  异常响应

| 状态码 | 含义           | 说明                               |
| --- | ------------ | -------------------------------- |
| 400 | Bad Request  | JSON 格式错误或缺少必填字段（tag / platform） |
| 401 | Unauthorized | X-API-Key 缺失或无效                  |
| 500 | Server Error | 广告系统后端服务异常                       |

 
