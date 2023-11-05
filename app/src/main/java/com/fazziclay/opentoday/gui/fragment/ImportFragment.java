package com.fazziclay.opentoday.gui.fragment;

import static com.fazziclay.opentoday.util.InlineUtil.viewClick;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.ColorHistoryManager;
import com.fazziclay.opentoday.app.ImportWrapper;
import com.fazziclay.opentoday.app.settings.SettingsManager;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.selection.SelectionManager;
import com.fazziclay.opentoday.app.items.tab.Tab;
import com.fazziclay.opentoday.app.items.tab.TabsManager;
import com.fazziclay.opentoday.databinding.FragmentImportBinding;
import com.fazziclay.opentoday.gui.ActivitySettings;
import com.fazziclay.opentoday.gui.EnumsRegistry;
import com.fazziclay.opentoday.gui.UI;
import com.fazziclay.opentoday.gui.interfaces.ActivitySettingsMember;
import com.fazziclay.opentoday.util.NetworkUtil;

import java.util.UUID;

public class ImportFragment extends Fragment implements ActivitySettingsMember {
    private static final String KEY_ITEMS_STORAGE = "importFragment:itemsStorageId";
    private static final String KEY_START_TEXT = "importFragment:startImportText";
    private static final String KEY_AUTORUN = "importFragment:autoRun";
    @NonNull
    public static ImportFragment create(@NonNull final UUID itemsStorageId) {
        final ImportFragment f = new ImportFragment();

        final Bundle args = new Bundle();
        args.putString(KEY_ITEMS_STORAGE, itemsStorageId.toString());
        f.setArguments(args);

        return f;
    }

    @NonNull
    public static ImportFragment create(@NonNull final UUID itemsStorageId, final String startText, final boolean autoRun) {
        final ImportFragment f = new ImportFragment();

        final Bundle args = new Bundle();
        args.putString(KEY_ITEMS_STORAGE, itemsStorageId.toString());
        args.putString(KEY_START_TEXT, startText);
        args.putBoolean(KEY_AUTORUN, autoRun);
        f.setArguments(args);

        return f;
    }


    private FragmentImportBinding binding;
    private Activity activity;
    private App app;
    private TabsManager tabsManager;
    private SelectionManager selectionManager;
    private ItemsStorage itemsStorage;
    private boolean autoRun = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = requireActivity();
        app = App.get(requireContext());
        tabsManager = app.getTabsManager();
        selectionManager = app.getSelectionManager();
        itemsStorage = tabsManager.getItemsStorageById(UUID.fromString(getArguments().getString(KEY_ITEMS_STORAGE)));

        if (getArguments().containsKey(KEY_START_TEXT)) {
            if (getArguments().getBoolean(KEY_AUTORUN)) {
                autoRun = true;
                String s = getArguments().getString(KEY_START_TEXT);
                importData(s);
            }
        }

