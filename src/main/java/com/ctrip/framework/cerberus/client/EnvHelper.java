package com.ctrip.framework.cerberus.client;

public enum EnvHelper {
    PRO("pro", "https://", "cerberus.ctrip.com");

    private static EnvHelper env = PRO;
    private String name;
    private String scheme;
    private String domain;

    EnvHelper(String name, String scheme, String domain) {
        this.name = name;
        this.scheme = scheme;
        this.domain = domain;
    }

    public String getName() {
        return name;
    }

    public String getDomain() {
        return domain;
    }

    public String getScheme() {
        return scheme;
    }

    public static EnvHelper getEnv() {
        return env;
    }

    public static void setEnv(EnvHelper env) {
        EnvHelper.env = env;
    }
}
