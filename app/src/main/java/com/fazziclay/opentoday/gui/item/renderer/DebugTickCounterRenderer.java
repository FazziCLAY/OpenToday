package com.fazziclay.opentoday.gui.item.renderer;

import static com.fazziclay.opentoday.gui.item.ItemViewGenerator.applyItemNotificationIndicator;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fazziclay.opentoday.databinding.ItemLongtextBinding;
import com.fazziclay.opentoday.debug.DebugTickCounterItem;
import com.fazziclay.opentoday.gui.interfaces.ItemInterface;
import com.fazziclay.opentoday.gui.item.ItemViewGenerator;
import com.fazziclay.opentoday.gui.item.ItemViewGeneratorBehavior;
import com.fazziclay.opentoday.gui.item.registry.ItemRenderer;
import com.fazziclay.opentoday.gui.item.registry.NameResolver;
import com.fazziclay.opentoday.util.Destroyer;

import org.jetbrains.annotations.NotNull;

public class DebugTickCounterRenderer implements NameResolver, ItemRenderer<DebugTickCounterItem> {
    @Override
    public String resolveName(Context context) {
        return "Debug tick counter";
    }

    @Override
    public String resolveDescription(Context context) {
        return "Description";
    }

    @Override
    public View render(DebugTickCounterItem item, Activity context, LayoutInflater layoutInflater, ViewGroup parent, @NotNull ItemViewGeneratorBehavior behavior, @NotNull ItemInterface onItemClick, ItemViewGenerator itemViewGenerator, boolean previewMode, @NotNull Destroyer destroyer) {
        // TODO: Warning: DebugTickCounter uses LongText layout!
        final ItemLongtextBinding binding = ItemLongtextBinding.inflate(layoutInflater, parent, false);

        // Title
        TextItemRenderer.applyTextItemToTextView(context, item, binding.title, behavior, destroyer, previewMode);
        applyItemNotificationIndicator(context, item, binding.indicatorNotification, behavior, destroyer, previewMode);

        // Debugs
        binding.longText.setText(ItemViewGenerator.colorize(item.getDebugStat(), Color.WHITE));
        binding.longText.setTextSize(10);

        return binding.getRoot();
    }
}
