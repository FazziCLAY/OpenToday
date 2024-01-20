package com.betterbrainmemory.opentoday.gui.item.renderer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.betterbrainmemory.opentoday.R;
import com.betterbrainmemory.opentoday.app.items.item.DayRepeatableCheckboxItem;
import com.betterbrainmemory.opentoday.databinding.ItemDayRepeatableCheckboxBinding;
import com.betterbrainmemory.opentoday.gui.interfaces.ItemInterface;
import com.betterbrainmemory.opentoday.gui.item.ItemViewGenerator;
import com.betterbrainmemory.opentoday.gui.item.ItemViewGeneratorBehavior;
import com.betterbrainmemory.opentoday.gui.item.registry.ItemRenderer;
import com.betterbrainmemory.opentoday.gui.item.registry.NameResolver;
import com.betterbrainmemory.opentoday.util.Destroyer;

import org.jetbrains.annotations.NotNull;

public class CheckmarkDayRepeatableItemRenderer implements NameResolver, ItemRenderer<DayRepeatableCheckboxItem> {
    @Override
    public String resolveName(Context context) {
        return context.getString(R.string.item_dayRepeatableCheckbox);
    }

    @Override
    public String resolveDescription(Context context) {
        return context.getString(R.string.item_dayRepeatableCheckbox_description);
    }

    @Override
    public View render(DayRepeatableCheckboxItem item, Activity context, LayoutInflater layoutInflater, ViewGroup parent, @NotNull ItemViewGeneratorBehavior behavior, @NotNull ItemInterface onItemClick, ItemViewGenerator itemViewGenerator, boolean previewMode, @NotNull Destroyer destroyer) {
        final ItemDayRepeatableCheckboxBinding binding = ItemDayRepeatableCheckboxBinding.inflate(layoutInflater, parent, false);

        TextItemRenderer.applyTextItemToTextView(context, item, binding.text, behavior, destroyer, previewMode);
        CheckmarkItemRenderer.applyCheckItemToCheckBoxView(context, item, binding.checkbox, behavior, previewMode);
        ItemViewGenerator.applyItemNotificationIndicator(context, item, binding.indicatorNotification, behavior, destroyer, previewMode);

        return binding.getRoot();
    }
}
