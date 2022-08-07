package ru.fazziclay.opentoday.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.app.settings.SettingsManager;
import ru.fazziclay.opentoday.databinding.ActivitySettingsBinding;
import ru.fazziclay.opentoday.util.SimpleSpinnerAdapter;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;
    private SettingsManager settingsManager;

    public static Intent createLaunchIntent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setTitle(getString(R.string.settings_title));

        settingsManager = App.get(this).getSettingsManager();
        setupThemeSpinner();
    }

    private void setupThemeSpinner() {
        SimpleSpinnerAdapter<Integer> themeSpinnerAdapter = new SimpleSpinnerAdapter<Integer>(this)
                .add(getString(R.string.settings_theme_system), AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                .add(getString(R.string.settings_theme_light), AppCompatDelegate.MODE_NIGHT_NO)
                .add(getString(R.string.settings_theme_night), AppCompatDelegate.MODE_NIGHT_YES);

        binding.themeSpinner.setAdapter(themeSpinnerAdapter);
        binding.themeSpinner.setSelection(themeSpinnerAdapter.getValuePosition(settingsManager.getTheme()));
        binding.themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int t = themeSpinnerAdapter.getItem(position);
                AppCompatDelegate.setDefaultNightMode(t);
                settingsManager.setTheme(t);
                settingsManager.save();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
}