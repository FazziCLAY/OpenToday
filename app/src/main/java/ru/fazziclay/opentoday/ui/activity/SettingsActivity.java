package ru.fazziclay.opentoday.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.app.settings.SettingsManager;
import ru.fazziclay.opentoday.databinding.ActivitySettingsBinding;
import ru.fazziclay.opentoday.util.SpinnerHelper;

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

        SpinnerHelper<Integer> themeSpinnerHelp = new SpinnerHelper<>(
                new String[]{
                        getString(R.string.settings_theme_system),
                        getString(R.string.settings_theme_light),
                        getString(R.string.settings_theme_night)
                },
                new Integer[]{
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
                        AppCompatDelegate.MODE_NIGHT_NO,
                        AppCompatDelegate.MODE_NIGHT_YES
                }
        );
        binding.themeSpinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_expandable_list_item_1, themeSpinnerHelp.getNames()));
        binding.themeSpinner.setSelection(themeSpinnerHelp.getPosition(settingsManager.getTheme()));
        binding.themeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int t = themeSpinnerHelp.getValue(position);
                AppCompatDelegate.setDefaultNightMode(t);
                settingsManager.setTheme(t);
                settingsManager.save();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
}