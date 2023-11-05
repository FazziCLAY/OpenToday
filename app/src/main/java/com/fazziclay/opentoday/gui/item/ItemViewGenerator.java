package com.fazziclay.opentoday.gui.item;

import static com.fazziclay.opentoday.util.InlineUtil.viewClick;
import static com.fazziclay.opentoday.util.InlineUtil.viewVisible;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.util.Linkify;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.fazziclay.opentoday.Debug;
import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.items.callback.ItemCallback;
import com.fazziclay.opentoday.app.items.item.CheckboxItem;
import com.fazziclay.opentoday.app.items.item.CounterItem;
import com.fazziclay.opentoday.app.items.item.CycleListItem;
import com.fazziclay.opentoday.app.items.item.DayRepeatableCheckboxItem;
import com.fazziclay.opentoday.app.items.item.DebugTickCounterItem;
import com.fazziclay.opentoday.app.items.item.FilterGroupItem;
import com.fazziclay.opentoday.app.items.item.GroupItem;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.ItemType;
import com.fazziclay.opentoday.app.items.item.ItemUtil;
import com.fazziclay.opentoday.app.items.item.LongTextItem;
import com.fazziclay.opentoday.app.items.item.MathGameItem;
import com.fazziclay.opentoday.app.items.item.SleepTimeItem;
import com.fazziclay.opentoday.app.items.item.TextItem;
import com.fazziclay.opentoday.databinding.ItemCheckboxBinding;
import com.fazziclay.opentoday.databinding.ItemCounterBinding;
import com.fazziclay.opentoday.databinding.ItemCycleListBinding;
import com.fazziclay.opentoday.databinding.ItemDayRepeatableCheckboxBinding;
import com.fazziclay.opentoday.databinding.ItemFilterGroupBinding;
import com.fazziclay.opentoday.databinding.ItemGroupBinding;
import com.fazziclay.opentoday.databinding.ItemLongtextBinding;
import com.fazziclay.opentoday.databinding.ItemMathGameBinding;
import com.fazziclay.opentoday.databinding.ItemSleepTimeBinding;
import com.fazziclay.opentoday.databinding.ItemTextBinding;
import com.fazziclay.opentoday.gui.interfaces.ItemInterface;
import com.fazziclay.opentoday.util.ColorUtil;
import com.fazziclay.opentoday.util.DebugUtil;
import com.fazziclay.opentoday.util.Logger;
import com.fazziclay.opentoday.util.RandomUtil;
import com.fazziclay.opentoday.util.ResUtil;
import com.fazziclay.opentoday.util.annotation.ForItem;
import com.fazziclay.opentoday.util.callback.CallbackImportance;
import com.fazziclay.opentoday.util.callback.Status;
import com.fazziclay.opentoday.util.time.ConvertMode;
import com.fazziclay.opentoday.util.time.TimeUtil;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Make a android View from item
 */
public class ItemViewGenerator {
    private static final String TAG = "ItemViewGenerator";
    private static final String DESTROYED_CONST = "DESTROYED"; // constant value for Debug.DESTROY_ANY_TEXTITEM_CHILD

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

        final ItemType type = item.getItemType();
        final View resultView = switch (type) {
            case MISSING_NO -> generateMissingNoItem(parent);
            case TEXT -> generateTextItemView((TextItem) item, parent, behavior, previewMode, destroyer);
            case DEBUG_TICK_COUNTER -> generateDebugTickCounterItemView((DebugTickCounterItem) item, parent, behavior, previewMode, destroyer);
            case LONG_TEXT -> generateLongTextItemView((LongTextItem) item, parent, behavior, previewMode, destroyer);
            case CHECKBOX -> generateCheckboxItemView((CheckboxItem) item, parent, behavior, previewMode, destroyer);
            case CHECKBOX_DAY_REPEATABLE -> generateDayRepeatableCheckboxItemView((DayRepeatableCheckboxItem) item, parent, behavior, previewMode, destroyer);
            case COUNTER -> generateCounterItemView((CounterItem) item, parent, behavior, previewMode, destroyer);
            case CYCLE_LIST -> generateCycleListItemView((CycleListItem) item, parent, behavior, previewMode, destroyer, onItemClick);
            case GROUP -> generateGroupItemView((GroupItem) item, parent, behavior, previewMode, destroyer, onItemClick);
            case FILTER_GROUP -> generateFilterGroupItemView((FilterGroupItem) item, parent, behavior, previewMode, destroyer, onItemClick);
            case MATH_GAME -> generateMathGameItemView((MathGameItem) item, parent, behavior, previewMode, destroyer);
            case SLEEP_TIME -> generateSleepTimeItemView((SleepTimeItem) item, parent, behavior, previewMode, destroyer);
            default -> {
                final UnsupportedOperationException exception = new UnsupportedOperationException(TAG + " can't generate view because itemType=" + type + " currently not supported... Check " + TAG + " for fix this!");
                Logger.e(TAG, "Unexpected item type to generate view. (wait 3000ms in DebugUtil.sleep())", exception);
                DebugUtil.sleep(3000);
                throw exception;
            }
        };

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


