package com.ctrip.framework.cerberus.client.app;

import com.ctrip.framework.cerberus.client.entity.App;
import com.ctrip.framework.cerberus.client.entity.Token;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

public class AppTokenManager extends Thread {
    private static final Logger logger = LoggerFactory.getLogger(AppTokenManager.class);
    public static final String ALIVE = "alive";

    private CopyOnWriteArrayList<CerberusApp> apps = new CopyOnWriteArrayList<>();

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private long updateInterval;

    public AppTokenManager() {
        this(60 * 60 * 1000L);
    }

    public AppTokenManager(long updateInterval) {
        this.updateInterval = updateInterval;
        this.setDaemon(true);
    }

    public void addApp(CerberusApp app) throws Exception {
        apps.add(doUpdate(app));
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted()) {
                sleep(updateInterval);
                try {
                    doUpdate();
                } catch (Exception e) {
                    logger.error("Encounter an error as polling.", e);
                }
            }
        } catch (InterruptedException e) {
            logger.warn("Encounter an error while poller sleeping.", e);
        }
    }

    private void doUpdate() {
        apps.forEach(app -> {
            try {
                doUpdate(app);
            } catch (Exception e) {
                logger.warn("update failed for app: " + app.getAppKey(), e);
            }
        });
    }

    private CerberusApp doUpdate(CerberusApp cerberusApp) throws Exception {
        App app = AppClient.getApp(cerberusApp);
        List<Token> availableTokens = new ArrayList<>();
        for (Token t : app.getTokens()) {
            if (ALIVE.equals(t.getStatus()) && t.getExpire() > System.currentTimeMillis()) {
                availableTokens.add(t);
            }
        }
        availableTokens.sort((o1, o2) -> o2.getExpire().compareTo(o1.getExpire()));
        cerberusApp.setToken(availableTokens.get(0));
        return cerberusApp;
    }
}
