package com.betterbrainmemory.opentoday.gui.item;

import static com.betterbrainmemory.opentoday.util.InlineUtil.viewVisible;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.betterbrainmemory.opentoday.R;
import com.betterbrainmemory.opentoday.app.items.callback.ItemCallback;
import com.betterbrainmemory.opentoday.app.items.item.Item;
import com.betterbrainmemory.opentoday.gui.interfaces.ItemInterface;
import com.betterbrainmemory.opentoday.gui.item.registry.ItemRenderer;
import com.betterbrainmemory.opentoday.gui.item.registry.ItemsGuiRegistry;
import com.betterbrainmemory.opentoday.util.ColorUtil;
import com.betterbrainmemory.opentoday.util.Destroyer;
import com.betterbrainmemory.opentoday.util.ResUtil;
import com.betterbrainmemory.opentoday.util.callback.CallbackImportance;
import com.betterbrainmemory.opentoday.util.callback.Status;

import org.jetbrains.annotations.NotNull;

/**
 * Make a android View from item
 */
public class ItemViewGenerator {
    private static final String TAG = "ItemViewGenerator";
    public static final String DESTROYED_CONST = "DESTROYED"; // constant value for Debug.DESTROY_ANY_TEXTITEM_CHILD

    @NonNull
    private final Activity activity;
    @NonNull
    private final LayoutInflater layoutInflater;
    private final boolean previewMode; // Disable items minimize view patch & disable buttons

    public ItemViewGenerator(@NonNull final Activity activity, final boolean previewMode) {
        this.activity = activity;
        this.layoutInflater = activity.getLayoutInflater();
        this.previewMode = previewMode;
    }

    // this method uses this.previewMode instead of method argument
    public View generate(final @NotNull Item item,
                         final @Nullable ViewGroup parent,
                         final @NotNull ItemViewGeneratorBehavior behavior,
                         final @NotNull Destroyer destroyer,
                         final @NotNull ItemInterface onItemClick) {
        return generate(item, parent, behavior, this.previewMode, destroyer, onItemClick);
    }

    public View generate(final @NotNull Item item,
                         final @Nullable ViewGroup parent,
                         final @NotNull ItemViewGeneratorBehavior behavior,
                         boolean previewMode,
                         final @NotNull Destroyer destroyer,
                         final @NotNull ItemInterface onItemClick) {

        final ItemRenderer<Item> renderer = ItemsGuiRegistry.REGISTRY.rendererForItem(item.getItemType());
        final View resultView = renderer.render(item, activity, layoutInflater, parent, behavior, onItemClick, this, previewMode, destroyer);

        final boolean isMinimized = behavior.isRenderMinimized(item);

        // Minimal height
        if (!isMinimized && !previewMode) resultView.setMinimumHeight(item.getViewMinHeight());

        // BackgroundColor
        if (item.isViewCustomBackgroundColor()) {
            resultView.setBackgroundTintList(ColorStateList.valueOf(item.getViewBackgroundColor()));
        }

        // foreground
        applyForeground(resultView, item, behavior);

        // view click
        resultView.setOnClickListener(view -> {
            item.dispatchClick();
            onItemClick.run(item);
        });

        return resultView;
    }

    private void applyForeground(View view, Item item, ItemViewGeneratorBehavior behavior) {
        view.setForeground(behavior.getForeground(item));
    }


    public static void applyItemNotificationIndicator(Context context, final Item item, final ImageView view, ItemViewGeneratorBehavior behavior, Destroyer destroyer, boolean previewMode) {
        Runnable updateRunnable = () -> {
            var color = ResUtil.getAttrColor(context, (item.getCachedNotificationStatus() ? R.attr.item_notificationIndicator_default : R.attr.item_notificationIndicator_disabledByTickPolicy));
            view.setImageTintList(ColorStateList.valueOf(color));
        };
        var itemCallback = new ItemCallback() {
            @Override
            public Status cachedNotificationStatusChanged(Item item, boolean isUpdateNotifications) {
                updateRunnable.run();
                return Status.NONE;
            }
        };
        item.getItemCallbacks().addCallback(CallbackImportance.LOW, itemCallback);
        destroyer.add(() -> item.getItemCallbacks().removeCallback(itemCallback));

        viewVisible(view, behavior.isRenderNotificationIndicator(item), View.GONE);
        updateRunnable.run();
    }


    public static SpannableString colorize(String text, int textColor) {
        return ColorUtil.colorize(text, textColor, Color.TRANSPARENT, Typeface.NORMAL);
    }


    public static void runFastChanges(Context context, ItemViewGeneratorBehavior behavior, int message, Runnable runnable) {
        runFastChanges(context, behavior, context.getString(message), runnable);
    }

    public static void runFastChanges(Context context, ItemViewGeneratorBehavior behavior, String message, Runnable runnable) {
        if (behavior.isConfirmFastChanges()) {
            new AlertDialog.Builder(context)
                    .setTitle(R.string.fastChanges_dialog_title)
                    .setMessage(message)
                    .setNeutralButton(R.string.fastChanges_dialog_dontAsk, (_ignore, __ignore) -> {
                        behavior.setConfirmFastChanges(false);
                        runnable.run();
                    })
                    .setNegativeButton(R.string.fastChanges_dialog_cancel, null)
                    .setPositiveButton(R.string.fastChanges_dialog_apply, (_ignore, __ignore) -> runnable.run())
                    .show();
        } else {
            runnable.run();
        }
    }
}
