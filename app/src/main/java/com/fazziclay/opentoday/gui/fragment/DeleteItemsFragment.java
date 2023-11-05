package com.fazziclay.opentoday.gui.fragment;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.settings.enums.ItemAction;
import com.fazziclay.opentoday.app.items.ItemsRoot;
import com.fazziclay.opentoday.app.items.item.CycleListItem;
import com.fazziclay.opentoday.app.items.item.FilterGroupItem;
import com.fazziclay.opentoday.app.items.item.GroupItem;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.databinding.FragmentDeleteItemsBinding;
import com.fazziclay.opentoday.gui.ActivitySettings;
import com.fazziclay.opentoday.gui.UI;
import com.fazziclay.opentoday.gui.interfaces.ActivitySettingsMember;
import com.fazziclay.opentoday.gui.item.ItemViewGenerator;
import com.fazziclay.opentoday.gui.item.ItemViewGeneratorBehavior;
import com.fazziclay.opentoday.gui.item.ItemViewHolder;
import com.fazziclay.opentoday.gui.item.ItemsStorageDrawerBehavior;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeleteItemsFragment extends Fragment implements ActivitySettingsMember {
    private static final String KEY_ITEMS_TO_DELETE = "itemsToDelete";
    private static final ItemViewGeneratorBehavior ITEM_VIEW_GENERATOR_BEHAVIOR = new DeleteViewGeneratorBehavior();

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
    private int totalToDelete;

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
        totalToDelete = 0;
        for (Item item : itemsToDelete) {
            totalToDelete += item.getChildrenItemCount() + 1;
        }
        // parse END

        itemViewGenerator = new ItemViewGenerator(requireActivity(), true);

        UI.getUIRoot(this).pushActivitySettings(a -> {
            a.setNotificationsVisible(false);
            a.setToolbarSettings(ActivitySettings.ToolbarSettings.createBack(requireActivity().getString(R.string.fragment_deleteItems_delete_title, String.valueOf(itemsToDelete.length), String.valueOf(totalToDelete)), () -> UI.rootBack(DeleteItemsFragment.this)));
        });
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
                final Item item = itemsToDelete[position];
                final View view = itemViewGenerator.generate(item, binding.list, ITEM_VIEW_GENERATOR_BEHAVIOR, holder.destroyer, item1 -> {
                    // do nothing
                });
                holder.bind(item, view);
            }

            @Override
            public void onViewRecycled(@NonNull ItemViewHolder holder) {
                holder.recycle();
            }

            @Override
            public int getItemCount() {
                return itemsToDelete.length;
            }
        });

        binding.deleteButton.setOnClickListener(v -> {
            AlertDialog show = new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(requireActivity().getString(R.string.fragment_deleteItems_delete_title, String.valueOf(itemsToDelete.length), String.valueOf(totalToDelete)))
                    .setNegativeButton(R.string.fragment_deleteItems_delete_cancel, null)
                    .setPositiveButton(R.string.fragment_deleteItems_delete_apply, ((_dialog1, _which) -> {
                        for (Item item : itemsToDelete) {
                            item.delete();
                        }
                        UI.rootBack(this);
                    }))
                    .show();
            show.setIcon(R.drawable.delete_24px);
        });

        binding.cancelButton.setOnClickListener(v -> UI.rootBack(this));
        return binding.getRoot();
    }


    private DeleteItemsFragment() {}

    private static class DeleteViewGeneratorBehavior implements ItemViewGeneratorBehavior {

        private static final ItemsStorageDrawerBehavior ITEM_STORAGE_DRAWER_BEHAVIOR = new ItemsStorageDrawerBehavior() {
            @Override
            public ItemAction getItemOnClickAction() {
                return null;
            }

            @Override
            public boolean isScrollToAddedItem() {
                return false;
            }

            @Override
            public ItemAction getItemOnLeftAction() {
                return null;
            }

            @Override
            public void onItemOpenEditor(Item item) {

            }

            @Override
            public void onItemOpenTextEditor(Item item) {

            }

            @Override
            public boolean ignoreFilterGroup() {
                return true;
            }

            @Override
            public void onItemDeleteRequest(Item item) {
                // do nothing
            }

        };

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

        @Override
        public ItemsStorageDrawerBehavior getItemsStorageDrawerBehavior(Item item) {
            return ITEM_STORAGE_DRAWER_BEHAVIOR;
        }

        @Override
        public boolean isRenderMinimized(Item item) {
            return false;
        }

        @Override
        public boolean isRenderNotificationIndicator(Item item) {
            return item.isNotifications();
        }
    }
}
