package com.ctrip.framework.cerberus.client;

import com.ctrip.framework.cerberus.client.app.AppTokenManager;
import com.ctrip.framework.cerberus.client.app.CerberusApp;
import com.ctrip.framework.cerberus.client.http.HttpMethod;
import com.ctrip.framework.cerberus.client.http.HttpRequest;

import java.net.URL;

public class ClientDemo {
    public static void main(String[] args) throws Exception {
        accessSHA();
//        accessFRA();
    }

    public static void accessSHA() throws Exception {
        /**
         *  for PRO users:
         *  1. set cerberus env to EnvHelper.PRO
         *  2. set appKey "272ca51470095b67" and appSecret "22ed29d71f24c14b311f1d598579ed72607fca294478e6c71ec0d83ae2f9a869"
         */
        // config and init. This is a one-time operation
        EnvHelper.setEnv(EnvHelper.PRO);//set cerberus env to PRO
        String appKey = "272ca51470095b67";
        String appSecret = "22ed29d71f24c14b311f1d598579ed72607fca294478e6c71ec0d83ae2f9a869";
        AppTokenManager appTokenManager = new AppTokenManager();
        CerberusApp app = new CerberusApp(appKey, appSecret);
        appTokenManager.addApp(app);
        appTokenManager.start();

        // send business request
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

    public static void accessFRA() throws Exception {
        /**
         *  for PRO_FRAAWS users:
         *  1. set cerberus env to EnvHelper.PRO
         *  2. set appKey "d74ee5515e3606a8" and appSecret "07d9b020f898e32601440a18986dbc8499085e599e8ed1eb861dfee1abf83125"
         */
        // config and init. This is a one-time operation
        EnvHelper.setEnv(EnvHelper.PRO_FRAAWS);//set cerberus env to PRO_FRAAWS
        String appKey = "d74ee5515e3606a8";
        String appSecret = "07d9b020f898e32601440a18986dbc8499085e599e8ed1eb861dfee1abf83125";
        AppTokenManager appTokenManager = new AppTokenManager();
        CerberusApp app = new CerberusApp(appKey, appSecret);
        appTokenManager.addApp(app);
        appTokenManager.start();

        // send business request
        String res = app.request(
                new HttpRequest.Buidler()
                        .method(HttpMethod.GET)
                        .url(new URL("http://fra-api.trip.com/gateway/test"))
                        .build()
        );
        if ("4008206666".equals(res)) {
            System.out.println("success!");
        }
    }
}
