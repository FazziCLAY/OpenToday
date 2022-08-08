package ru.fazziclay.opentoday.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.widget.AdapterView;

import androidx.appcompat.app.AppCompatDelegate;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.app.settings.SettingsManager;
import ru.fazziclay.opentoday.databinding.ActivitySettingsBinding;
import ru.fazziclay.opentoday.util.SimpleSpinnerAdapter;

public class DialogSettings {
    private final Activity activity;
    private final Dialog dialog;
    private final ActivitySettingsBinding binding;
    private final SettingsManager settingsManager;

    public DialogSettings(Activity activity) {
        this.activity = activity;
        this.dialog = new Dialog(activity, android.R.style.ThemeOverlay_Material);
        this.binding = ActivitySettingsBinding.inflate(activity.getLayoutInflater());
        this.settingsManager = App.get(activity).getSettingsManager();
        setupThemeSpinner();
    }

    private void setupThemeSpinner() {
        SimpleSpinnerAdapter<Integer> themeSpinnerAdapter = new SimpleSpinnerAdapter<Integer>(activity)
                .add(activity.getString(R.string.settings_theme_system), AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                .add(activity.getString(R.string.settings_theme_light), AppCompatDelegate.MODE_NIGHT_NO)
                .add(activity.getString(R.string.settings_theme_night), AppCompatDelegate.MODE_NIGHT_YES);

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

    public View getView() {
        return binding.getRoot();
    }

    public void show() {
        dialog.setContentView(getView());
        dialog.show();
    }
}
