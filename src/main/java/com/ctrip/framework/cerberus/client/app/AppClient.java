package com.ctrip.framework.cerberus.client.app;

import com.ctrip.framework.cerberus.client.EnvHelper;
import com.ctrip.framework.cerberus.client.entity.App;
import com.ctrip.framework.cerberus.client.entity.Token;
import com.ctrip.framework.cerberus.client.http.HttpClient;
import com.ctrip.framework.cerberus.client.http.HttpMethod;
import com.ctrip.framework.cerberus.client.http.HttpRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import org.apache.http.entity.StringEntity;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

public class AppClient {
    private static final String APP_GET_URI = "/api/app/getApp";
    private static final String TOKEN_NEW_URI = "/api/app/createToken";
    private static final String TOKEN_DELETE_URI = "/api/app/token/delete";
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static App getApp(CerberusApp app) throws Exception {
        try {
            HttpRequest.Buidler builer = new HttpRequest.Buidler()
                    .method(HttpMethod.GET)
                    .scheme(EnvHelper.getEnv().getScheme())
                    .domain(EnvHelper.getEnv().getDomain())
                    .uri(APP_GET_URI)
                    .addParam("appKey", app.getAppKey())
                    .addParam("type", "detail");
            addSignParams(app, APP_GET_URI, builer);
            App[] result = objectMapper.readValue(HttpClient.execute(builer.build()), new TypeReference<App[]>() {
            });
            return result[0];
        } catch (Exception e) {
            throw new Exception("get app failed, appKey: " + app.getAppKey(), e);
        }
    }

    public static Token newToken(CerberusApp app) throws Exception {
        try {
            HttpRequest.Buidler builer = new HttpRequest.Buidler()
                    .method(HttpMethod.POST)
                    .scheme(EnvHelper.getEnv().getScheme())
                    .domain(EnvHelper.getEnv().getDomain())
                    .uri(TOKEN_NEW_URI)
                    .addHeader("content-type", "application/json")
                    .entity(new StringEntity("{\"appKey\": \"" + app.getAppKey() + "\"}"));
            addSignParams(app, TOKEN_NEW_URI, builer);
            return objectMapper.readValue(HttpClient.execute(builer.build()), Token.class);
        } catch (Exception e) {
            throw new Exception("new token failed for app: " + app.getAppKey(), e);
        }
    }

    public static void deleteToken(CerberusApp app, Token token) throws Exception {
        try {


            HttpRequest.Buidler builer = new HttpRequest.Buidler()
                    .method(HttpMethod.GET)
                    .scheme(EnvHelper.getEnv().getScheme())
                    .domain(EnvHelper.getEnv().getDomain())
                    .uri(TOKEN_DELETE_URI)
                    .addParam("token", token.getTokenValue());
            addSignParams(app, TOKEN_DELETE_URI, builer);
            HttpClient.execute(builer.build());

        } catch (Exception e) {
            throw new Exception("delete token failed, appKey: " + app.getAppKey() + " token: " + token.getTokenValue(), e);
        }
    }

    private static void addSignParams(CerberusApp app, String uri, HttpRequest.Buidler buidler) {
        String timeStamp = String.valueOf(System.currentTimeMillis());
        String sign = uri.toLowerCase() + app.getAppKey() + timeStamp + app.getAppSecret();
        sign = Hashing.md5().hashString(sign, Charsets.UTF_8).toString().substring(8, 24);
        buidler.addParam("sign", sign);
        buidler.addParam("timeStamp", timeStamp);
        buidler.addParamIfAbsent("appKey", app.getAppKey());
    }
}
