package com.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.Translation;
import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.app.items.tick.TickSession;
import com.fazziclay.opentoday.util.time.TimeUtil;

public class SleepTimeItem extends TextItem {
    public final static SleepTimeItemCodec CODEC = new SleepTimeItemCodec();
    public static class SleepTimeItemCodec extends TextItem.TextItemCodec {
        @NonNull
        @Override
        public Cherry exportItem(@NonNull Item item) {
            SleepTimeItem sleepTimeItem = (SleepTimeItem) item;
            return super.exportItem(sleepTimeItem)
                    .put("wakeUpTime", sleepTimeItem.wakeUpTime)
                    .put("requiredSleepTime", sleepTimeItem.requiredSleepTime)
                    .put("sleepTextPattern", sleepTimeItem.sleepTextPattern);
        }

        private final SleepTimeItem defaultValues = new SleepTimeItem();
        @NonNull
        @Override
        public Item importItem(@NonNull Cherry cherry, Item item) {
            SleepTimeItem sleepTimeItem = item != null ? (SleepTimeItem) item : new SleepTimeItem();
            super.importItem(cherry, sleepTimeItem);
            sleepTimeItem.wakeUpTime = cherry.optInt("wakeUpTime", defaultValues.wakeUpTime);
            sleepTimeItem.requiredSleepTime = cherry.optInt("requiredSleepTime", defaultValues.requiredSleepTime);
            sleepTimeItem.sleepTextPattern = cherry.optString("sleepTextPattern", defaultValues.sleepTextPattern);
            return sleepTimeItem;
        }
    }

    private int wakeUpTime;
    private int requiredSleepTime;
    private String sleepTextPattern = null;

    private int elapsedTime; // cached
    private int wakeUpForRequiredAtCurr; // cached
    private int elapsedToStartSleep; // cached
    private long tick; // cached

    @NonNull
    public static SleepTimeItem createEmpty() {
        return new SleepTimeItem();
    }


    public SleepTimeItem(TextItem append) {
        super(append);
        tick = 0;
        elapsedTime = 0;
        wakeUpForRequiredAtCurr = 0;
    }

    public SleepTimeItem(SleepTimeItem copy) {
        super(copy);
        tick = 0;
        elapsedTime = 0;
        wakeUpForRequiredAtCurr = 0;
        elapsedToStartSleep = 0;

        if (copy != null) {
            this.wakeUpTime = copy.wakeUpTime;
            this.requiredSleepTime = copy.requiredSleepTime;
            this.sleepTextPattern = copy.sleepTextPattern;
        }
    }

    private SleepTimeItem() {
        this(null);
    }

    @Override
    public ItemType getItemType() {
        return ItemType.SLEEP_TIME;
    }

    @Override
    protected void regenerateId() {
        super.regenerateId();
        if (sleepTextPattern == null) {
            sleepTextPattern = App.get().getTranslation().get(Translation.KEY_SLEEP_TIME_ITEM_PATTERN); // TODO: 08.10.2023 uses static App.get() is bad...
            save();
        }
    }

    @Override
    public void tick(TickSession tickSession) {
        super.tick(tickSession);
        profPush(tickSession, "sleep_time_update");
        elapsedTime = wakeUpTime - TimeUtil.getDaySeconds();
        if (elapsedTime <= 0) {
            elapsedTime = TimeUtil.SECONDS_IN_DAY + elapsedTime;
        }

        wakeUpForRequiredAtCurr = TimeUtil.getDaySeconds() + requiredSleepTime;
        if (wakeUpForRequiredAtCurr > TimeUtil.SECONDS_IN_DAY) {
            wakeUpForRequiredAtCurr-=TimeUtil.SECONDS_IN_DAY;
        }

        elapsedToStartSleep = wakeUpTime - requiredSleepTime - TimeUtil.getDaySeconds();
        if (elapsedToStartSleep < 0) {
            elapsedToStartSleep += TimeUtil.SECONDS_IN_DAY;
        }
        if (elapsedToStartSleep < 0) {
            elapsedToStartSleep += TimeUtil.SECONDS_IN_DAY;
        }

        if (tick % 5 == 0) {
            visibleChanged();
        }
        tick++;
        profPop(tickSession);
    }

    public int getElapsedTime() {
        return elapsedTime;
    }


    public int getElapsedTimeToStartSleep() {
        return elapsedToStartSleep;
    }


    public void setWakeUpTime(int wakeUpTime) {
        this.wakeUpTime = wakeUpTime;
    }

    public int getWakeUpTime() {
        return wakeUpTime;
    }

    public int getWakeUpForRequiredAtCurr() {
        return wakeUpForRequiredAtCurr;
    }

    public int getRequiredSleepTime() {
        return requiredSleepTime;
    }

    public void setRequiredSleepTime(int requiredSleepTime) {
        this.requiredSleepTime = requiredSleepTime;
    }


    public String getSleepTextPattern() {
        if (sleepTextPattern == null) return "";
        return sleepTextPattern;
    }

    public void setSleepTextPattern(String sleepTextPattern) {
        this.sleepTextPattern = sleepTextPattern;
    }
}
