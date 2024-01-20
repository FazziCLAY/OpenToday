package com.fazziclay.opentoday.gui.item.renderer;

import static com.fazziclay.opentoday.util.InlineUtil.viewClick;
import static com.fazziclay.opentoday.util.InlineUtil.viewVisible;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.items.item.CountDownCheckmarkItem;
import com.fazziclay.opentoday.databinding.ItemCountdownCheckmarkBinding;
import com.fazziclay.opentoday.gui.interfaces.ItemInterface;
import com.fazziclay.opentoday.gui.item.ItemViewGenerator;
import com.fazziclay.opentoday.gui.item.ItemViewGeneratorBehavior;
import com.fazziclay.opentoday.gui.item.registry.ItemRenderer;
import com.fazziclay.opentoday.gui.item.registry.NameResolver;
import com.fazziclay.opentoday.util.Destroyer;

import org.jetbrains.annotations.NotNull;

public class CheckmarkCountdownRenderer implements ItemRenderer<CountDownCheckmarkItem>, NameResolver {
    @Override
    public String resolveName(Context context) {
        return context.getString(R.string.item_countdownCheckmark);
    }

    @Override
    public String resolveDescription(Context context) {
        return context.getString(R.string.item_countdownCheckmark_description);
    }

    @Override
    public View render(CountDownCheckmarkItem item, Activity context, LayoutInflater layoutInflater, ViewGroup parent, @NotNull ItemViewGeneratorBehavior behavior, @NotNull ItemInterface onItemClick, ItemViewGenerator itemViewGenerator, boolean previewMode, @NotNull Destroyer destroyer) {
        final ItemCountdownCheckmarkBinding binding = ItemCountdownCheckmarkBinding.inflate(layoutInflater, parent, false);

        TextItemRenderer.applyTextItemToTextView(context, item, binding.text, behavior, destroyer, previewMode);
        ItemViewGenerator.applyItemNotificationIndicator(context, item, binding.indicatorNotification, behavior, destroyer, previewMode);

        // custom applyCheckBox
        {
            CheckBox view = binding.checkbox;
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

        viewVisible(binding.countdown, item.isChecked(), View.GONE);
        binding.countdown.setText(item.getCountDownDisplay());

        return binding.getRoot();
    }
}