    @ForItem(k = ItemType.MISSING_NO)
    private View generateMissingNoItem(@Nullable ViewGroup parent) {
        final ItemTextBinding binding = ItemTextBinding.inflate(layoutInflater, parent, false);
        binding.title.setText(R.string.item_missingNo);
        binding.title.setTextColor(Color.RED);
        binding.getRoot().setBackground(null);
        return binding.getRoot();
    }

    @ForItem(k = ItemType.MATH_GAME)
    private View generateMathGameItemView(MathGameItem item,
                                          ViewGroup parent,
                                          ItemViewGeneratorBehavior behavior,
                                          boolean previewMode,
                                          Destroyer destroyer) {
        final ItemMathGameBinding binding = ItemMathGameBinding.inflate(this.layoutInflater, parent, false);

        final MathGameInterface gameInterface = new MathGameInterface() {
            private String currentNumberStr = "0";
            private int currentNumber = 0;

            public void numberPress(byte b) {
                if (b < 0 || b > 9) {
                    throw new IllegalArgumentException("OutOfRange of numberPress(0-9): " + b);
                }
                currentNumberStr += b;
                try {
                    currentNumber = Integer.parseInt(currentNumberStr);
                } catch (Exception ignored) {
                    currentNumber = RandomUtil.nextInt(); // easter egg number (if number out of int(32-bits))
                }
                currentNumberStr = String.valueOf(currentNumber);
                updateDisplay();
            }

            public void donePress() {
                final int color;
                final boolean right = item.isResultRight(currentNumber);
                if (right) {
                    color = Color.GREEN;
                    item.postResult(currentNumber);
                    binding.questText.setText(item.getQuestText());
                    binding.questText.setTextSize(item.getQuestTextSize());
                    binding.questText.setGravity(item.getQuestTextGravity());
                } else {
                    color = Color.RED;
                }

                clearCurrentInput();

                final ValueAnimator animator = ValueAnimator.ofObject(new ArgbEvaluator(), color, Color.TRANSPARENT);
                animator.setDuration(right ? 1000 : 512);
                animator.setInterpolator(right ? new DecelerateInterpolator() : new AccelerateInterpolator());
                animator.addUpdateListener(valueAnimator -> {
                    binding.userEnterNumber.setBackgroundTintList(ColorStateList.valueOf(((int) valueAnimator.getAnimatedValue())));
                });
                animator.start();
            }

            public void clearCurrentInput() {
                setCurrentInput(0);
            }

            private void setCurrentInput(int v) {
                currentNumber = v;
                currentNumberStr = String.valueOf(v);
                updateDisplay();
            }

            private void updateDisplay() {
                binding.userEnterNumber.setText(currentNumberStr);
            }

            public void invert() {
                setCurrentInput(-currentNumber);
            }

            @Override
            public void init() {
                binding.questText.setText(item.getQuestText());
                binding.questText.setTextSize(item.getQuestTextSize());
                binding.questText.setGravity(item.getQuestTextGravity());

                binding.userEnterNumber.setText(currentNumberStr);
                viewClick(binding.userEnterNumber, this::invert);

                viewClick(binding.number0, () -> numberPress((byte) 0));
                viewClick(binding.number1, () -> numberPress((byte) 1));
                viewClick(binding.number2, () -> numberPress((byte) 2));
                viewClick(binding.number3, () -> numberPress((byte) 3));
                viewClick(binding.number4, () -> numberPress((byte) 4));
                viewClick(binding.number5, () -> numberPress((byte) 5));
                viewClick(binding.number6, () -> numberPress((byte) 6));
                viewClick(binding.number7, () -> numberPress((byte) 7));
                viewClick(binding.number8, () -> numberPress((byte) 8));
                viewClick(binding.number9, () -> numberPress((byte) 9));
                viewClick(binding.numberClear, this::clearCurrentInput);
                viewClick(binding.numberNext, this::donePress);
            }
        };

        // Text
        applyTextItemToTextView(item, binding.title, behavior, destroyer, previewMode);
        applyItemNotificationIndicator(item, binding.indicatorNotification, behavior, destroyer, previewMode);
        gameInterface.init();

        binding.keyboard.setEnabled(!previewMode);
        binding.userEnterNumber.setEnabled(!previewMode);
        binding.questText.setEnabled(!previewMode);
        binding.numberClear.setEnabled(!previewMode);
        binding.numberNext.setEnabled(!previewMode);
        binding.number0.setEnabled(!previewMode);
        binding.number1.setEnabled(!previewMode);
        binding.number2.setEnabled(!previewMode);
        binding.number3.setEnabled(!previewMode);
        binding.number4.setEnabled(!previewMode);
        binding.number5.setEnabled(!previewMode);
        binding.number6.setEnabled(!previewMode);
        binding.number7.setEnabled(!previewMode);
        binding.number8.setEnabled(!previewMode);
        binding.number9.setEnabled(!previewMode);


        if (behavior.isRenderMinimized(item)) {
            binding.keyboard.setVisibility(View.GONE);
            binding.userEnterNumber.setVisibility(View.GONE);
            binding.questText.setGravity(Gravity.NO_GRAVITY);
            binding.questText.setTextSize(18);
        }

        return binding.getRoot();
    }

