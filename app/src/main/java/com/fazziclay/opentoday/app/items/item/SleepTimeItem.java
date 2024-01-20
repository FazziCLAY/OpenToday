package com.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fazziclay.opentoday.app.Translation;
import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.app.items.ItemsRoot;
import com.fazziclay.opentoday.app.items.tick.TickSession;
import com.fazziclay.opentoday.util.time.TimeUtil;

public class SleepTimeItem extends TextItem {
    public static final SleepTimeItemCodec CODEC = new SleepTimeItemCodec();
    public static final ItemFactory<SleepTimeItem> FACTORY = new SleepTimeItemFactory();

    private int wakeup;
    private int duration;
    private String sleepTextPattern = null; // uninitialized by default for initialize on first attach (translatable)

    private int elapsedToWakeupTime; // cached
    private int wakeupAtCurrent; // cached
    private int beforeFallingAsleep; // cached
    private long tick; // cached


    public SleepTimeItem(@Nullable TextItem append) {
        super(append);

        tick = 0;
        elapsedToWakeupTime = 0;
        wakeupAtCurrent = 0;
        beforeFallingAsleep = 0;
    }

    public SleepTimeItem(@Nullable SleepTimeItem copy) {
        this((TextItem) copy);

        if (copy != null) {
            this.wakeup = copy.wakeup;
            this.duration = copy.duration;
            this.sleepTextPattern = copy.sleepTextPattern;
        }
    }

    public SleepTimeItem() {
        super();
    }

    @Override
    protected void onAttached(@NonNull ItemsRoot itemsRoot) {
        super.onAttached(itemsRoot);
        if (sleepTextPattern == null) {
            sleepTextPattern = itemsRoot.getTranslation().get(Translation.KEY_SLEEP_TIME_ITEM_PATTERN);
            save();
        }
    }

    @Override
    public void tick(TickSession tickSession) {
        super.tick(tickSession);
        profilerPush(tickSession, "sleep_time_update");
        elapsedToWakeupTime = wakeup - TimeUtil.getDaySeconds();
        if (elapsedToWakeupTime <= 0) {
            elapsedToWakeupTime = TimeUtil.SECONDS_IN_DAY + elapsedToWakeupTime;
        }

        wakeupAtCurrent = TimeUtil.getDaySeconds() + duration;
        if (wakeupAtCurrent > TimeUtil.SECONDS_IN_DAY) {
            wakeupAtCurrent -=TimeUtil.SECONDS_IN_DAY;
        }

        beforeFallingAsleep = wakeup - duration - TimeUtil.getDaySeconds();
        if (beforeFallingAsleep < 0) {
            beforeFallingAsleep += TimeUtil.SECONDS_IN_DAY;
        }
        if (beforeFallingAsleep < 0) {
            beforeFallingAsleep += TimeUtil.SECONDS_IN_DAY;
        }

        if (tick % 5 == 0) {
            visibleChanged();
        }
        tick++;
        profilerPop(tickSession);
    }

    public int getElapsedToWakeupTime() {
        return elapsedToWakeupTime;
    }


    public int getElapsedTimeToStartSleep() {
        return beforeFallingAsleep;
    }


    public void setWakeup(int wakeup) {
        this.wakeup = wakeup;
    }

    public int getWakeup() {
        return wakeup;
    }

    public int getWakeupAtCurrent() {
        return wakeupAtCurrent;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }


    public String getSleepTextPattern() {
        if (sleepTextPattern == null) return "";
        return sleepTextPattern;
    }

    public void setSleepTextPattern(String sleepTextPattern) {
        this.sleepTextPattern = sleepTextPattern;
    }



    // Import - Export - Factory
    public static class SleepTimeItemCodec extends TextItem.TextItemCodec {
        private final static String KEY_WAKEUP = "sleep_wakeup";
        private final static String KEY_DURATION = "sleep_duration";
        private final static String KEY_TEXT_PATTERN = "sleep_duration";

        @NonNull
        @Override
        public Cherry exportItem(@NonNull Item item) {
            SleepTimeItem sleepTimeItem = (SleepTimeItem) item;
            return super.exportItem(sleepTimeItem)
                    .put(KEY_WAKEUP, sleepTimeItem.wakeup)
                    .put(KEY_DURATION, sleepTimeItem.duration)
                    .put(KEY_TEXT_PATTERN, sleepTimeItem.sleepTextPattern);
        }

        private final SleepTimeItem defaultValues = new SleepTimeItem();
        @NonNull
        @Override
        public Item importItem(@NonNull Cherry cherry, Item item) {
            var sleepTimeItem = fallback(item, SleepTimeItem::new);
            super.importItem(cherry, sleepTimeItem);

            sleepTimeItem.wakeup = cherry.optInt(KEY_WAKEUP, defaultValues.wakeup);
            sleepTimeItem.duration = cherry.optInt(KEY_DURATION, defaultValues.duration);
            sleepTimeItem.sleepTextPattern = cherry.optString(KEY_TEXT_PATTERN, defaultValues.sleepTextPattern);
            return sleepTimeItem;
        }
    }

    private static class SleepTimeItemFactory implements ItemFactory<SleepTimeItem> {
        @Override
        public SleepTimeItem create() {
            return new SleepTimeItem();
        }

        @Override
        public SleepTimeItem copy(Item item) {
            return new SleepTimeItem((SleepTimeItem) item);
        }

        @Override
        public Transform.Result transform(Item from) {
            if (from instanceof TextItem textItem) {
                return Transform.Result.allow(() -> new SleepTimeItem(textItem));
            }
            return Transform.Result.NOT_ALLOW;
        }
    }
}
