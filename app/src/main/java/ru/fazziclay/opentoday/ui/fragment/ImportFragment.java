package ru.fazziclay.opentoday.ui.fragment;

import static ru.fazziclay.opentoday.util.InlineUtil.viewClick;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.UUID;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.app.items.ImportWrapper;
import ru.fazziclay.opentoday.app.items.ItemManager;
import ru.fazziclay.opentoday.app.items.ItemsStorage;
import ru.fazziclay.opentoday.app.items.Selection;
import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.databinding.DialogImportBinding;
import ru.fazziclay.opentoday.ui.UI;
import ru.fazziclay.opentoday.util.NetworkUtil;

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
    private ItemManager itemManager;
    private ItemsStorage itemsStorage;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        itemManager = App.get(requireContext()).getItemManager();
        itemsStorage = itemManager.getItemStorageById(UUID.fromString(getArguments().getString(KEY_ITEMS_STORAGE)));
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogImportBinding.inflate(inflater);

        viewClick(binding.runImport, () -> {
            importData(binding.editText.getText().toString());
            UI.back(this);
        });

        viewClick(binding.cancel, () -> UI.back(this));

        return binding.getRoot();
    }

    private void importData(String input) {
        Dialog loading = new Dialog(requireContext());
        loading.getWindow().setBackgroundDrawable(null);
        loading.setCancelable(false);
        loading.setCanceledOnTouchOutside(false);
        ProgressBar progressBar = new ProgressBar(requireContext());
        progressBar.setIndeterminate(true);
        loading.setContentView(progressBar);
        loading.show();

        try {
            final Activity activity = requireActivity();
            new Thread(() -> {
                try {
                    String content;
                    if (input.startsWith("https://") || input.startsWith("http://")) {
                        content = NetworkUtil.parseTextPage(input);
                    } else {
                        content = input;
                    }

                    ImportWrapper importWrapper = ImportWrapper.finalImport(content);
                    for (Item item : importWrapper.getItems()) {
                        activity.runOnUiThread(() -> {
                            itemsStorage.addItem(item);
                            itemManager.selectItem(new Selection(itemsStorage, item));
                        });
                    }
                    activity.runOnUiThread(() -> Toast.makeText(activity, R.string.toolbar_more_file_import_success, Toast.LENGTH_SHORT).show());
                } catch (Exception e) {
                    e.printStackTrace();
                    activity.runOnUiThread(() -> Toast.makeText(activity, activity.getString(R.string.toolbar_more_file_import_exception, e.toString()), Toast.LENGTH_SHORT).show());
                }
                activity.runOnUiThread(loading::cancel);
            }).start();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), requireContext().getString(R.string.toolbar_more_file_import_exception, e.toString()), Toast.LENGTH_SHORT).show();
            loading.cancel();
        }
    }
}
