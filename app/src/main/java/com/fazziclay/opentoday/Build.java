package com.fazziclay.opentoday;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.CustomBuildConfig;
import com.fazziclay.opentoday.app.FeatureFlag;

import java.util.Arrays;

// I im happy!
public class Build {
    // Setting this :)
    public static final BuildDebugStatus DEBUG_STATUS = BuildDebugStatus.AUTOMATIC;
    public static final BuildLogsStatus LOGS_STATUS = BuildLogsStatus.OFF;
    public static final boolean IS_SECRET_SETTINGS_AVAILABLE = true;
    public static final boolean IS_SHADOW_CUSTOM_BUILD_CONFIG = false; // normally is FALSE
    public static final FeatureFlag[] INITIAL_FEATURE_FLAGS = {
            //FeatureFlag.TOOLBAR_DEBUG,
            //FeatureFlag.ITEM_SLEEP_TIME,
            //FeatureFlag.ITEM_DEBUG_TICK_COUNTER,
            //FeatureFlag.DISABLE_DEBUG_MODE_NOTIFICATION,
    };
    public static final boolean PROFILERS = false; // normally is FALSE (long uses profilers causes crashes)


    // work code
    public static boolean isDebug() {
        return switch (DEBUG_STATUS) {
            case TRUE -> true;
            case FALSE -> false;
            case AUTOMATIC -> CustomBuildConfig.DEBUG;
        };
    }

    public static boolean isLogs() {
        return LOGS_STATUS != BuildLogsStatus.OFF;
    }

    public static boolean isLogsSave() {
        return LOGS_STATUS == BuildLogsStatus.ON_WITH_FILE;
    }

    public static boolean isSecretSettingAvailable() {
        return IS_SECRET_SETTINGS_AVAILABLE;
    }

    @NonNull
    public static String getBuildDebugReport() {
        return "DebugStatus=" + DEBUG_STATUS +
                "; LogsStatus=" + LOGS_STATUS +
                "; SecretSettings=" + IS_SECRET_SETTINGS_AVAILABLE +
                "; ShadowCustomBuildConfig=" + IS_SHADOW_CUSTOM_BUILD_CONFIG +
                "; InitialFeatureFlags=" + Arrays.toString(INITIAL_FEATURE_FLAGS) +
                "; Profilers=" + PROFILERS;
    }

    public static boolean isProfilersEnabled() {
        return PROFILERS;
    }

    public enum BuildDebugStatus {
        TRUE,
        FALSE,
        AUTOMATIC
    }

    public enum BuildLogsStatus {
        OFF,
        ON,
        ON_WITH_FILE
    }
}
