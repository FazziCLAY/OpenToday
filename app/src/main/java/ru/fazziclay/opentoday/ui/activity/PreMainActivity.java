package ru.fazziclay.opentoday.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.util.DebugUtil;

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

        // TODO: 30.09.2022 | create lastTab field in settings?
        startActivity(new Intent(this, MainActivity.class).putExtra("tabId", App.get(this).getItemManager().getMainTab().getId().toString()));
    }
}
