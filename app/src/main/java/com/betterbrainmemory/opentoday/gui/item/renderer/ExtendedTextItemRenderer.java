package com.betterbrainmemory.opentoday.gui.item.renderer;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.betterbrainmemory.opentoday.R;
import com.betterbrainmemory.opentoday.app.items.item.ExtendedTextItem;
import com.betterbrainmemory.opentoday.databinding.ItemLongtextBinding;
import com.betterbrainmemory.opentoday.gui.interfaces.ItemInterface;
import com.betterbrainmemory.opentoday.gui.item.ItemViewGenerator;
import com.betterbrainmemory.opentoday.gui.item.ItemViewGeneratorBehavior;
import com.betterbrainmemory.opentoday.gui.item.registry.ItemRenderer;
import com.betterbrainmemory.opentoday.gui.item.registry.NameResolver;
import com.betterbrainmemory.opentoday.util.Destroyer;
import com.betterbrainmemory.opentoday.util.ResUtil;

import org.jetbrains.annotations.NotNull;

public class ExtendedTextItemRenderer implements ItemRenderer<ExtendedTextItem>, NameResolver {
    @Override
    public String resolveName(Context context) {
        return context.getString(R.string.item_longTextItem);
    }

    @Override
    public String resolveDescription(Context context) {
        return context.getString(R.string.item_longTextItem_description);
    }

    @Override
    public View render(ExtendedTextItem item, Activity context, LayoutInflater layoutInflater, ViewGroup parent, @NotNull ItemViewGeneratorBehavior behavior, @NotNull ItemInterface onItemClick, ItemViewGenerator itemViewGenerator, boolean previewMode, @NotNull Destroyer destroyer) {
        final ItemLongtextBinding binding = ItemLongtextBinding.inflate(layoutInflater, parent, false);

        // Text
        TextItemRenderer.applyTextItemToTextView(context, item, binding.title, behavior, destroyer, previewMode);
        applyLongTextItemToLongTextView(context, item, binding.longText, previewMode);
        ItemViewGenerator.applyItemNotificationIndicator(context, item, binding.indicatorNotification, behavior, destroyer, previewMode);

        return binding.getRoot();
    }

    public static void applyLongTextItemToLongTextView(Context context, final ExtendedTextItem item, final TextView view, boolean previewMode) {
        final int longTextColor = item.isCustomAddictionTextColor() ? item.getAddictionTextColor() : ResUtil.getAttrColor(context, R.attr.item_textColor);
        final SpannableString visibleText = item.isFormatting() ? ItemViewGenerator.colorize(item.getAddictionText(), longTextColor) : SpannableString.valueOf(item.getAddictionText());
        final int MAX = 170;
        if (!previewMode && item.isMinimize()) {
            if (visibleText.length() > MAX) {
                view.setText(new SpannableStringBuilder().append(visibleText.subSequence(0, MAX - 3)).append("…"));
            } else {
                view.setText(visibleText);
            }
        } else {
            view.setText(visibleText);
        }
        if (item.isCustomAddictionTextSize()) view.setTextSize(item.getAddictionTextSize());
        if (item.isCustomAddictionTextColor()) {
            view.setTextColor(ColorStateList.valueOf(longTextColor));
        }
        if (item.isAddictionTextClickableUrls()) Linkify.addLinks(view, Linkify.ALL);
    }
}
