package com.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;

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
                    .put("requiredSleepTime", sleepTimeItem.requiredSleepTime);
        }

        private final SleepTimeItem defaultValues = new SleepTimeItem();
        @NonNull
        @Override
        public Item importItem(@NonNull Cherry cherry, Item item) {
            SleepTimeItem sleepTimeItem = item != null ? (SleepTimeItem) item : new SleepTimeItem();
            super.importItem(cherry, sleepTimeItem);
            sleepTimeItem.wakeUpTime = cherry.optInt("wakeUpTime", defaultValues.wakeUpTime);
            sleepTimeItem.requiredSleepTime = cherry.optInt("requiredSleepTime", defaultValues.requiredSleepTime);
            return sleepTimeItem;
        }
    }

    private int wakeUpTime;
    private int requiredSleepTime;

    private int elapsedTime; // cached
    private int wakeUpForRequiredAtCurr; // cached
    private long tick; // cached

    @NonNull
    public static SleepTimeItem createEmpty() {
        return new SleepTimeItem();
    }



    public SleepTimeItem(SleepTimeItem copy) {
        this.wakeUpTime = copy.wakeUpTime;
        this.requiredSleepTime = copy.requiredSleepTime;
        tick = 0;
        elapsedTime = 0;
        wakeUpForRequiredAtCurr = 0;
    }

    private SleepTimeItem() {

    }

    @Override
    public void tick(TickSession tickSession) {
        super.tick(tickSession);
        elapsedTime = wakeUpTime - TimeUtil.getDaySeconds();
        if (elapsedTime <= 0) {
            elapsedTime = TimeUtil.SECONDS_IN_DAY + elapsedTime;
        }

        wakeUpForRequiredAtCurr = TimeUtil.getDaySeconds() + requiredSleepTime;
        if (wakeUpForRequiredAtCurr > TimeUtil.SECONDS_IN_DAY) {
            wakeUpForRequiredAtCurr-=TimeUtil.SECONDS_IN_DAY;
        }

        if (tick % 5 == 0) {
            visibleChanged();
        }
        tick++;
    }

    public int getElapsedTime() {
        return elapsedTime;
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
        // TODO: 20.09.2023 make translatable
        return "$(elapsed) осталось до $(wakeUpTime)\nТекущее + $(requiredSleepTime) = $(wakeUpForRequired)";
    }

}
