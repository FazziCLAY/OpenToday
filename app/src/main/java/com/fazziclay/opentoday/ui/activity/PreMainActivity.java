package com.fazziclay.opentoday.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;

import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.FeatureFlag;
import com.fazziclay.opentoday.util.DebugUtil;

public class PreMainActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        run();
        finish();
    }

    private void run() {
        DebugUtil.sleep(App.DEBUG_MAIN_ACTIVITY_START_SLEEP);
        if (App.DEBUG_TEST_EXCEPTION_ONCREATE_MAINACTIVITY) {
            throw new RuntimeException("This is cute Runtime Exception :)", new RuntimeException("DEBUG_TEST_EXCEPTION_ONCREATE_MAINACTIVITY is enabled :)"));
        }
        if (!(App.DEBUG_MAIN_ACTIVITY == MainActivity.class || App.DEBUG_MAIN_ACTIVITY == null)) {
            startActivity(new Intent(this, App.DEBUG_MAIN_ACTIVITY));
            return;
        }
        final App app = App.get(this);
        AppCompatDelegate.setDefaultNightMode(app.getSettingsManager().getTheme());
        if (app.isFeatureFlag(FeatureFlag.SHOW_APP_STARTUP_TIME_IN_PREMAIN_ACTIVITY)) {
            StringBuilder text = new StringBuilder("App startup time:\n");
            text.append(app.getAppStartupTime()).append("ms");
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        }
        startActivity(new Intent(this, MainActivity.class));
    }
}
