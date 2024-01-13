package com.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.app.items.tick.TickSession;
import com.fazziclay.opentoday.util.time.TimeUtil;

import java.util.Objects;

public class CountDownCheckmarkItem extends CheckboxItem {
    // START - Save
    public final static CountDownCheckmarkItem.CountDownCheckmarkItemCodec CODEC = new CountDownCheckmarkItem.CountDownCheckmarkItemCodec();
    public static class CountDownCheckmarkItemCodec extends CheckboxItem.CheckboxItemCodec {
        @NonNull
        @Override
        public Cherry exportItem(@NonNull Item item) {
            CountDownCheckmarkItem countDownCheckmarkItem = (CountDownCheckmarkItem) item;
            return super.exportItem(countDownCheckmarkItem)
                    .put("checkedTime", countDownCheckmarkItem.checkedTime)
                    .put("step", countDownCheckmarkItem.step)
                    .put("availableSteps", countDownCheckmarkItem.availableSteps)
                    .put("roundToDays", countDownCheckmarkItem.roundToDays);
        }

        private final CountDownCheckmarkItem defaultValues = new CountDownCheckmarkItem();
        @NonNull
        @Override
        public Item importItem(@NonNull Cherry cherry, Item item) {
            CountDownCheckmarkItem countDownCheckmarkItem = item != null ? (CountDownCheckmarkItem) item : new CountDownCheckmarkItem();
            super.importItem(cherry, countDownCheckmarkItem);

            countDownCheckmarkItem.checkedTime = cherry.optLong("checkedTime", defaultValues.checkedTime);
            countDownCheckmarkItem.step = cherry.optLong("step", defaultValues.step);
            countDownCheckmarkItem.availableSteps = cherry.optInt("availableSteps", defaultValues.availableSteps);
            countDownCheckmarkItem.roundToDays = cherry.optBoolean("roundToDays", defaultValues.roundToDays);

            return countDownCheckmarkItem;
        }
    }
    // END - Save

    private long checkedTime;
    private long step = TimeUtil.SECONDS_IN_DAY * 1000;
    private int availableSteps = 2;
    private boolean roundToDays = false;

    private String displayCache;

    @NonNull
    public static CountDownCheckmarkItem createEmpty() {
        return new CountDownCheckmarkItem();
    }

    public CountDownCheckmarkItem() {
        this(null);
    }


    public CountDownCheckmarkItem(CountDownCheckmarkItem copy) {
        if (copy != null) {
            this.checkedTime = copy.checkedTime;
            this.step = copy.step;
            this.availableSteps = copy.availableSteps;
            this.roundToDays = copy.roundToDays;
        }
        this.displayCache = "?";
    }

    @Override
    public void tick(TickSession tickSession) {
        if (isChecked()) {
            long current = tickSession.getGregorianCalendar().getTimeInMillis();
            long diffWithCheck;
            if (roundToDays) {
                diffWithCheck = TimeUtil.noTimeUnixTimestamp(current) - TimeUtil.noTimeUnixTimestamp(checkedTime);
            } else {
                diffWithCheck = current - checkedTime;
            }
            long stepsPassed = diffWithCheck / step;

            int countdown = availableSteps - Math.toIntExact(stepsPassed);
            if (countdown <= 0) {
                setChecked(false);
                tickSession.saveNeeded();
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
            checkedTime = TickSession.getLatestGregorianCalendar().getTimeInMillis();
            displayCache = String.valueOf(availableSteps);
        } else {
            checkedTime = 0;
        }
        // before sets checkedTime because this may call instant tick for this checkboxItem
        super.setChecked(s);
    }

    public long getStep() {
        return step;
    }

    public void setStep(long step) {
        this.step = step;
    }

    public int getAvailableSteps() {
        return availableSteps;
    }

    public void setAvailableSteps(int availableSteps) {
        this.availableSteps = availableSteps;
    }

    public boolean isRoundToDays() {
        return roundToDays;
    }

    public void setRoundToDays(boolean roundToDays) {
        this.roundToDays = roundToDays;
    }
}
