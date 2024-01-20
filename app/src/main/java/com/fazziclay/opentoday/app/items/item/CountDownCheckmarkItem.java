package com.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.app.items.tick.TickSession;
import com.fazziclay.opentoday.util.time.TimeUtil;

import java.util.Objects;

public class CountDownCheckmarkItem extends CheckboxItem {
    private static final String DISPLAY_CACHE_FALLBACK = "?";
    private static final String TAG = "CountDownCheckmarkItem";

    public static final CountDownCheckmarkItem.CountDownCheckmarkItemCodec CODEC = new CountDownCheckmarkItemCodec();
    public static final ItemFactory<CountDownCheckmarkItem> FACTORY = new CountDownCheckmarkItemFactory();


    private long checkedAt;
    private long stepSize = TimeUtil.MILLISECONDS_IN_DAY;
    private int steps = 2;
    private boolean roundToDays = false;

    private String displayCache;


    public CountDownCheckmarkItem() {
        super();
        this.displayCache = DISPLAY_CACHE_FALLBACK;
    }


    // copy
    public CountDownCheckmarkItem(@Nullable CountDownCheckmarkItem copy) {
        super(copy);
        this.displayCache = DISPLAY_CACHE_FALLBACK;
        if (copy != null) {
            this.checkedAt = copy.checkedAt;
            this.stepSize = copy.stepSize;
            this.steps = copy.steps;
            this.roundToDays = copy.roundToDays;
        }
    }

    // append
    public CountDownCheckmarkItem(@Nullable CheckboxItem copy) {
        super(copy);
        this.displayCache = DISPLAY_CACHE_FALLBACK;
    }

    // append
    public CountDownCheckmarkItem(@Nullable TextItem copy) {
        super(copy, false);
        this.displayCache = DISPLAY_CACHE_FALLBACK;
    }

    public CountDownCheckmarkItem(TextItem textItem, boolean checked, long stepSize, int steps, boolean roundToDays) {
        super(textItem, checked);
        this.stepSize = stepSize;
        this.steps = steps;
        this.roundToDays = roundToDays;
    }

    @Override
    public void tick(TickSession tickSession) {
        if (isChecked()) {
            long current = tickSession.getGregorianCalendar().getTimeInMillis();
            long diffWithCheck;
            if (roundToDays) {
                diffWithCheck = TimeUtil.noTimeUnixTimestamp(current) - TimeUtil.noTimeUnixTimestamp(checkedAt);
            } else {
                diffWithCheck = current - checkedAt;
            }
            long stepsPassed = diffWithCheck / stepSize;

            int countdown = steps - Math.toIntExact(stepsPassed);
            if (countdown <= 0) {
                setChecked(false);
                tickSession.saveNeeded();
                getItemCallbacks().run((callbackStorage, callback) -> callback.countDownCheckmarkStopped(CountDownCheckmarkItem.this, false));
                visibleChanged();
            } else {
                String toDisplay = String.valueOf(countdown);
                if (!Objects.equals(displayCache, toDisplay)) {
                    displayCache = toDisplay;
                    visibleChanged();
                }
            }
        }

        super.tick(tickSession);
    }

    public String getCountDownDisplay() {
        return displayCache;
    }

    @Override
    public void setChecked(boolean s) {
        if (s) {
            checkedAt = TickSession.getLatestGregorianCalendar().getTimeInMillis();
            displayCache = String.valueOf(steps);
        } else {
            checkedAt = 0;
            displayCache = DISPLAY_CACHE_FALLBACK;
        }
        // before sets checkedTime because this may call instant tick for this checkboxItem
        super.setChecked(s);
    }

    public long getStepSize() {
        return stepSize;
    }

    public void setStepSize(long stepSize) {
        this.stepSize = stepSize;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public boolean isRoundToDays() {
        return roundToDays;
    }

    public void setRoundToDays(boolean roundToDays) {
        this.roundToDays = roundToDays;
    }



    // Import - Export - Factory
    public static class CountDownCheckmarkItemCodec extends CheckboxItem.CheckboxItemCodec {
        private static final String KEY_COUNTDOWN_CHECKED_AT = "countdown_checked_time";
        private static final String KEY_COUNTDOWN_STEP_SIZE = "countdown_step_size";
        private static final String KEY_COUNTDOWN_STEPS = "countdown_steps";
        private static final String KEY_COUNTDOWN_ROUND_TO_DAYS = "countdown_round_to_days";

        @NonNull
        @Override
        public Cherry exportItem(@NonNull Item item) {
            CountDownCheckmarkItem countDownCheckmarkItem = (CountDownCheckmarkItem) item;
            return super.exportItem(countDownCheckmarkItem)
                    .put(KEY_COUNTDOWN_CHECKED_AT, countDownCheckmarkItem.checkedAt)
                    .put(KEY_COUNTDOWN_STEP_SIZE, countDownCheckmarkItem.stepSize)
                    .put(KEY_COUNTDOWN_STEPS, countDownCheckmarkItem.steps)
                    .put(KEY_COUNTDOWN_ROUND_TO_DAYS, countDownCheckmarkItem.roundToDays);
        }

        private final CountDownCheckmarkItem DEFAULT_VALUES = new CountDownCheckmarkItem();
        @NonNull
        @Override
        public Item importItem(@NonNull Cherry cherry, Item item) {
            var cdcm = fallback(item, CountDownCheckmarkItem::new);
            super.importItem(cherry, cdcm);

            cdcm.checkedAt = cherry.optLong(KEY_COUNTDOWN_CHECKED_AT, DEFAULT_VALUES.checkedAt);
            cdcm.stepSize = cherry.optLong(KEY_COUNTDOWN_STEP_SIZE, DEFAULT_VALUES.stepSize);
            cdcm.steps = cherry.optInt(KEY_COUNTDOWN_STEPS, DEFAULT_VALUES.steps);
            cdcm.roundToDays = cherry.optBoolean(KEY_COUNTDOWN_ROUND_TO_DAYS, DEFAULT_VALUES.roundToDays);

            return cdcm;
        }
    }

    private static class CountDownCheckmarkItemFactory implements ItemFactory<CountDownCheckmarkItem> {
        @Override
        public CountDownCheckmarkItem create() {
            return new CountDownCheckmarkItem();
        }

        @Override
        public CountDownCheckmarkItem copy(Item item) {
            return new CountDownCheckmarkItem((CountDownCheckmarkItem) item);
        }

        @Override
        public Transform.Result transform(Item from) {
            if (from instanceof DayRepeatableCheckboxItem checkboxItem) {
                return Transform.Result.allow(() -> new CountDownCheckmarkItem(checkboxItem, checkboxItem.isChecked(), TimeUtil.MILLISECONDS_IN_DAY, 1, true));

            } else if (from instanceof CheckboxItem checkboxItem) {
                return Transform.Result.allow(() -> new CountDownCheckmarkItem(checkboxItem));


            } else if (from instanceof TextItem textItem) {
                return Transform.Result.allow(() -> new CountDownCheckmarkItem(textItem));
            }
            return Transform.Result.NOT_ALLOW;
        }
    }
}
