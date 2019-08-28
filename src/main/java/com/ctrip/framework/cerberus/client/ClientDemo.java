package com.ctrip.framework.cerberus.client;

import com.ctrip.framework.cerberus.client.app.AppTokenManager;
import com.ctrip.framework.cerberus.client.app.CerberusApp;
import com.ctrip.framework.cerberus.client.http.HttpMethod;
import com.ctrip.framework.cerberus.client.http.HttpRequest;

import java.net.URL;

public class ClientDemo {
    public static void main(String[] args) throws Exception {
        EnvHelper.setEnv(EnvHelper.PRO);
        AppTokenManager appTokenManager = new AppTokenManager();
        CerberusApp app = new CerberusApp("272ca51470095b67", "22ed29d71f24c14b311f1d598579ed72607fca294478e6c71ec0d83ae2f9a869");
        appTokenManager.addApp(app);
        appTokenManager.start();
        String res = app.request(
                new HttpRequest.Buidler()
                        .method(HttpMethod.GET)
                        .url(new URL("http://apiproxy.ctrip.com/apiproxy/gateway/test"))
                        .build()
        );
        if ("4008206666".equals(res)) {
            System.out.println("success!");
        }
    }
}
