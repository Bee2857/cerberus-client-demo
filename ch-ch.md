# Cerberus文档

Cerberus是携程API网关的授权与认证模块，为三方用户与合作伙伴访问携程API接口提供认证与保护机制。

# 基本概念

以下是关于Cerberus的一些基本概念

- **app**

  三方用户、合作公司访问ctrip服务的基本单元

- **appSecret**

  app私钥，常和token或appKey一起用于请求签名。必须谨慎保管appSecret，泄漏会损害安全性。

- **appKey**

  app公钥，和appSecret一起用于管理操作的签名，如app查询、token增删等，具体使用方法见后续章节。

- **token**

  访问API时的身份标识，请注意：

  1. 可同时存在多个token，默认最大数量为10；
  2. token有过期时间，默认为24小时；
3. 因为token有过期时间，因此客户端必须定期获取/刷新token；
  4. 一般来说客户端无需主动删除或添加token，Cerberus会自动对token进行创建与销毁。

# 我该如何接入

- 获取属于我的app

  由携程方对接人申请app，并向他/她获取你app对应的appKey与appSecret。

- 如何写一个自己的client

  1. 需要有个一个后台任务管理你的app，负责获取并更新token；
  2. 需要一个自动为请求签名的http client，签名用于身份校验，细节见后续章节。

- token管理的一些细节与建议

  1. Cerberus本身会保证你的app至少有一个可用时间大于2小时的token；
  2. 建议app在启动时获取token，然后周期性地刷新token；
  3. 刷新时间建议设置为30分钟，最长不超过1小时；
  4. 切忌在代码中指定token，因为token会过期；
  5. 除非怀疑token泄露，否则请勿手动删除token。必须删除时，建议新建并刷新token，避免请求失败。

