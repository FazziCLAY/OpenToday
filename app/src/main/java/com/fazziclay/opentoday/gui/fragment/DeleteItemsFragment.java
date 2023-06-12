package com.fazziclay.opentoday.gui.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.items.ItemsRoot;
import com.fazziclay.opentoday.app.items.item.CycleListItem;
import com.fazziclay.opentoday.app.items.item.FilterGroupItem;
import com.fazziclay.opentoday.app.items.item.GroupItem;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.databinding.FragmentDeleteItemsBinding;
import com.fazziclay.opentoday.gui.ActivitySettings;
import com.fazziclay.opentoday.gui.UI;
import com.fazziclay.opentoday.gui.item.ItemViewGenerator;
import com.fazziclay.opentoday.gui.item.ItemViewGeneratorBehavior;
import com.fazziclay.opentoday.gui.item.ItemViewHolder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeleteItemsFragment extends Fragment {
    private static final String KEY_ITEMS_TO_DELETE = "itemsToDelete";

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

        a.putStringArray(KEY_ITEMS_TO_DELETE, itemsToDelete);

        result.setArguments(a);
        return result;
    }


    private FragmentDeleteItemsBinding binding;
    private ItemsRoot itemsRoot;
    private ItemViewGenerator itemViewGenerator;
    private Item[] itemsToDelete;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App app = App.get(requireContext());
        itemsRoot = app.getItemsRoot();

        // parse
        String[] r = getArguments().getStringArray(KEY_ITEMS_TO_DELETE);
        List<Item> u = new ArrayList<>();
        for (String s : r) {
            u.add(itemsRoot.getItemById(UUID.fromString(s)));
        }
        itemsToDelete = u.toArray(new Item[0]);
        // parse END

        itemViewGenerator = ItemViewGenerator.builder(requireActivity(), new DeleteViewGeneratorBehavior())
                .setPreviewMode(true)
                .build();

        UI.getUIRoot(this).pushActivitySettings(a -> {
            a.setNotificationsVisible(false);
            a.setToolbarSettings(ActivitySettings.ToolbarSettings.createBack(requireActivity().getString(R.string.fragment_deleteItems_delete_title, String.valueOf(itemsToDelete.length)), () -> UI.rootBack(DeleteItemsFragment.this)));
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UI.getUIRoot(this).popActivitySettings();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDeleteItemsBinding.inflate(inflater);

        binding.list.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.list.setAdapter(new RecyclerView.Adapter<ItemViewHolder>() {
            @NonNull
            @Override
            public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                return new ItemViewHolder(parent.getContext());
            }

            @Override
            public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
                Item item = itemsToDelete[position];
                holder.layout.removeAllViews();
                holder.layout.addView(itemViewGenerator.generate(item, binding.list));
            }

            @Override
            public int getItemCount() {
                return itemsToDelete.length;
            }
        });

        binding.deleteButton.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle(requireActivity().getString(R.string.fragment_deleteItems_delete_title, String.valueOf(itemsToDelete.length)))
                .setNegativeButton(R.string.fragment_deleteItems_delete_cancel, null)
                .setPositiveButton(R.string.fragment_deleteItems_delete_apply, ((_dialog1, _which) -> {
                    for (Item item : itemsToDelete) {
                        item.delete();
                    }
                    UI.rootBack(this);
                }))
                .show());

        binding.cancelButton.setOnClickListener(v -> UI.rootBack(this));
        return binding.getRoot();
    }


    public DeleteItemsFragment() {}

    private static class DeleteViewGeneratorBehavior implements ItemViewGeneratorBehavior {

        @Override
        public boolean isConfirmFastChanges() {
            return false;
        }

        @Override
        public void setConfirmFastChanges(boolean b) {
        }

        @Override
        public Drawable getForeground(Item item) {
            return null;
        }

        @Override
        public void onGroupEdit(GroupItem groupItem) {
        }

        @Override
        public void onCycleListEdit(CycleListItem cycleListItem) {
        }

        @Override
        public void onFilterGroupEdit(FilterGroupItem filterGroupItem) {
        }
    }
}
