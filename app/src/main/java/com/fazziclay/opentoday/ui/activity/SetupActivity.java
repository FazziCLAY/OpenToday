package com.fazziclay.opentoday.ui.activity;

import static com.fazziclay.opentoday.util.InlineUtil.viewClick;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.databinding.ActivitySetupBinding;

public class SetupActivity extends AppCompatActivity {
    private ActivitySetupBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            getSupportActionBar().hide();
        } catch (Exception ignored) {}

        binding = ActivitySetupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewClick(binding.done, this::done);
    }

    private void done() {
        App app = App.get(this);
        app.getSettingsManager().setTelemetry(binding.telemetry.isChecked());
        app.getSettingsManager().save();

        setupDone();
        startMain();
    }

    private void setupDone() {
        getSharedPreferences(App.SHARED_NAME, MODE_PRIVATE).edit().putBoolean(App.SHARED_KEY_IS_SETUP_DONE, true).apply();
    }

    private void startMain() {
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }
}
