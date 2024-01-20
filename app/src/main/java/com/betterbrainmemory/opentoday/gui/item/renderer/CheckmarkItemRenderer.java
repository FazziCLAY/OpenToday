package com.betterbrainmemory.opentoday.gui.item.renderer;

import static com.betterbrainmemory.opentoday.util.InlineUtil.viewClick;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.betterbrainmemory.opentoday.R;
import com.betterbrainmemory.opentoday.app.items.item.CheckboxItem;
import com.betterbrainmemory.opentoday.databinding.ItemCheckboxBinding;
import com.betterbrainmemory.opentoday.gui.interfaces.ItemInterface;
import com.betterbrainmemory.opentoday.gui.item.ItemViewGenerator;
import com.betterbrainmemory.opentoday.gui.item.ItemViewGeneratorBehavior;
import com.betterbrainmemory.opentoday.gui.item.registry.ItemRenderer;
import com.betterbrainmemory.opentoday.gui.item.registry.NameResolver;
import com.betterbrainmemory.opentoday.util.Destroyer;

import org.jetbrains.annotations.NotNull;

public class CheckmarkItemRenderer implements NameResolver, ItemRenderer<CheckboxItem> {
    @Override
    public String resolveName(Context context) {
        return context.getString(R.string.item_checkbox);
    }

    @Override
    public String resolveDescription(Context context) {
        return context.getString(R.string.item_checkbox_description);
    }

    @Override
    public View render(CheckboxItem item, Activity context, LayoutInflater layoutInflater, ViewGroup parent, @NotNull ItemViewGeneratorBehavior behavior, @NotNull ItemInterface onItemClick, ItemViewGenerator itemViewGenerator, boolean previewMode, @NotNull Destroyer destroyer) {
        final ItemCheckboxBinding binding = ItemCheckboxBinding.inflate(layoutInflater, parent, false);

        TextItemRenderer.applyTextItemToTextView(context, item, binding.text, behavior, destroyer, previewMode);
        applyCheckItemToCheckBoxView(context, item, binding.checkbox, behavior, previewMode);
        ItemViewGenerator.applyItemNotificationIndicator(context, item, binding.indicatorNotification, behavior, destroyer, previewMode);

        return binding.getRoot();
    }

    public static void applyCheckItemToCheckBoxView(Context context, final CheckboxItem item, final CheckBox view, ItemViewGeneratorBehavior behavior, boolean previewMode) {
        view.setChecked(item.isChecked());
        view.setEnabled(!previewMode);
        viewClick(view, () -> {
            boolean to = view.isChecked();
            view.setChecked(!to);
            ItemViewGenerator.runFastChanges(context, behavior, to ? R.string.item_checkbox_fastChanges_checked : R.string.item_checkbox_fastChanges_unchecked, () -> {
                view.setChecked(to);
                item.setChecked(to);
                item.visibleChanged();
                item.save();
            });
        });
    }
}
