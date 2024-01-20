package com.fazziclay.opentoday.gui.item.renderer;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.items.item.MissingNoItem;
import com.fazziclay.opentoday.databinding.ItemTextBinding;
import com.fazziclay.opentoday.gui.interfaces.ItemInterface;
import com.fazziclay.opentoday.gui.item.ItemViewGenerator;
import com.fazziclay.opentoday.gui.item.ItemViewGeneratorBehavior;
import com.fazziclay.opentoday.gui.item.registry.ItemRenderer;
import com.fazziclay.opentoday.gui.item.registry.NameResolver;
import com.fazziclay.opentoday.util.Destroyer;

import org.jetbrains.annotations.NotNull;

public class MissingNoRenderer implements ItemRenderer<MissingNoItem>, NameResolver {
    @Override
    public View render(MissingNoItem item, Activity context, LayoutInflater layoutInflater, ViewGroup parent, @NotNull ItemViewGeneratorBehavior behavior, @NotNull ItemInterface onItemClick, ItemViewGenerator itemViewGenerator, boolean previewMode, @NotNull Destroyer destroyer) {
        final ItemTextBinding binding = ItemTextBinding.inflate(layoutInflater, parent, false);
        binding.title.setText(R.string.item_missingNo);
        binding.title.setTextColor(Color.RED);
        binding.getRoot().setBackground(null);
        return binding.getRoot();
    }

    @Override
    public String resolveName(Context context) {
        return context.getString(R.string.item_missingNo);
    }

    @Override
    public String resolveDescription(Context context) {
        return context.getString(R.string.item_missingNo_description);
    }
}
