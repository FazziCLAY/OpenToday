package com.fazziclay.opentoday.app;

import com.fazziclay.opentoday.BuildConfig;

public class CustomBuildConfig {
    public static final boolean SHADOW_BUILD_CONFIG = false;

    public static final String VERSION_NAME;
    public static final int VERSION_CODE;
    public static final String APPLICATION_ID;
    public static final boolean DEBUG;

    static {
        if (SHADOW_BUILD_CONFIG) {
            VERSION_CODE = 0;
            VERSION_NAME = "0.0 Shadow";
            APPLICATION_ID = "com.fazziclay.opentoday.shadow";
            DEBUG = true;

        } else {
            /**/
            VERSION_CODE = BuildConfig.VERSION_CODE;
            VERSION_NAME = BuildConfig.VERSION_NAME;
            APPLICATION_ID = BuildConfig.APPLICATION_ID;
            DEBUG = BuildConfig.DEBUG;
            /**/
        }
    }
}