    @ForItem(k = ItemType.MATH_GAME)
    private interface MathGameInterface {
        void init();
    }

    @ForItem(k = ItemType.SLEEP_TIME)
    private View generateSleepTimeItemView(SleepTimeItem item,
                                           ViewGroup parent,
                                           ItemViewGeneratorBehavior behavior,
                                           boolean previewMode,
                                           Destroyer destroyer) {
        final ItemSleepTimeBinding binding = ItemSleepTimeBinding.inflate(this.layoutInflater, parent, false);

        applyTextItemToTextView(item, binding.title, behavior, destroyer, previewMode);
        applyItemNotificationIndicator(item, binding.indicatorNotification, behavior, destroyer, previewMode);

        final String s = item.getSleepTextPattern()
                .replace("$(elapsed)", TimeUtil.convertToHumanTime(item.getElapsedTime(), ConvertMode.HHMM))
                .replace("$(elapsedToStartSleep)", TimeUtil.convertToHumanTime(item.getElapsedTimeToStartSleep(), ConvertMode.HHMM))
                .replace("$(current)", TimeUtil.convertToHumanTime(TimeUtil.getDaySeconds(), ConvertMode.HHMM))
                .replace("$(wakeUpForRequired)", TimeUtil.convertToHumanTime(item.getWakeUpForRequiredAtCurr(), ConvertMode.HHMM))
                .replace("$(wakeUpTime)", TimeUtil.convertToHumanTime(item.getWakeUpTime(), ConvertMode.HHMM))
                .replace("$(requiredSleepTime)", TimeUtil.convertToHumanTime(item.getRequiredSleepTime(), ConvertMode.HHMM));

        binding.description.setText(colorize(s, item.getTextColor()));

        return binding.getRoot();
    }

    @ForItem(k = ItemType.LONG_TEXT)
    public View generateLongTextItemView(final LongTextItem item,
                                         final ViewGroup parent,
                                         ItemViewGeneratorBehavior behavior,
                                         boolean previewMode,
                                         Destroyer destroyer) {
        final ItemLongtextBinding binding = ItemLongtextBinding.inflate(this.layoutInflater, parent, false);

        // Text
        applyTextItemToTextView(item, binding.title, behavior, destroyer, previewMode);
        applyLongTextItemToLongTextView(item, binding.longText);
        applyItemNotificationIndicator(item, binding.indicatorNotification, behavior, destroyer, previewMode);

        return binding.getRoot();
    }

