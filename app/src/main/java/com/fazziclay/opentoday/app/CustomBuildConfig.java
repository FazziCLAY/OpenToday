package com.fazziclay.opentoday.app;

import com.fazziclay.opentoday.Build;
import com.fazziclay.opentoday.BuildConfig;

public class CustomBuildConfig {
    public static final boolean SHADOW_BUILD_CONFIG = Build.IS_SHADOW_CUSTOM_BUILD_CONFIG;

    public static final String VERSION_NAME;
    public static final int VERSION_CODE;
    public static final long VERSION_RELEASE_TIME;
    public static final String VERSION_BRANCH;
    public static final String APPLICATION_ID;
    public static final boolean DEBUG;

    static {
        if (SHADOW_BUILD_CONFIG) {
            VERSION_CODE = 0;
            VERSION_NAME = "0.0 Shadow";
            VERSION_RELEASE_TIME = System.currentTimeMillis() / 1000;
            VERSION_BRANCH = "This is a shadow BuildConfig";
            APPLICATION_ID = "com.fazziclay.opentoday.shadow";
            DEBUG = true;

        } else {
            /**/
            VERSION_CODE = BuildConfig.VERSION_CODE;
            VERSION_NAME = BuildConfig.VERSION_NAME;
            VERSION_RELEASE_TIME = BuildConfig.VERSION_RELEASE_TIME;
            VERSION_BRANCH = BuildConfig.VERSION_BRANCH;
            APPLICATION_ID = BuildConfig.APPLICATION_ID;
            DEBUG = BuildConfig.DEBUG;
            /**/
        }
    }
}
