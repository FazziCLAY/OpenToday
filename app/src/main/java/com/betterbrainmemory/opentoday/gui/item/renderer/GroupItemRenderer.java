package com.betterbrainmemory.opentoday.gui.item.renderer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.betterbrainmemory.opentoday.R;
import com.betterbrainmemory.opentoday.app.App;
import com.betterbrainmemory.opentoday.app.items.item.GroupItem;
import com.betterbrainmemory.opentoday.databinding.ItemGroupBinding;
import com.betterbrainmemory.opentoday.gui.interfaces.ItemInterface;
import com.betterbrainmemory.opentoday.gui.item.ItemViewGenerator;
import com.betterbrainmemory.opentoday.gui.item.ItemViewGeneratorBehavior;
import com.betterbrainmemory.opentoday.gui.item.ItemsStorageDrawer;
import com.betterbrainmemory.opentoday.gui.item.ItemsStorageDrawerBehavior;
import com.betterbrainmemory.opentoday.gui.item.registry.ItemRenderer;
import com.betterbrainmemory.opentoday.gui.item.registry.NameResolver;
import com.betterbrainmemory.opentoday.util.Destroyer;

import org.jetbrains.annotations.NotNull;

public class GroupItemRenderer implements ItemRenderer<GroupItem>, NameResolver {
    @Override
    public String resolveName(Context context) {
        return context.getString(R.string.item_group);
    }

    @Override
    public String resolveDescription(Context context) {
        return context.getString(R.string.item_group_description);
    }

    @Override
    public View render(GroupItem item, Activity context, LayoutInflater layoutInflater, ViewGroup parent, @NotNull ItemViewGeneratorBehavior behavior, @NotNull ItemInterface onItemClick, ItemViewGenerator itemViewGenerator, boolean previewMode, @NotNull Destroyer destroyer) {
        final ItemGroupBinding binding = ItemGroupBinding.inflate(layoutInflater, parent, false);

        // Text
        TextItemRenderer.applyTextItemToTextView(context, item, binding.title, behavior, destroyer, previewMode);
        ItemViewGenerator.applyItemNotificationIndicator(context, item, binding.indicatorNotification, behavior, destroyer, previewMode);

        // Group
        if (!behavior.isRenderMinimized(item)) {
            var drawer = createItemsStorageDrawerForGroupItem(context, item, binding.content, behavior, previewMode, behavior.getItemsStorageDrawerBehavior(item), onItemClick);
            drawer.create();
            destroyer.add(drawer::destroy);
        }

        binding.externalEditor.setEnabled(!previewMode);
        binding.externalEditor.setOnClickListener(_ignore -> behavior.onGroupEdit(item));

        return binding.getRoot();
    }

    private ItemsStorageDrawer createItemsStorageDrawerForGroupItem(Activity activity, GroupItem item, RecyclerView content, ItemViewGeneratorBehavior behavior, boolean previewMode, ItemsStorageDrawerBehavior itemsStorageDrawerBehavior, ItemInterface onItemClick) {
        return ItemsStorageDrawer.builder(activity, itemsStorageDrawerBehavior, behavior, App.get(activity).getSelectionManager(), item)
                .setView(content)
                .setOnItemClick(onItemClick)
                .setPreviewMode(previewMode)
                .build();
    }
}