    @ForItem(k = ItemType.DEBUG_TICK_COUNTER)
    private View generateDebugTickCounterItemView(final DebugTickCounterItem item, final ViewGroup parent, ItemViewGeneratorBehavior behavior, boolean previewMode, Destroyer destroyer) {
        // Warning: DebugTickCounter uses LongText layout!
        final ItemLongtextBinding binding = ItemLongtextBinding.inflate(this.layoutInflater, parent, false);

        // Title
        applyTextItemToTextView(item, binding.title, behavior, destroyer, previewMode);
        applyItemNotificationIndicator(item, binding.indicatorNotification, behavior, destroyer, previewMode);

        // Debugs
        binding.longText.setText(colorize(item.getDebugStat(), Color.WHITE));
        binding.longText.setTextSize(10);

        return binding.getRoot();
    }

    @ForItem(k = ItemType.FILTER_GROUP)
    private View generateFilterGroupItemView(final FilterGroupItem item, final ViewGroup parent, ItemViewGeneratorBehavior behavior, boolean previewMode, Destroyer destroyer, ItemInterface onItemClick) {
        final ItemFilterGroupBinding binding = ItemFilterGroupBinding.inflate(this.layoutInflater, parent, false);

        // Text
        applyTextItemToTextView(item, binding.title, behavior, destroyer, previewMode);
        applyItemNotificationIndicator(item, binding.indicatorNotification, behavior, destroyer, previewMode);

        // FilterGroup
        if (!behavior.isRenderMinimized(item)) {
            var drawer = createItemsStorageDrawerForFilterGroupItem(item, binding.content, behavior, previewMode, behavior.getItemsStorageDrawerBehavior(item), onItemClick);
            drawer.create();
            destroyer.add(drawer::destroy);
        }

        binding.externalEditor.setEnabled(!previewMode);
        binding.externalEditor.setOnClickListener(_ignore -> behavior.onFilterGroupEdit(item));

        return binding.getRoot();
    }

    private ItemsStorageDrawer createItemsStorageDrawerForFilterGroupItem(FilterGroupItem item, RecyclerView content, ItemViewGeneratorBehavior behavior, boolean previewMode, ItemsStorageDrawerBehavior itemsStorageDrawerBehavior, ItemInterface onItemClick) {
        return ItemsStorageDrawer.builder(activity, itemsStorageDrawerBehavior, behavior, App.get(activity).getSelectionManager(), item)
                .setView(content)
                .setDragsEnable(false)
                .setSwipesEnable(false)
                .setOnItemClick(onItemClick)
                .setPreviewMode(previewMode)
                .setItemViewWrapper((_iterItem, viewSupplier, destroyer) -> {
                    if (itemsStorageDrawerBehavior.ignoreFilterGroup()) {
                        return viewSupplier.get();
                    }
                    if (item.isActiveItem(_iterItem)) {
                        return viewSupplier.get();
                    }
                    return null;
                })
                .build();
    }

    @ForItem(k = ItemType.GROUP)
    private View generateGroupItemView(GroupItem item, ViewGroup parent, ItemViewGeneratorBehavior behavior, boolean previewMode, Destroyer destroyer, ItemInterface onItemClick) {
        final ItemGroupBinding binding = ItemGroupBinding.inflate(this.layoutInflater, parent, false);

        // Text
        applyTextItemToTextView(item, binding.title, behavior, destroyer, previewMode);
        applyItemNotificationIndicator(item, binding.indicatorNotification, behavior, destroyer, previewMode);

        // Group
        if (!behavior.isRenderMinimized(item)) {
            var drawer = createItemsStorageDrawerForGroupItem(item, binding.content, behavior, previewMode, behavior.getItemsStorageDrawerBehavior(item), onItemClick);
            drawer.create();
            destroyer.add(drawer::destroy);
        }

        binding.externalEditor.setEnabled(!previewMode);
        binding.externalEditor.setOnClickListener(_ignore -> behavior.onGroupEdit(item));

        return binding.getRoot();
    }