        UI.getUIRoot(this).pushActivitySettings(a -> {
            a.setNotificationsVisible(false);
            a.setToolbarSettings(ActivitySettings.ToolbarSettings.createBack(R.string.fragment_import_title, () -> UI.rootBack(this)));
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentImportBinding.inflate(inflater);


        viewClick(binding.runImport, () -> importData(binding.editText.getText().toString()));
        viewClick(binding.cancel, () -> UI.rootBack(this));

        return binding.getRoot();
    }

    private void importData(String input) {
        if (input.startsWith("https://") || input.startsWith("http://")) {
            importNetwork(input);
        } else {
            importContent(input);
        }
    }

    @UiThread
    private void importContent(String content) {
        try {
            ImportWrapper importWrapper = ImportWrapper.finalImport(content);
            importWrapper(importWrapper);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(activity, activity.getString(R.string.toolbar_more_file_import_exception, e.toString()), Toast.LENGTH_SHORT).show();
            UI.rootBack(this);
        }
    }

    private void importNetwork(String url) {
        Dialog loading = new Dialog(activity);
        loading.getWindow().setBackgroundDrawable(null);
        loading.setCancelable(false);
        loading.setCanceledOnTouchOutside(false);
        ProgressBar progressBar = new ProgressBar(activity);
        progressBar.setIndeterminate(true);
        loading.setContentView(progressBar);
        loading.show();

        Thread thread = new Thread(() -> {
            try {
                String content = NetworkUtil.parseTextPage(url);
                activity.runOnUiThread(() -> importContent(content));

            } catch (Exception e) {
                e.printStackTrace();
                activity.runOnUiThread(() -> Toast.makeText(activity, activity.getString(R.string.toolbar_more_file_import_exception, e.toString()), Toast.LENGTH_LONG).show());
            }
            activity.runOnUiThread(loading::cancel);
        });
        thread.setName("NetworkImportParseThread");
        thread.start();
    }

    private void importWrapper(ImportWrapper importWrapper) {
        if (importWrapper.isError()) {
            ImportWrapper.ErrorCode errorCode = importWrapper.getErrorCode();
            Toast.makeText(activity, activity.getString(R.string.toolbar_more_file_import_exception, getString(EnumsRegistry.INSTANCE.nameResId(errorCode))), Toast.LENGTH_LONG).show();
            UI.rootBack(this);
            return;
        }
        if (importWrapper.isNewestVersion()) {
            Toast.makeText(activity, activity.getString(R.string.toolbar_more_file_import_exception, getString(EnumsRegistry.INSTANCE.nameResId(ImportWrapper.ErrorCode.VERSION_NOT_COMPATIBLE))), Toast.LENGTH_LONG).show();
            UI.rootBack(this);
            return;
        }

        StringBuilder perms = new StringBuilder();
        String info = "";
        final boolean ONLY_DESCRIPTION = true;
        for (ImportWrapper.Permission permission : importWrapper.getPermissions()) {
            if (perms.length() > 0) perms.append("\n");
            String desc = getDescription(permission);
            if (ONLY_DESCRIPTION) {
                // ReplaceNullCheck required height android version
                //noinspection ReplaceNullCheck
                if (desc == null) {
                    perms.append("* ").append(permission.name());
                } else {
                    perms.append("* ").append(desc);
                }
            } else {
                perms.append("* ").append(permission.name()).append(desc != null ? "\n  | " + desc : "");
            }
        }

        if (importWrapper.isPerm(ImportWrapper.Permission.ADD_ITEMS_TO_CURRENT)) {
            info = info + activity.getString(R.string.fragment_import_dialog_main_info_items, String.valueOf(importWrapper.getItems().size())) + "\n";
        }

        if (importWrapper.isPerm(ImportWrapper.Permission.ADD_TABS)) {
            info = info + activity.getString(R.string.fragment_import_dialog_main_info_tabs, String.valueOf(importWrapper.getTabs().size())) + "\n";
        }

        if (info.isEmpty()) {
            info = activity.getString(R.string.fragment_import_dialog_main_info_empty);
        }

        new AlertDialog.Builder(activity)
                .setTitle(R.string.fragment_import_dialog_main_title)
                .setMessage(getString(R.string.fragment_import_dialog_main_message, perms.toString(), info))
                .setNegativeButton(R.string.fragment_import_dialog_main_cancel, (_ignore00, _ignore11) -> {
                    if (autoRun) {
                        UI.rootBack(this);
                    }
                })
                .setPositiveButton(R.string.fragment_import_dialog_main_import, (_ignore0, _ignore1) -> {
                    if (importWrapper.isPerm(ImportWrapper.Permission.PRE_IMPORT_SHOW_DIALOG)) {
                        new AlertDialog.Builder(requireActivity())
                                .setTitle(R.string.fragment_import_dialog_importMessage_title)
                                .setMessage(importWrapper.getDialogMessage())
                                .setPositiveButton(R.string.fragment_import_dialog_importMessage_import, (ignore2, ignore3) -> directImport(importWrapper))
                                .show();
                    } else {
                        directImport(importWrapper);
                    }
                    UI.rootBack(this);
                })
                .show();
    }

    private String getDescription(ImportWrapper.Permission permission) {
        return switch (permission) {
            case ADD_ITEMS_TO_CURRENT ->
                    activity.getString(R.string.importWrapper_permission_ADD_ITEMS_TO_CURRENT);
            case ADD_TABS -> activity.getString(R.string.importWrapper_permission_ADD_TABS);
            case PRE_IMPORT_SHOW_DIALOG ->
                    activity.getString(R.string.importWrapper_permission_PRE_IMPORT_SHOW_DIALOG);
            case OVERWRITE_SETTINGS ->
                    activity.getString(R.string.importWrapper_permission_OVERWRITE_SETTINGS);
            case OVERWRITE_COLOR_HISTORY ->
                    activity.getString(R.string.importWrapper_permission_OVERWRITE_COLOR_HISTORY);
        };

    }
    private void directImport(ImportWrapper importWrapper) {
        try {
            directImport0(importWrapper);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(activity, activity.getString(R.string.toolbar_more_file_import_exception, e.toString()), Toast.LENGTH_SHORT).show();
        }
    }

    private void directImport0(ImportWrapper importWrapper) {
        boolean isRestart = false;
        if (importWrapper.isPerm(ImportWrapper.Permission.ADD_ITEMS_TO_CURRENT)) {
            for (Item item : importWrapper.getItems()) {
                switch (app.getSettingsManager().getItemAddPosition()) {
                    case TOP -> itemsStorage.addItem(item, 0);
                    case BOTTOM -> itemsStorage.addItem(item);
                }
                selectionManager.selectItem(item);
            }
        }

        if (importWrapper.isPerm(ImportWrapper.Permission.ADD_TABS)) {
            for (Tab tab : importWrapper.getTabs()) {
                tabsManager.addTab(tab);
            }
        }

        if (importWrapper.isPerm(ImportWrapper.Permission.OVERWRITE_COLOR_HISTORY)) {
            ColorHistoryManager colorHistoryManager = app.getColorHistoryManager();
            colorHistoryManager.importData(importWrapper.getColorHistory());
        }

        if (importWrapper.isPerm(ImportWrapper.Permission.OVERWRITE_SETTINGS)) {
            SettingsManager settingsManager = app.getSettingsManager();
            settingsManager.importData(importWrapper.getSettings());
            AppCompatDelegate.setDefaultNightMode(settingsManager.getTheme());
        }

        Toast.makeText(activity, R.string.toolbar_more_file_import_success, Toast.LENGTH_SHORT).show();

        if (isRestart) {
            System.exit(0);
        }
    }
}
