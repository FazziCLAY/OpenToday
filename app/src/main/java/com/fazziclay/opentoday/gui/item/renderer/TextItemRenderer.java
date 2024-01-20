package com.fazziclay.opentoday.gui.item.renderer;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fazziclay.opentoday.Debug;
import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.items.item.ItemUtil;
import com.fazziclay.opentoday.app.items.item.TextItem;
import com.fazziclay.opentoday.databinding.ItemTextBinding;
import com.fazziclay.opentoday.gui.interfaces.ItemInterface;
import com.fazziclay.opentoday.gui.item.ItemViewGenerator;
import com.fazziclay.opentoday.gui.item.ItemViewGeneratorBehavior;
import com.fazziclay.opentoday.gui.item.registry.ItemRenderer;
import com.fazziclay.opentoday.gui.item.registry.NameResolver;
import com.fazziclay.opentoday.util.ColorUtil;
import com.fazziclay.opentoday.util.Destroyer;
import com.fazziclay.opentoday.util.RandomUtil;
import com.fazziclay.opentoday.util.ResUtil;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class TextItemRenderer implements NameResolver, ItemRenderer<TextItem> {
    @Override
    public String resolveName(Context context) {
        return context.getString(R.string.item_text);
    }

    @Override
    public String resolveDescription(Context context) {
        return context.getString(R.string.item_text_description);
    }

    @Override
    public View render(TextItem item, Activity context, LayoutInflater layoutInflater, ViewGroup parent, @NotNull ItemViewGeneratorBehavior behavior, @NotNull ItemInterface onItemClick, ItemViewGenerator itemViewGenerator, boolean previewMode, @NotNull Destroyer destroyer) {
        final ItemTextBinding binding = ItemTextBinding.inflate(layoutInflater, parent, false);

        // Text
        applyTextItemToTextView(context, item, binding.title, behavior, destroyer, previewMode);
        ItemViewGenerator.applyItemNotificationIndicator(context, item, binding.indicatorNotification, behavior, destroyer, previewMode);

        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    public static void applyTextItemToTextView(Context context, final TextItem item, final TextView view, ItemViewGeneratorBehavior behavior, Destroyer destroyer, boolean previewMode) {
        if (Debug.SHOW_PATH_TO_ITEM_ON_ITEMTEXT) {
            view.setText(Arrays.toString(ItemUtil.getPathToItem(item)));
            view.setTextSize(15);
            view.setTextColor(Color.RED);
            view.setBackgroundColor(Color.BLACK);
            return;
        }
        if (Debug.SHOW_ID_ON_ITEMTEXT) {
            view.setText(ColorUtil.colorize(item.getText() + "\n$[-#aaaaaa;S12]" + item.getId(), Color.WHITE, Color.TRANSPARENT, Typeface.NORMAL));
            view.setTextSize(17);
            return;
        }

        if (Debug.SHOW_GEN_ID_ON_ITEMTEXT) {
            view.setText(ColorUtil.colorize(item.getText() + "\n$[-#aaaaaa;S12]" + RandomUtil.bounds(-9999, 9999), Color.WHITE, Color.TRANSPARENT, Typeface.NORMAL));
            view.setTextSize(17);
            return;
        }
        if (Debug.DESTROY_ANY_TEXTITEM_CHILD) {
            destroyer.add(() -> {
                view.setTextColor(Color.RED);
                view.setText(ItemViewGenerator.DESTROYED_CONST);
                view.setTextSize(15);
                view.setBackgroundColor(Color.BLACK);
            });
        }

        final int textColor = item.isCustomTextColor() ? item.getTextColor() : ResUtil.getAttrColor(context, R.attr.item_textColor);
        final SpannableString visibleText = item.isFormatting() ? ItemViewGenerator.colorize(item.getText(), textColor) : SpannableString.valueOf(item.getText());
        final int MAX = 100;
        if (!previewMode && item.isMinimize()) {
            final String text = visibleText.toString();
            if (text.length() > MAX) {
                view.setText(new SpannableStringBuilder().append(visibleText.subSequence(0, MAX - 3)).append("…"));
            } else {
                view.setText(visibleText);
            }
        } else {
            view.setText(visibleText);
        }
        if (item.isCustomTextColor()) {
            view.setTextColor(ColorStateList.valueOf(textColor));
        }
        if (item.isClickableUrls()) Linkify.addLinks(view, Linkify.ALL);
    }
}