    private ItemsStorageDrawer createItemsStorageDrawerForGroupItem(GroupItem item, RecyclerView content, ItemViewGeneratorBehavior behavior, boolean previewMode, ItemsStorageDrawerBehavior itemsStorageDrawerBehavior, ItemInterface onItemClick) {
        return ItemsStorageDrawer.builder(activity, itemsStorageDrawerBehavior, behavior, App.get(activity).getSelectionManager(), item)
                .setView(content)
                .setOnItemClick(onItemClick)
                .setPreviewMode(previewMode)
                .build();
    }

    @ForItem(k = ItemType.COUNTER)
    public View generateCounterItemView(CounterItem item, ViewGroup parent, ItemViewGeneratorBehavior behavior, boolean previewMode, Destroyer destroyer) {
        final ItemCounterBinding binding = ItemCounterBinding.inflate(this.layoutInflater, parent, false);

        // Title
        applyTextItemToTextView(item, binding.title, behavior, destroyer, previewMode);
        applyItemNotificationIndicator(item, binding.indicatorNotification, behavior, destroyer, previewMode);

        // Counter
        viewClick(binding.up, () -> runFastChanges(behavior, R.string.item_counter_fastChanges_up, item::up));
        viewClick(binding.down, () -> runFastChanges(behavior, R.string.item_counter_fastChanges_down, item::down));
        binding.up.setEnabled(!previewMode);
        binding.down.setEnabled(!previewMode);

        binding.counter.setText(String.valueOf(item.getCounter()));

        return binding.getRoot();
    }

    @ForItem(k = ItemType.CYCLE_LIST)
    public View generateCycleListItemView(CycleListItem item, ViewGroup parent, ItemViewGeneratorBehavior behavior, boolean previewMode, Destroyer destroyer, ItemInterface onItemClick) {
        final ItemCycleListBinding binding = ItemCycleListBinding.inflate(this.layoutInflater, parent, false);

        // Text
        applyTextItemToTextView(item, binding.title, behavior, destroyer, previewMode);
        applyItemNotificationIndicator(item, binding.indicatorNotification, behavior, destroyer, previewMode);

        // CycleList
        binding.next.setEnabled(!previewMode);
        binding.next.setOnClickListener(v -> item.next());
        binding.previous.setEnabled(!previewMode);
        binding.previous.setOnClickListener(v -> item.previous());

        binding.externalEditor.setEnabled(!previewMode);
        binding.externalEditor.setOnClickListener(_ignore -> behavior.onCycleListEdit(item));

        if (!behavior.isRenderMinimized(item)) {
            final var drawer = new CurrentItemStorageDrawer(this.activity, binding.content, this, behavior, item, destroyer, onItemClick);
            drawer.setOnUpdateListener(currentItem -> {
                viewVisible(binding.empty, currentItem == null, View.GONE);
                return Status.NONE;
            });
            drawer.create();
            destroyer.add(drawer::destroy);
        } else {
            binding.empty.setVisibility(View.GONE);
        }

        return binding.getRoot();
    }

    @ForItem(k = ItemType.CHECKBOX_DAY_REPEATABLE)
    public View generateDayRepeatableCheckboxItemView(DayRepeatableCheckboxItem item, ViewGroup parent, ItemViewGeneratorBehavior behavior, boolean previewMode, Destroyer destroyer) {
        final ItemDayRepeatableCheckboxBinding binding = ItemDayRepeatableCheckboxBinding.inflate(this.layoutInflater, parent, false);

        applyTextItemToTextView(item, binding.text, behavior, destroyer, previewMode);
        applyCheckItemToCheckBoxView(item, binding.checkbox, behavior, previewMode);
        applyItemNotificationIndicator(item, binding.indicatorNotification, behavior, destroyer, previewMode);

        return binding.getRoot();
    }

    @ForItem(k = ItemType.CHECKBOX)
    public View generateCheckboxItemView(CheckboxItem item, ViewGroup parent, ItemViewGeneratorBehavior behavior, boolean previewMode, Destroyer destroyer) {
        final ItemCheckboxBinding binding = ItemCheckboxBinding.inflate(this.layoutInflater, parent, false);

        applyTextItemToTextView(item, binding.text, behavior, destroyer, previewMode);
        applyCheckItemToCheckBoxView(item, binding.checkbox, behavior, previewMode);
        applyItemNotificationIndicator(item, binding.indicatorNotification, behavior, destroyer, previewMode);

        return binding.getRoot();
    }

