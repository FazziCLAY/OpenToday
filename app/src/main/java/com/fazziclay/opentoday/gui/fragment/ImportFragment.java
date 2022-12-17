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
import androidx.fragment.app.Fragment;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.ColorHistoryManager;
import com.fazziclay.opentoday.app.ImportWrapper;
import com.fazziclay.opentoday.app.items.ItemManager;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.Selection;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.tab.Tab;
import com.fazziclay.opentoday.app.settings.SettingsManager;
import com.fazziclay.opentoday.databinding.DialogImportBinding;
import com.fazziclay.opentoday.gui.UI;
import com.fazziclay.opentoday.util.NetworkUtil;

import java.util.UUID;

public class ImportFragment extends Fragment {
    private static final String KEY_ITEMS_STORAGE = "importFragment:itemsStorageId";

    @NonNull
    public static ImportFragment create(@NonNull final UUID itemsStorageId) {
        final ImportFragment f = new ImportFragment();

        final Bundle args = new Bundle();
        args.putString(KEY_ITEMS_STORAGE, itemsStorageId.toString());
        f.setArguments(args);

        return f;
    }

    private DialogImportBinding binding;
    private Activity activity;
    private App app;
    private ItemManager itemManager;
    private ItemsStorage itemsStorage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = requireActivity();
        app = App.get(requireContext());
        itemManager = app.getItemManager();
        itemsStorage = itemManager.getItemStorageById(UUID.fromString(getArguments().getString(KEY_ITEMS_STORAGE)));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogImportBinding.inflate(inflater);

        viewClick(binding.runImport, () -> importData(binding.editText.getText().toString()));
        viewClick(binding.cancel, () -> UI.back(this));

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
            UI.back(this);
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
        StringBuilder perms = new StringBuilder();
        String info = "";
        final boolean ONLY_DESCRIPTION = true;
        for (ImportWrapper.Permission permission : importWrapper.getPermissions()) {
            if (perms.length() > 0) perms.append("\n");
            String desc = getDescription(permission);
            if (ONLY_DESCRIPTION) {
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
            info = info + activity.getString(R.string.importFragment_dialog_main_info_items, String.valueOf(importWrapper.getItems().size())) + "\n";
        }

        if (importWrapper.isPerm(ImportWrapper.Permission.ADD_TABS)) {
            info = info + activity.getString(R.string.importFragment_dialog_main_info_tabs, String.valueOf(importWrapper.getTabs().size())) + "\n";
        }

        if (info.isEmpty()) {
            info = activity.getString(R.string.empty); // TODO: 12.11.2022 use other resID
        }

        new AlertDialog.Builder(activity)
                .setTitle(R.string.importFragment_dialog_main_title)
                .setMessage(getString(R.string.importFragment_dialog_main_message, perms.toString(), info))
                .setNegativeButton(R.string.importFragment_dialog_main_cancel, null)
                .setPositiveButton(R.string.importFragment_dialog_main_import, (ignore0, ignore1) -> {
                    if (importWrapper.isPerm(ImportWrapper.Permission.PRE_IMPORT_SHOW_DIALOG)) {
                        new AlertDialog.Builder(requireActivity())
                                .setTitle(R.string.importFragment_dialog_importMessage_title)
                                .setMessage(importWrapper.getDialogMessage())
                                .setPositiveButton(R.string.importFragment_dialog_importMessage_import, (ignore2, ignore3) -> directImport(importWrapper))
                                .show();
                    } else {
                        directImport(importWrapper);
                    }
                    UI.back(this);
                })
                .show();
    }

    private String getDescription(ImportWrapper.Permission permission) {
        switch (permission) {
            case ADD_ITEMS_TO_CURRENT:
                return activity.getString(R.string.import_permission_ADD_ITEMS_TO_CURRENT);

            case ADD_TABS:
                return activity.getString(R.string.import_permission_ADD_TABS);

            case PRE_IMPORT_SHOW_DIALOG:
                return activity.getString(R.string.import_permission_PRE_IMPORT_SHOW_DIALOG);

            case OVERWRITE_SETTINGS:
                return activity.getString(R.string.import_permission_OVERWRITE_SETTINGS);

            case OVERWRITE_COLOR_HISTORY:
                return activity.getString(R.string.import_permission_OVERWRITE_COLOR_HISTORY);
        }

        return null;
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
                itemsStorage.addItem(item);
                itemManager.selectItem(new Selection(itemsStorage, item));
            }
        }

        if (importWrapper.isPerm(ImportWrapper.Permission.ADD_TABS)) {
            for (Tab tab : importWrapper.getTabs()) {
                itemManager.addTab(tab);
            }
        }

        if (importWrapper.isPerm(ImportWrapper.Permission.OVERWRITE_COLOR_HISTORY)) {
            ColorHistoryManager colorHistoryManager = app.getColorHistoryManager();
            colorHistoryManager.importData(importWrapper.getColorHistory());
        }

        if (importWrapper.isPerm(ImportWrapper.Permission.OVERWRITE_SETTINGS)) {
            SettingsManager settingsManager = app.getSettingsManager();
            settingsManager.importData(importWrapper.getSettings());
        }

        Toast.makeText(activity, R.string.toolbar_more_file_import_success, Toast.LENGTH_SHORT).show();

        if (isRestart) {
            System.exit(0);
        }
    }
}