- Java开发者可以参考我们的demo进行实现 [【链接】](https://github.com/Bee2857/cerberus-client-demo)

# App管理接口

## 权限校验

1. 假设已有app，并且相关信息如下：
   - **appKey**: testApp1
   - **appSecret**: 111222333xxxyyyzzz
2. 系统参数如下：
   - **timeStamp**: 1552632509159（毫秒）

> Tip: Cerberus仅接受1分钟内发起的请求，因此务必使用线上实时生成的timeStamp值。

3. 我们希望获取testApp1的详细信息，对应访问地址：

> GET /api/app/getApp?appKey=testApp1&type=detail

4. 按照以下顺序拼接你的访问uri：

   ```java
   String result = concat(uri.toLowerCase, appKey, timeStamp, secret);
   // result = "/api/app/getapptestApp1552632509159111222333xxxyyyzzz";
   ```

5. 对上一步得到的字符串进行MD5编码，采用utf-8，输出16位小写字符串，我们将得到签名：

   > sign = 8db342c01c85cc27

6. 将sign、timeStamp与appKey加入请求的queryString中，得到最终的访问uri：

   > GET /api/app/getApp?sign=8db342c01c85cc27&timeStamp=1552632509159&appKey=testApp1&type=detail

## 各环境地址

| env        | host                     | scheme |
| ---------- | ------------------------ | ------ |
| production | cerberus.ctrip.com       | https  |
| fws        | cerberus-fws.ctripqa.com | https  |
| uat        | cerberus-uat.ctripqa.com | https  |

## 获取app信息

- Request uri

  GET /api/app/getApp?appKey=testApp1&type=detail

- Request parameters

| name   | type   | example  | description                          |
| :----- | :----- | -------- | :----------------------------------- |
| appKey | String | testApp1 | 一次可请求多个appKey                 |
| type   | String | detail   | 选择如何展示结果，detail表示详细信息 |

- Response fields

| name          | type         | example           | description   |
| ------------- | ------------ | ----------------- | ------------- |
| name          | String       | test service      | app名称       |
| description   | String       | test service No.1 | app描述       |
| appCredential | Object       | {}                | 权限相关信息  |
| tokens        | Object Array | []                | 当前token列表 |

Object fields(appCredential)

| name          | type    | example               | description             |
| ------------- | ------- | --------------------- | ----------------------- |
| appKey        | String  | 4e827ce7c46           | -                       |
| appSecret     | String  | 41357028d203041c53c14 | -                       |
| maxTokenCount | Integer | 10                    | 最大新建token数量       |
| validTime     | Integer | 86400                 | token生存时间，单位：秒 |

Object fields(token)

| name       | type   | example                       | description               |
| ---------- | ------ | ----------------------------- | ------------------------- |
| tokenValue | String | ac876821-f1f3-44a2-b05a-f02ce | token值                   |
| status     | String | alive                         | token状态: alive/expired  |
| expire     | Long   | 1543200293694                 | token过期时间，单位：毫秒 |

- Response example

```json
[{
    "name": "test service",
    "description": "test service No.1",
    "appCredential": {
        "appKey": "4e827ce7c4685b4e8be",
        "appSecret": "41357028d203041c53c1470854049b8cb40867b3de3a701038b5e38e5319ec6a",
        "maxTokenCount": 10,
        "validTime": 86400,
        "datachangeLasttime": "2018-11-26 11:11:08"
    },
    "tokens": [{
        "tokenValue": "ac876821-f1f3-44a2-b05a-f02ce9d824f3",
        "status": "alive",
        "expire": 1543200293694,
        "datachangeLasttime": "2018-11-26 10:43:27"
    }],
    "tags": [
        "owner_aphe",
        "user_aphe"
    ],
    "properties": [{
        "name": "status",
        "value": "activated"
    }]
}]
```

## 添加token

- Request uri

  POST /api/app/createToken

- Request fields

| name   | type   | example  | description |
| :----- | :----- | -------- | :---------- |
| appKey | String | testApp1 | appKey      |

- Request example

```json
{
    "appKey": "testApp1"
}
```

- Response fields

| name       | type   | example                       | description               |
| ---------- | ------ | ----------------------------- | ------------------------- |
| tokenValue | String | ac876821-f1f3-44a2-b05a-f02ce | token值                   |
| status     | String | alive                         | token状态: alive/expired  |
| expire     | Long   | 1543200293694                 | token过期时间，单位：毫秒 |

- Response example

```json
{
    "tokenValue": "70fd8316-c9b2-4b90-957a-e3b07fb1c3cb",
    "status": "alive",
    "expire": 1543202373785
}
```

## 删除token

- Request uri

  GET /api/app/token/delete?token=qqqwwweeerrr

- Request parameters

| name  | type   | example      | description |
| :---- | :----- | ------------ | :---------- |
| token | String | qqqwwweeerrr | token值     |

- Response example

```json
{
    "success": true
}
```

# 业务API调用

## 权限校验

1. 假设已有app，并且相关信息如下：
   - **appSecret**: 111222333xxxyyyzzz
   - **token**: qqqwwweeerrr
2. 系统参数如下：
   - **timeStamp**: 1552632509159（毫秒）

> Tip: Cerberus仅接受1分钟内发起的请求，因此务必使用线上实时生成的timeStamp值。

3. 你希望调用的业务API地址为：

> https://apiproxy.ctrip.com/apiproxy/gateway/test

4. 按照以下顺序拼接你的访问uri：

   ```java
   String result = concat(uri.toLowerCase, token, timeStamp, secret);
   // result = "/apiproxy/gateway/testqqqwwweeerrr1552632509159111222333xxxyyyzzz";
   ```

5. 对上一步得到的字符串进行MD5编码，采用utf-8，输出16位小写字符串，我们将得到签名：

   > sign = 2aebf9bd91ffa82a

6. 将sign、timeStamp与token加入请求的queryString中，得到最终的访问uri：

   > https://apiproxy.ctrip.com/apiproxy/gateway/test?sign=2aebf9bd91ffa82a&timeStamp=1552632509159&token=qqqwwweeerrr

## 访问域名

目前携程服务主要部署在上海。为了提供更高的访问质量，我们申请了不同网络配置的域名，为不同地理位置的供应商提供服务。三方用户可根据实际业务场景以及网络延迟情况，自行选择访问的域名。

> 注意：不同域名仅在外部网络链路上存在区别，实际使用时替换域名即可，uri保持不变。

| host                   | scheme     | target user | description                                  |
| ---------------------- | ---------- | ----------- | -------------------------------------------- |
| apiproxy.ctrip.com     | http&https | 所有        | （推荐）国内解析，akamai加速，香港专线回源   |
| intl-api.ctrip.com     | http&https | 海外        | 仅海外解析，akamai加速                       |
| hkproxy.ctrip.com      | http&https | 香港        | 香港专线回源                                 |
| alliance-api.ctrip.com | http&https | 特殊        | 无海外加速配置，建议国内或海外大流量用户使用 |
