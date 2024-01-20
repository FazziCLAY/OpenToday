package com.betterbrainmemory.opentoday.gui.item.renderer;

import static com.betterbrainmemory.opentoday.util.InlineUtil.viewVisible;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.betterbrainmemory.opentoday.R;
import com.betterbrainmemory.opentoday.app.items.item.CycleListItem;
import com.betterbrainmemory.opentoday.databinding.ItemCycleListBinding;
import com.betterbrainmemory.opentoday.gui.interfaces.ItemInterface;
import com.betterbrainmemory.opentoday.gui.item.CurrentItemStorageDrawer;
import com.betterbrainmemory.opentoday.gui.item.ItemViewGenerator;
import com.betterbrainmemory.opentoday.gui.item.ItemViewGeneratorBehavior;
import com.betterbrainmemory.opentoday.gui.item.registry.ItemRenderer;
import com.betterbrainmemory.opentoday.gui.item.registry.NameResolver;
import com.betterbrainmemory.opentoday.util.Destroyer;
import com.betterbrainmemory.opentoday.util.callback.Status;

import org.jetbrains.annotations.NotNull;

public class CycleListItemRenderer implements ItemRenderer<CycleListItem>, NameResolver {
    @Override
    public String resolveName(Context context) {
        return context.getString(R.string.item_cycleList);
    }

    @Override
    public String resolveDescription(Context context) {
        return context.getString(R.string.item_cycleList_description);
    }

    @Override
    public View render(CycleListItem item, Activity context, LayoutInflater layoutInflater, ViewGroup parent, @NotNull ItemViewGeneratorBehavior behavior, @NotNull ItemInterface onItemClick, ItemViewGenerator itemViewGenerator, boolean previewMode, @NotNull Destroyer destroyer) {
        final ItemCycleListBinding binding = ItemCycleListBinding.inflate(layoutInflater, parent, false);

        // Text
        TextItemRenderer.applyTextItemToTextView(context, item, binding.title, behavior, destroyer, previewMode);
        ItemViewGenerator.applyItemNotificationIndicator(context, item, binding.indicatorNotification, behavior, destroyer, previewMode);

        // CycleList
        binding.next.setEnabled(!previewMode);
        binding.next.setOnClickListener(v -> item.next());
        binding.previous.setEnabled(!previewMode);
        binding.previous.setOnClickListener(v -> item.previous());

        binding.externalEditor.setEnabled(!previewMode);
        binding.externalEditor.setOnClickListener(_ignore -> behavior.onCycleListEdit(item));

        if (!behavior.isRenderMinimized(item)) {
            final var drawer = new CurrentItemStorageDrawer(context, binding.content, itemViewGenerator, behavior, item, destroyer, onItemClick);
            drawer.setOnUpdateListener(currentItem -> {
                viewVisible(binding.empty, currentItem == null, View.GONE);
                return Status.NONE;
            });
            drawer.create();
            destroyer.add(drawer::destroy);
        } else {
            binding.empty.setVisibility(View.GONE);
        }

        return binding.getRoot();
    }
}
