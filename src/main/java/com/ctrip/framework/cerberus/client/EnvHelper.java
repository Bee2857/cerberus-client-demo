package com.ctrip.framework.cerberus.client;

public enum EnvHelper {
    TEST("test", "https://", "cerberus-fws.ctripqa.com"),
    PRO("pro", "https://", "cerberus.ctrip.com"),
    PRO_FRAAWS("pro_fraaws", "https://", "fra-cerberus.trip.com");

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
