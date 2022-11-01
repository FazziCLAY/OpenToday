package ru.fazziclay.opentoday.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.app.items.ItemManager;
import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.app.settings.SettingsManager;
import ru.fazziclay.opentoday.databinding.FragmentDeleteItemsBinding;
import ru.fazziclay.opentoday.ui.UI;
import ru.fazziclay.opentoday.ui.item.ItemViewHolder;
import ru.fazziclay.opentoday.ui.item.ItemViewGenerator;
import ru.fazziclay.opentoday.util.MinBaseAdapter;

public class DeleteItemsFragment extends Fragment {
    public static DeleteItemsFragment create(Item[] items) {
        List<UUID> u = new ArrayList<>();
        for (Item item : items) {
            u.add(item.getId());
        }
        return create(u.toArray(new UUID[0]));
    }

    public static DeleteItemsFragment create(UUID[] items) {
        List<String> list = new ArrayList<>();
        for (UUID item : items) {
            list.add(item.toString());
        }
        String[] itemsToDelete = list.toArray(new String[0]);

        DeleteItemsFragment result = new DeleteItemsFragment();
        Bundle a = new Bundle();

        a.putStringArray("itemsToDelete", itemsToDelete);

        result.setArguments(a);
        return result;
    }

    private FragmentDeleteItemsBinding binding;
    private ItemManager itemManager;
    private SettingsManager settingsManager;
    private ItemViewGenerator itemViewGenerator;
    private Item[] itemsToDelete;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App app = App.get(requireContext());
        itemManager = app.getItemManager();
        settingsManager = app.getSettingsManager();

        // parse
        String[] r = getArguments().getStringArray("itemsToDelete");
        List<Item> u = new ArrayList<>();
        for (String s : r) {
            u.add(itemManager.getItemById(UUID.fromString(s)));
        }
        itemsToDelete = u.toArray(new Item[0]);
        //

        itemViewGenerator = ItemViewGenerator.builder(requireActivity(), itemManager, settingsManager)
                .setPreviewMode()
                .build();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDeleteItemsBinding.inflate(inflater);

        binding.list.setAdapter(new MinBaseAdapter() {
            @Override
            public int getCount() {
                return itemsToDelete.length;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                Item item = itemsToDelete[position];
                ItemViewHolder itemViewHolder = new ItemViewHolder(requireContext());
                itemViewHolder.layout.addView(itemViewGenerator.generate(item, parent));
                return itemViewHolder.itemView;
            }
        });

        binding.deleteButton.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle(requireActivity().getString(R.string.dialog_previewDeleteItems_delete_title, String.valueOf(itemsToDelete.length)))
                .setNegativeButton(R.string.dialog_previewDeleteItems_delete_cancel, null)
                .setPositiveButton(R.string.dialog_previewDeleteItems_delete_apply, ((dialog1, which) -> {
                    for (Item item : itemsToDelete) {
                        itemManager.deselectItem(item);
                        item.delete();
                    }
                    UI.back(this);
                }))
                .show());

        binding.cancelButton.setOnClickListener(v -> UI.back(this));
        return binding.getRoot();
    }


    public DeleteItemsFragment() {}
}
