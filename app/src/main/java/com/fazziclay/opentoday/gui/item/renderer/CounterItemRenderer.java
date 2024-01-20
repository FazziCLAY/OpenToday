package com.fazziclay.opentoday.gui.item.renderer;

import static com.fazziclay.opentoday.util.InlineUtil.viewClick;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.items.item.CounterItem;
import com.fazziclay.opentoday.databinding.ItemCounterBinding;
import com.fazziclay.opentoday.gui.interfaces.ItemInterface;
import com.fazziclay.opentoday.gui.item.ItemViewGenerator;
import com.fazziclay.opentoday.gui.item.ItemViewGeneratorBehavior;
import com.fazziclay.opentoday.gui.item.registry.ItemRenderer;
import com.fazziclay.opentoday.gui.item.registry.NameResolver;
import com.fazziclay.opentoday.util.Destroyer;

import org.jetbrains.annotations.NotNull;

public class CounterItemRenderer implements ItemRenderer<CounterItem>, NameResolver {
    @Override
    public String resolveName(Context context) {
        return context.getString(R.string.item_counter);
    }

    @Override
    public String resolveDescription(Context context) {
        return context.getString(R.string.item_counter_description);
    }

    @Override
    public View render(CounterItem item, Activity context, LayoutInflater layoutInflater, ViewGroup parent, @NotNull ItemViewGeneratorBehavior behavior, @NotNull ItemInterface onItemClick, ItemViewGenerator itemViewGenerator, boolean previewMode, @NotNull Destroyer destroyer) {
        final ItemCounterBinding binding = ItemCounterBinding.inflate(layoutInflater, parent, false);

        // Title
        TextItemRenderer.applyTextItemToTextView(context, item, binding.title, behavior, destroyer, previewMode);
        ItemViewGenerator.applyItemNotificationIndicator(context, item, binding.indicatorNotification, behavior, destroyer, previewMode);

        // Counter
        viewClick(binding.up, () -> ItemViewGenerator.runFastChanges(context, behavior, R.string.item_counter_fastChanges_up, item::increase));
        viewClick(binding.down, () -> ItemViewGenerator.runFastChanges(context, behavior, R.string.item_counter_fastChanges_down, item::decrease));
        binding.up.setEnabled(!previewMode);
        binding.down.setEnabled(!previewMode);

        binding.counter.setText(String.valueOf(item.getCounter()));

        return binding.getRoot();
    }
}
