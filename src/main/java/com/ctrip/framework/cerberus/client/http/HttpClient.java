package com.ctrip.framework.cerberus.client.http;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;


public class HttpClient {
    private static final CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(newSSLSocketFactory()).build();
    private static final int DEFAULT_CONNECT_TIMEOUT = 10000;
    private static final int DEFAULT_READ_TIMEOUT = 30000;

    private static final Logger logger = LoggerFactory.getLogger(HttpClient.class);

    private static SSLConnectionSocketFactory newSSLSocketFactory() {
        try {
            /**
             * SSL and TLS 1.x are considered insecure, so the minimum required TLS version is 1.2.
             * You can flexibly configure the TLS version according to your JDK environment.
             * JDK6: Needs to be upgraded to JDK7 or above
             * JDK7: Recommended to configure as TLSv1.2
             * JDK8+: Recommended to configure as TLSv1.2 and TLSv1.3, if the version not supports TLSv1.3, it can also be configured as TLSv1.2
             */
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    SSLContexts.createDefault(),
                    new String[]{"TLSv1.2", "TLSv1.3"},
                    null,
                    SSLConnectionSocketFactory.getDefaultHostnameVerifier());

            return sslsf;
        } catch (Exception e) {
            throw new RuntimeException("init http client error.", e);
        }
    }

    public static String execute(HttpMethod method, String url, Map<String, String> headers, HttpEntity entity) throws Exception {
        CloseableHttpResponse response = null;

        try {
            HttpUriRequest request;
            RequestConfig requestConfig = RequestConfig
                    .custom()
                    .setSocketTimeout(DEFAULT_READ_TIMEOUT)
                    .setConnectTimeout(DEFAULT_CONNECT_TIMEOUT)
                    .build();
            switch (method) {
                case GET:
                    request = new HttpGet(url);
                    ((HttpGet) request).setConfig(requestConfig);
                    break;
                case POST:
                    request = new HttpPost(url);
                    ((HttpPost) request).setConfig(requestConfig);
                    ((HttpPost) request).setEntity(entity);
                    break;
                case PUT:
                    request = new HttpPut(url);
                    ((HttpPut) request).setConfig(requestConfig);
                    ((HttpPut) request).setEntity(entity);
                    break;
                default:
                    request = RequestBuilder.create(method.name()).setConfig(requestConfig).setUri(url).build();
                    break;
            }
            if (headers != null) {
                headers.forEach(request::addHeader);
            }
            response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            String result = EntityUtils.toString(response.getEntity(), Consts.UTF_8);
            if (statusCode != 200) {
                throw new Exception("http status code: " + statusCode + ", url: " + url + ", response: " + result);
            }
            return result == null ? "" : result;
        } finally {
            try {
                if (response != null) {
                    EntityUtils.consume(response.getEntity());
                    response.close();
                }
            } catch (IOException e) {
                logger.error("close http client failed", e);
            }
        }
    }

    public static String execute(HttpRequest req) throws Exception {
        StringBuilder sb = new StringBuilder();
        if (req.getParams() != null) {
            req.getParams().forEach((key, values) -> {
                values.forEach(value -> sb.append("&").append(key).append("=").append(value));
            });
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(0).insert(0, "?");
        }
        String url = sb.insert(0, req.getUri()).insert(0, req.getDomain()).insert(0, req.getScheme()).toString();
        return execute(req.getMethod(), url, req.getHeaders(), req.getEntity());
    }
}
