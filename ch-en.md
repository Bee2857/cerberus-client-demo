# Cerberus Documentation

Cerberus is the authentication&authorization module of ctrip API Gateway. It is strongly recommended that business parteners and affliates access our APIs via Cerberus so that traffics can be protected from potential malicious third parties.

# What should you know

Below shows the basics you should know about Cerberus.

- **app**

  Basic API-accessing unit of Cerberus.

- **appSecret**

  Secret key of your app. it is used for signature. Please be noted that:

  1. AppSecret must be kept safely. Any leakage may lead to a safe-fail.
  2. AppSecret can be modified, but handle this carefully otherwise your on-going API traffic might be effected.

- **appKey**

  Public key of your app. It is unique and unmodifiable. AppKey and appSecret can be used for the query and management of your app. See details in the following chapters.

- **token**

  Token is the runtime mark of your identity when you're accessing our APIs. Below shows the default configuration for every token:

  1. Default max alive token count: 10.
  2. Default alive time: 86400s\(1day).
  
  Please be noted that:
  
  1. Every token expires, users must implement their own runtime token management mechanism.
  2. It's unnecessary to create or delete tokens on your own since Cerberus will do this for you.

# What should you do

- Get your own app

  Reach out for your contact @CTRIP, ask him/her to do the application for you. Ask them for your  appKey and appSecret.

- To implement your own Cerberus client, you'll need: 
  
  1. A backgroud thread to do your runtime app&token management.
  2. A customized http client to sign and send your http request.
  
- It is guaranteed by Cerberus that  your app have at least one token with a remaining lifetime longer than 2 hours. It is  highly suggested that: 
  
  1. Acquire your token with appKey at startup.
  2. Refresh your app info periodically, get and set the lastest token with a 30-minute interval.
  3. NEVER delete your token manually, unless you have a potential token leakage, in which case,  ALWAYS create new token and update before deleting old ones. Otherwise API requests might fail.
  
