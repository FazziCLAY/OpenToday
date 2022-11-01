package ru.fazziclay.opentoday.ui.fragment;

import static ru.fazziclay.opentoday.util.InlineUtil.viewClick;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.app.receiver.QuickNoteReceiver;
import ru.fazziclay.opentoday.app.settings.SettingsManager;
import ru.fazziclay.opentoday.databinding.FragmentSettingsBinding;
import ru.fazziclay.opentoday.util.SimpleSpinnerAdapter;

public class SettingsFragment extends Fragment {
    public static SettingsFragment create() {
        return new SettingsFragment();
    }

    private FragmentSettingsBinding binding;
    private SettingsManager settingsManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.binding = FragmentSettingsBinding.inflate(getLayoutInflater());
        this.settingsManager = App.get(requireContext()).getSettingsManager();
        setupThemeSpinner();

        binding.quickNoteCheckbox.setChecked(settingsManager.isQuickNoteNotification());
        viewClick(binding.quickNoteCheckbox, () -> {
            settingsManager.setQuickNoteNotification(binding.quickNoteCheckbox.isChecked());
            if (settingsManager.isQuickNoteNotification()) {
                QuickNoteReceiver.sendQuickNoteNotification(requireContext());
            } else {
                QuickNoteReceiver.cancelQuickNoteNotification(requireContext());
            }
            settingsManager.save();
        });

        binding.parseTimeFromQuickNote.setChecked(settingsManager.isParseTimeFromQuickNote());
        viewClick(binding.parseTimeFromQuickNote, () -> {
            settingsManager.setParseTimeFromQuickNote(binding.parseTimeFromQuickNote.isChecked());
            settingsManager.save();
        });


        binding.minimizeGrayColor.setChecked(settingsManager.isMinimizeGrayColor());
        viewClick(binding.minimizeGrayColor, () -> {
            settingsManager.setMinimizeGrayColor(binding.minimizeGrayColor.isChecked());
            settingsManager.save();
        });

        binding.trimItemNamesOnEdit.setChecked(settingsManager.isTrimItemNamesOnEdit());
        viewClick(binding.trimItemNamesOnEdit, () -> {
            settingsManager.setTrimItemNamesOnEdit(binding.trimItemNamesOnEdit.isChecked());
            settingsManager.save();
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return binding.getRoot();
    }

    private void setupThemeSpinner() {
        SimpleSpinnerAdapter<Integer> themeSpinnerAdapter = new SimpleSpinnerAdapter<Integer>(requireContext())
                .add(requireContext().getString(R.string.settings_theme_system), AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
                .add(requireContext().getString(R.string.settings_theme_light), AppCompatDelegate.MODE_NIGHT_NO)
                .add(requireContext().getString(R.string.settings_theme_night), AppCompatDelegate.MODE_NIGHT_YES);

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
