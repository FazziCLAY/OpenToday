package com.fazziclay.opentoday.gui.dialog;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.icons.IconsRegistry;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.function.Consumer;

public class IconSelectorDialog {

    private final Context context;
    private final Consumer<IconsRegistry.Icon> iconConsumer;
    private AlertDialog dialog;
    private boolean noneIsAvailable;

    public IconSelectorDialog(Context context, Consumer<IconsRegistry.Icon> iconConsumer) {
        this.context = context;
        this.iconConsumer = iconConsumer;
    }

    public void show() {
        var view = new LinearLayout(context);
        view.setOrientation(LinearLayout.VERTICAL);
        view.setPadding(20, 10, 10, 10);

        for (IconsRegistry.Icon icon : IconsRegistry.REGISTRY.getIconsList()) {
            if (icon == IconsRegistry.REGISTRY.NONE) {
                continue;
            }
            LinearLayout l = new LinearLayout(context);
            l.setOrientation(LinearLayout.HORIZONTAL);

            ImageView i = new ImageView(context);
            i.setLayoutParams(new LinearLayout.LayoutParams(70, 70));
            i.setScaleType(ImageView.ScaleType.FIT_XY);
            Drawable d = AppCompatResources.getDrawable(context, icon.getResId());
            i.setImageDrawable(d);
            l.addView(i);

            TextView t = new TextView(context);
            LinearLayout.LayoutParams tl = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tl.setMarginStart(7);
            tl.gravity = Gravity.CENTER;
            t.setTextSize(17);
            t.setLayoutParams(tl);
            t.setText(icon.getId());
            l.addView(t);

            l.setOnClickListener(v -> {
                iconConsumer.accept(icon);
                cancel();
            });
            view.addView(l);
        }

        var scroll = new ScrollView(context);
        scroll.addView(view);

        final var builder = new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.dialog_iconSelector_title)
                .setView(scroll)
                .setNegativeButton(R.string.abc_cancel, null);

        if (noneIsAvailable) {
            builder.setNeutralButton(R.string.dialog_iconSelector_none, (dialogInterface, i) -> iconConsumer.accept(IconsRegistry.REGISTRY.NONE));
        }

        dialog = builder.show();


    }

    private void cancel() {
        if (dialog != null) {
            dialog.cancel();
        }
    }

    public IconSelectorDialog noneIsAvailable(boolean b) {
        this.noneIsAvailable = b;
        return this;
    }
}