    @ForItem(k = ItemType.TEXT)
    public View generateTextItemView(TextItem item, ViewGroup parent, ItemViewGeneratorBehavior behavior, boolean previewMode, Destroyer destroyer) {
        final ItemTextBinding binding = ItemTextBinding.inflate(this.layoutInflater, parent, false);

        // Text
        applyTextItemToTextView(item, binding.title, behavior, destroyer, previewMode);
        applyItemNotificationIndicator(item, binding.indicatorNotification, behavior, destroyer, previewMode);

        return binding.getRoot();
    }

    private void applyItemNotificationIndicator(final Item item, final ImageView view, ItemViewGeneratorBehavior behavior, Destroyer destroyer, boolean previewMode) {
        Runnable updateRunnable = () -> {
            var color = ResUtil.getAttrColor(activity, (item.getCachedNotificationStatus() ? R.attr.item_notificationIndicator_default : R.attr.item_notificationIndicator_disabledByTickPolicy));
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

    //
    @SuppressLint("SetTextI18n")
    private void applyTextItemToTextView(final TextItem item, final TextView view, ItemViewGeneratorBehavior behavior, Destroyer destroyer, boolean previewMode) {
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
                view.setText(DESTROYED_CONST);
                view.setTextSize(15);
                view.setBackgroundColor(Color.BLACK);
            });
        }

        final int textColor = item.isCustomTextColor() ? item.getTextColor() : ResUtil.getAttrColor(activity, R.attr.item_textColor);
        final SpannableString visibleText = item.isParagraphColorize() ? colorize(item.getText(), textColor) : SpannableString.valueOf(item.getText());
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

    private SpannableString colorize(String text, int textColor) {
        return ColorUtil.colorize(text, textColor, Color.TRANSPARENT, Typeface.NORMAL);
    }

    private void applyForeground(View view, Item item, ItemViewGeneratorBehavior behavior) {
        view.setForeground(behavior.getForeground(item));
    }

    private void applyLongTextItemToLongTextView(final LongTextItem item, final TextView view) {
        final int longTextColor = item.isCustomLongTextColor() ? item.getLongTextColor() : ResUtil.getAttrColor(activity, R.attr.item_textColor);
        final SpannableString visibleText = item.isParagraphColorize() ? colorize(item.getLongText(), longTextColor) : SpannableString.valueOf(item.getLongText());
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
        if (item.isCustomLongTextSize()) view.setTextSize(item.getLongTextSize());
        if (item.isCustomLongTextColor()) {
            view.setTextColor(ColorStateList.valueOf(longTextColor));
        }
        if (item.isLongTextClickableUrls()) Linkify.addLinks(view, Linkify.ALL);
    }

    private void applyCheckItemToCheckBoxView(final CheckboxItem item, final CheckBox view, ItemViewGeneratorBehavior behavior, boolean previewMode) {
        view.setChecked(item.isChecked());
        view.setEnabled(!previewMode);
        viewClick(view, () -> {
            boolean to = view.isChecked();
            view.setChecked(!to);
            runFastChanges(behavior, to ? R.string.item_checkbox_fastChanges_checked : R.string.item_checkbox_fastChanges_unchecked, () -> {
                view.setChecked(to);
                item.setChecked(to);
                item.visibleChanged();
                item.save();
            });
        });
    }

    @SuppressWarnings("unused")
    private String getString(int resId, Object... formatArgs) {
        return activity.getString(resId, formatArgs);
    }

    private String getString(int resId) {
        return activity.getString(resId);
    }

    private void runFastChanges(ItemViewGeneratorBehavior behavior, int message, Runnable runnable) {
        runFastChanges(behavior, getString(message), runnable);
    }

    private void runFastChanges(ItemViewGeneratorBehavior behavior, String message, Runnable runnable) {
        if (behavior.isConfirmFastChanges()) {
            new AlertDialog.Builder(activity)
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
