package com.ctrip.framework.cerberus.client.http;

import com.ctrip.framework.cerberus.client.utils.Args;
import org.apache.http.HttpEntity;

import java.net.URL;
import java.util.*;

public class HttpRequest {
    private String scheme;
    private HttpMethod method;
    private String domain;
    private String uri;
    private HttpEntity entity;
    private Map<String, List<String>> params;
    private Map<String, String> headers;

    public HttpRequest(String scheme, HttpMethod method, String domain, String uri, HttpEntity entity, Map<String, List<String>> params, Map<String, String> headers) {
        Args.notNull(scheme, "scheme");
        Args.notNull(method, "method");
        Args.notNull(domain, "domain");
        Args.notNull(uri, "uri");
        if (method.isEntityRequired()) {
            Args.notNull(entity, "entity");
            this.entity = entity;
        }
        this.scheme = scheme;
        this.method = method;
        this.domain = domain;
        this.uri = uri;
        this.params = params;
        this.headers = headers;
    }

    public static class Buidler {
        private String scheme;
        private HttpMethod method;
        private String domain;
        private String uri;
        private HttpEntity entity;
        private Map<String, List<String>> params;
        private Map<String, String> headers;

        public Buidler scheme(String scheme) {
            this.scheme = scheme;
            return this;
        }

        public Buidler url(URL url) {
            this.scheme = url.getProtocol() + "://";
            this.domain = url.getAuthority();
            String path = url.getPath();
            if (path == null || path.isEmpty()) path = "/";
            this.uri = path;
            String query = url.getQuery();
            if (query != null && !query.isEmpty()) {
                Arrays.stream(query.split("&")).forEach(s -> {
                    String[] kv = s.split("=", 2);
                    if (kv.length == 2) {
                        addParam(kv[0], kv[1]);
                    } else {
                        throw new IllegalArgumentException("illegal query:" + s);
                    }
                });
            }
            return this;
        }

        public Buidler method(HttpMethod method) {
            this.method = method;
            return this;
        }

        public Buidler domain(String domain) {
            this.domain = domain;
            return this;
        }

        public Buidler uri(String uri) {
            this.uri = uri;
            return this;
        }

        public Buidler entity(HttpEntity entity) {
            this.entity = entity;
            return this;
        }

        public Buidler headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Buidler params(Map<String, List<String>> params) {
            this.params = params;
            return this;
        }

        public Buidler addHeader(String key, String value) {
            if (headers == null) headers = new HashMap<>();
            headers.put(key, value);
            return this;
        }

        public Buidler addHeaderIfAbsent(String key, String value) {
            if (headers == null) headers = new HashMap<>();
            headers.putIfAbsent(key, value);
            return this;
        }

        public Buidler addParam(String key, String value) {
            if (params == null) params = new HashMap<>();
            List<String> values = params.get(key);
            if (values == null) {
                values = new ArrayList<>();
                params.put(key, values);
            }
            values.add(value);
            return this;
        }

        public Buidler addParamIfAbsent(String key, String value) {
            if (params == null) params = new HashMap<>();
            List<String> values = params.get(key);
            if (values == null) {
                values = new ArrayList<>();
                params.put(key, values);
                values.add(value);
            }
            return this;
        }

        public HttpRequest build() {
            return new HttpRequest(scheme, method, domain, uri, entity, params, headers);
        }
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public HttpEntity getEntity() {
        return entity;
    }

    public void setEntity(HttpEntity entity) {
        this.entity = entity;
    }

    public Map<String, List<String>> getParams() {
        return params;
    }

    public void setParams(Map<String, List<String>> params) {
        this.params = params;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void addHeader(String key, String value) {
        if (headers == null) headers = new HashMap<>();
        headers.put(key, value);
    }

    public void addHeaderIfAbsent(String key, String value) {
        if (headers == null) headers = new HashMap<>();
        headers.putIfAbsent(key, value);
    }

    public void addParam(String key, String value) {
        if (params == null) params = new HashMap<>();
        List<String> values = params.get(key);
        if (values == null) {
            values = new ArrayList<>();
            params.put(key, values);
        }
        values.add(value);
    }

    public void addParamIfAbsent(String key, String value) {
        if (params == null) params = new HashMap<>();
        List<String> values = params.get(key);
        if (values == null) {
            values = new ArrayList<>();
            params.put(key, values);
            values.add(value);
        }
    }
}
