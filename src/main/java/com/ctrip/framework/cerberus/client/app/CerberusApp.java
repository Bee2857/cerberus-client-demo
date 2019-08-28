package com.ctrip.framework.cerberus.client.app;

import com.ctrip.framework.cerberus.client.entity.Token;
import com.ctrip.framework.cerberus.client.http.HttpClient;
import com.ctrip.framework.cerberus.client.http.HttpRequest;
import com.ctrip.framework.cerberus.client.utils.Args;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import java.util.concurrent.atomic.AtomicReference;

public class CerberusApp {
    private final String appKey;
    private final String appSecret;
    private AtomicReference<Token> currentToken = new AtomicReference<>();

    public CerberusApp(String appKey, String appSecret) {
        Args.notBlank(appKey, "appKey");
        Args.notBlank(appSecret, "appSecret");
        this.appKey = appKey;
        this.appSecret = appSecret;
    }

    public void setToken(Token token) {
        Args.notNull(token, "token");
        currentToken.set(token);
    }

    public Token getToken() {
        return currentToken.get();
    }

    public String getAppKey() {
        return appKey;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public String request(HttpRequest request) throws Exception {
        if (getToken() == null || getToken().getTokenValue() == null) {
            throw new Exception("request failed, null token for app: " + getAppKey());
        }

        String timeStamp = String.valueOf(System.currentTimeMillis());
        String sign = request.getUri().toLowerCase() + getToken().getTokenValue() + timeStamp + getAppSecret();
        sign = Hashing.md5().hashString(sign, Charsets.UTF_8).toString().substring(8, 24);

        request.addParam("sign", sign);
        request.addParam("timeStamp", timeStamp);
        request.addParam("token", getToken().getTokenValue());

        return HttpClient.execute(request);
    }
}