- For Java Developers, a demo implementation is provided, link: [【link】](https://github.com/Bee2857/cerberus-client-demo)

# App management API

## Authentication

1. Let's say you already have an app with the following properties:
   - **appKey**: testApp1
   - **appSecret**: 111222333xxxyyyzzz
2. With the system property:
   - **timeStamp**: 1552632509159(milliseconds)

> Tip: Use the runtime timeStamp value because Cerberus server only accepts requests that are sent within 1 minute

3. We'd like to remove token "qqqwwweeerrr", thus belowing uri will be used:

> GET /api/app/getApp?appKey=testApp1&type=detail

4. Concatenate strings in the following order

   ```java
   String result = concat(uri.toLowerCase, appKey, timeStamp, secret);
   // result = "/api/app/getapptestApp1552632509159111222333xxxyyyzzz";
   ```

5. Invoke MD5 on the result string (charset: utf-8, lowercase, length:16), we'll get:

   > sign = 8db342c01c85cc27

6. Add sign, timeStamp and appKey in the query string of your request, finally the request would be:

   > GET /api/app/getApp?token=qqqwwweeerrr&sign=8db342c01c85cc27&timeStamp=1552632509159&appKey=testApp1&type=detail

## Reqeuest address

| env        | host                     | scheme |
| ---------- | ------------------------ | ------ |
| production | cerberus.ctrip.com       | https  |
| fws        | cerberus-fws.ctripqa.com | https  |
| uat        | cerberus-uat.ctripqa.com | https  |

## Get app

- Request uri

  GET /api/app/getApp?appKey=testApp1&type=detail

- Request parameters

| name   | type   | example  | description                                                  |
| :----- | :----- | -------- | :----------------------------------------------------------- |
| appKey | String | testApp1 | You may query multiple appKeys in a single query             |
| type   | String | detail   | Determine how the result will be shown. Info(default) shows the basics of an app. Detail shows more specific info. |

- Response fields

| name          | type         | example           | description                  |
| ------------- | ------------ | ----------------- | ---------------------------- |
| name          | String       | test service      | app name                     |
| description   | String       | test service No.1 | app description              |
| appCredential | Object       | {}                | credential related info, see |
| tokens        | Object Array | []                | current tokens               |

Object fields(appCredential)

| name          | type    | example               | description           |
| ------------- | ------- | --------------------- | --------------------- |
| appKey        | String  | 4e827ce7c46           | -                     |
| appSecret     | String  | 41357028d203041c53c14 | -                     |
| maxTokenCount | Integer | 10                    | max alive token count |
| validTime     | Integer | 86400                 | ttl in seconds        |

Object fields(token)

| name       | type   | example                       | description                            |
| ---------- | ------ | ----------------------------- | -------------------------------------- |
| tokenValue | String | ac876821-f1f3-44a2-b05a-f02ce | token value                            |
| status     | String | alive                         | token status: alive/expired            |
| expire     | Long   | 1543200293694                 | token expire timestamp in milliseconds |

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

## Add token

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

| name       | type   | example                       | description                       |
| ---------- | ------ | ----------------------------- | --------------------------------- |
| tokenValue | String | ac876821-f1f3-44a2-b05a-f02ce | token value                       |
| status     | String | alive                         | token status: alive/expired       |
| expire     | Long   | 1543200293694                 | token expire time in milliseconds |

- Response example

```json
{
    "tokenValue": "70fd8316-c9b2-4b90-957a-e3b07fb1c3cb",
    "status": "alive",
    "expire": 1543202373785
}
```

## Remove token

> Tip: It's unnecessary to delete expired tokens since Cerberus will do this for you.

- Request uri

  GET /api/app/token/delete?token=qqqwwweeerrr

- Request parameters

| name  | type   | example      | description |
| :---- | :----- | ------------ | :---------- |
| token | String | qqqwwweeerrr | token value |

- Response example

```json
{
    "success": true
}
```

# Business API

## Authentication

1. Let's say you already have an app with the following properties:
   - **appKey**: testApp1
   - **appSecret**: 111222333xxxyyyzzz
   - **token**: qqqwwweeerrr

2. With the system property:
   - **timeStamp**: 1552632509159(milliseconds)

> Tip: Use the runtime timeStamp value because Cerberus server only accepts requests that are sent within 1 minute

3. The API you'd like to visit is:

> https://apiproxy.ctrip.com/apiproxy/gateway/test

4. Concatenate strings in the following order

   ```java
   String result = concat(uri.toLowerCase, token, timeStamp, secret);
   // result = "/apiproxy/gateway/testqqqwwweeerrr1552632509159111222333xxxyyyzzz";
   ```

5. Invoke MD5 on the result string (charset: utf-8, lowercase, length:16), we'll get:

   > sign = 2aebf9bd91ffa82a

6. Add sign, timeStamp, token in the query string of your request, finally the request would be:

   > https://apiproxy.ctrip.com/apiproxy/gateway/test?sign=2aebf9bd91ffa82a&timeStamp=1552632509159&token=qqqwwweeerrr

## Reqeuest address

Cerberus servers are deployed @Shanghai China, but we've got entrances all over the world to achieve lower latency and higher stability. All of the following hosts target the same Cerberus cluster. You may choose depending on your runtime latency.

| host                   | scheme     | target user             | description                                                  |
| ---------------------- | ---------- | ----------------------- | ------------------------------------------------------------ |
| apiproxy.ctrip.com     | http&https | all(recommended)        | Domestic DNS configuration in China; Special Node in Hong Kong; Akamai Accelerator |
| intl-api.ctrip.com     | http&https | outside mainland, China | Overseas DNS configuration only; Akamai Accelerator          |
| hkproxy.ctrip.com      | http&https | Hong Kong               | Special Node in Hong Kong                                    |
| alliance-api.ctrip.com | http&https | large traffic           | No Acceleration, for clients with large traffic              |

