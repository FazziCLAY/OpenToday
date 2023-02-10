package com.fazziclay.opentoday.gui.fragment;

import static com.fazziclay.opentoday.util.InlineUtil.viewClick;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.ColorHistoryManager;
import com.fazziclay.opentoday.app.FeatureFlag;
import com.fazziclay.opentoday.app.ImportWrapper;
import com.fazziclay.opentoday.app.items.ItemManager;
import com.fazziclay.opentoday.app.items.item.ItemsRegistry;
import com.fazziclay.opentoday.app.items.tab.Tab;
import com.fazziclay.opentoday.app.receiver.QuickNoteReceiver;
import com.fazziclay.opentoday.app.settings.SettingsManager;
import com.fazziclay.opentoday.databinding.ExportBinding;
import com.fazziclay.opentoday.databinding.FragmentSettingsBinding;
import com.fazziclay.opentoday.gui.dialog.DialogSelectItemType;
import com.fazziclay.opentoday.util.SimpleSpinnerAdapter;

import org.json.JSONException;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class SettingsFragment extends Fragment {
    public static SettingsFragment create() {
        return new SettingsFragment();
    }

    private FragmentSettingsBinding binding;
    private App app;
    private SettingsManager settingsManager;
    private ColorHistoryManager colorHistoryManager;
    private long easterEggLastClick = 0;
    private int easterEggCounter = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = App.get(requireContext());
        settingsManager = app.getSettingsManager();
        colorHistoryManager = app.getColorHistoryManager();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater);
        setupView();
        return binding.getRoot();
    }

    private void setupView() {
        setupThemeSpinner();
        setupFirstDayOfWeekSpinner();

        // QuickNote
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

        // Parse time from quick note
        binding.parseTimeFromQuickNote.setChecked(settingsManager.isParseTimeFromQuickNote());
        viewClick(binding.parseTimeFromQuickNote, () -> {
            settingsManager.setParseTimeFromQuickNote(binding.parseTimeFromQuickNote.isChecked());
            settingsManager.save();
        });

        // Minimize gray color
        binding.minimizeGrayColor.setChecked(settingsManager.isMinimizeGrayColor());
        viewClick(binding.minimizeGrayColor, () -> {
            settingsManager.setMinimizeGrayColor(binding.minimizeGrayColor.isChecked());
            settingsManager.save();
        });

        // Trim item names in Editor
        binding.trimItemNamesOnEdit.setChecked(settingsManager.isTrimItemNamesOnEdit());
        viewClick(binding.trimItemNamesOnEdit, () -> {
            settingsManager.setTrimItemNamesOnEdit(binding.trimItemNamesOnEdit.isChecked());
            settingsManager.save();
        });

        // Lock color history
        viewClick(binding.colorHistoryTitle, this::experimentalFeaturesInteract);
        binding.colorHistoryLocked.setChecked(colorHistoryManager.isLocked());
        viewClick(binding.colorHistoryLocked, () -> {
            colorHistoryManager.setLocked(binding.colorHistoryLocked.isChecked());
            colorHistoryManager.save();
        });

        // Export
        viewClick(binding.export, () -> showExportDialog(requireActivity(), settingsManager, colorHistoryManager));

        // Is telemetry
        binding.isTelemetry.setChecked(settingsManager.isTelemetry());
        viewClick(binding.isTelemetry, () -> {
            final boolean is = binding.isTelemetry.isChecked();
            settingsManager.setTelemetry(is);
            settingsManager.save();
            app.getTelemetry().setEnabled(is);
        });

        binding.defaultQuickNoteType.setText(getString(R.string.settings_defaultQuickNoteType, getString(settingsManager.getDefaultQuickNoteType().getNameResId())));
        viewClick(binding.defaultQuickNoteType, () -> new DialogSelectItemType(getContext(), type -> {
            settingsManager.setDefaultQuickNoteType(ItemsRegistry.REGISTRY.get(type));
            binding.defaultQuickNoteType.setText(getString(R.string.settings_defaultQuickNoteType, getString(settingsManager.getDefaultQuickNoteType().getNameResId())));
            settingsManager.save();
        }).show());
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

    private void setupFirstDayOfWeekSpinner() {
        DateFormatSymbols dfs = DateFormatSymbols.getInstance(Locale.getDefault());
        String[] weekdays = dfs.getWeekdays();

        SimpleSpinnerAdapter<Integer> themeSpinnerAdapter = new SimpleSpinnerAdapter<Integer>(requireContext())
                .add(weekdays[Calendar.SUNDAY], Calendar.SUNDAY)
                .add(weekdays[Calendar.MONDAY], Calendar.MONDAY);

        binding.firstDayOfWeekSpinner.setAdapter(themeSpinnerAdapter);
        binding.firstDayOfWeekSpinner.setSelection(themeSpinnerAdapter.getValuePosition(settingsManager.getFirstDayOfWeek()));
        binding.firstDayOfWeekSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int t = themeSpinnerAdapter.getItem(position);
                settingsManager.setFirstDayOfWeek(t);
                settingsManager.save();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    public static void showExportDialog(Activity context, SettingsManager settingsManager, ColorHistoryManager colorHistoryManager) {
        ExportBinding binding = ExportBinding.inflate(context.getLayoutInflater());
        
        new AlertDialog.Builder(context)
                .setView(binding.getRoot())
                .setTitle(R.string.settings_export_dialog_title)
                .setNegativeButton(R.string.abc_cancel, null)
                .setPositiveButton(R.string.settings_export_dialog_export, (ignore0, ignore1) -> {
                    final ItemManager itemManager = App.get(context).getItemManager();
                    List<ImportWrapper.Permission> perms = new ArrayList<>();
                    final boolean isAllItems = binding.exportAllItems.isChecked();
                    final boolean isSettings = binding.exportSettings.isChecked();
                    final boolean isColorHistory = binding.exportColorHistory.isChecked();

                    final String dialogMessage = binding.exportDialogMessage.getText().toString().trim();
                    final boolean isDialogMessage = !dialogMessage.isEmpty();

                    if (isAllItems) perms.add(ImportWrapper.Permission.ADD_TABS);
                    if (isSettings) perms.add(ImportWrapper.Permission.OVERWRITE_SETTINGS);
                    if (isColorHistory) perms.add(ImportWrapper.Permission.OVERWRITE_COLOR_HISTORY);
                    if (isDialogMessage) perms.add(ImportWrapper.Permission.PRE_IMPORT_SHOW_DIALOG);


                    ImportWrapper.Builder i = ImportWrapper.createImport(perms.toArray(new ImportWrapper.Permission[0]));
                    if (isDialogMessage) i.setDialogMessage(dialogMessage);
                    if (isAllItems) i.addTabAll(itemManager.getTabs().toArray(new Tab[0]));
                    if (isSettings) {
                        try {
                            i.setSettings(settingsManager.exportJSONSettings());
                        } catch (JSONException e) {
                            Toast.makeText(context, context.getString(R.string.export_error, e.toString()), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    if (isColorHistory) {
                        try {
                            i.setColorHistory(colorHistoryManager.exportJSONColorHistory());
                        } catch (JSONException e) {
                            Toast.makeText(context, context.getString(R.string.export_error, e.toString()), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    try {
                        String s = i.build().finalExport();

                        ClipboardManager clipboardManager = context.getSystemService(ClipboardManager.class);
                        clipboardManager.setPrimaryClip(ClipData.newPlainText(context.getString(R.string.export_clipdata_label), s));

                        Toast.makeText(context, R.string.export_success, Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(context, context.getString(R.string.export_error, e.toString()), Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }

    private void experimentalFeaturesInteract() {
        if (System.currentTimeMillis() - easterEggLastClick < 1000) {
            easterEggCounter++;
            if (easterEggCounter >= 6) {
                easterEggCounter = 0;
                showFeatureFlagsDialog();
            }
        } else {
            easterEggCounter = 0;
        }
        easterEggLastClick = System.currentTimeMillis();
    }

    private void showFeatureFlagsDialog() {
        LinearLayout view = new LinearLayout(requireContext());
        view.setOrientation(LinearLayout.VERTICAL);

        for (FeatureFlag featureFlag : FeatureFlag.values()) {
            CheckBox c = new CheckBox(requireContext());
            c.setText(featureFlag.name());
            c.setChecked(app.isFeatureFlag(featureFlag));
            viewClick(c, () -> {
                boolean is = c.isChecked();
                if (is) {
                    if (!app.isFeatureFlag(featureFlag)) {
                        app.getFeatureFlags().add(featureFlag);
                    }
                } else {
                    if (app.isFeatureFlag(featureFlag)) {
                        app.getFeatureFlags().remove(featureFlag);
                    }
                }
            });

            TextView textView = new TextView(requireContext());
            textView.setText(featureFlag.getDescription());
            textView.setTextSize(11);
            textView.setPadding(60, 0, 0, 0);

            view.addView(c);
            view.addView(textView);
        }

        ScrollView scrollView = new ScrollView(requireContext());
        scrollView.addView(view);

        Dialog dialog = new AlertDialog.Builder(requireContext())
                .setView(scrollView)
                .setTitle("DEBUG: FeatureFlags")
                .setNegativeButton(R.string.abc_cancel, null)
                .create();
        dialog.show();
    }
}
