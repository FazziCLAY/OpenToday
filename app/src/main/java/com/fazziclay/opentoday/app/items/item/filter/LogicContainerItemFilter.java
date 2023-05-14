package com.fazziclay.opentoday.app.items.item.filter;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.util.Logger;

import java.util.ArrayList;
import java.util.List;

// 2023.05.12 quality control passed.
public class LogicContainerItemFilter extends ItemFilter {
    private static final String TAG = LogicContainerItemFilter.class.getSimpleName();
    /**
     * Logic of data storage
     * <pre>
     * <code>
     *     {
     *         "reverse": true, // true / false
     *         "logicMode": "OR", // "OR" / "AND"
     *         "description": "This is user-editable description",
     *         "filters": [
     *              // It contains a ItemFilter export Cherry objects. Like a this object (recurse), DataItemFilter and etc...
     *         ]
     *     }
     * </code>
     * </pre>
     */
    public static final FilterCodec CODEC = new LogicContainerItemFilterCodec();
    private static class LogicContainerItemFilterCodec extends FilterCodec {
        private static final String KEY_FILTERS = "filters";
        private static final String KEY_DESCRIPTION = "description";
        private static final String KEY_REVERSE = "reverse";
        private static final String KEY_LOGIC_MODE = "logicMode";

        @NonNull
        @Override
        public Cherry exportFilter(@NonNull ItemFilter filter) {
            final LogicContainerItemFilter l = (LogicContainerItemFilter) filter;
            return new Cherry()
                    .put(KEY_FILTERS, FilterCodecUtil.exportFiltersList(l.filters))
                    .put(KEY_DESCRIPTION, l.description)
                    .put(KEY_REVERSE, l.reverse)
                    .put(KEY_LOGIC_MODE, l.logicMode);
        }

        @NonNull
        @Override
        public ItemFilter importFilter(@NonNull Cherry cherry, ItemFilter d) {
            final LogicContainerItemFilter l = new LogicContainerItemFilter();
            l.filters.addAll(FilterCodecUtil.importFiltersList(cherry.optOrchard(KEY_FILTERS)));
            l.description = cherry.optString(KEY_DESCRIPTION, l.description);
            l.reverse = cherry.optBoolean(KEY_REVERSE, l.reverse);
            l.logicMode = cherry.optEnum(KEY_LOGIC_MODE, l.logicMode);
            return l;
        }
    }

    private final List<ItemFilter> filters = new ArrayList<>();
    private LogicMode logicMode = LogicMode.AND;
    private boolean reverse = false;
    private String description = "";

    public LogicContainerItemFilter() {
    }

    // Copy constructor
    public LogicContainerItemFilter(LogicContainerItemFilter copy) {
        this.reverse = copy.reverse;
        this.logicMode = copy.logicMode;
        for (ItemFilter filter : copy.filters) {
            this.filters.add(filter.copy());
        }
        this.description = copy.description;
    }

    private boolean _isFitInternalNoReverse(final FitEquip fitEquip) {
        if (filters.isEmpty()) return false; // ALWAYS return FALSE for empty logic containers
        if (logicMode == LogicMode.AND) {
            for (ItemFilter filter : filters) {
                if (!filter.isFit(fitEquip)) {
                    return false;
                }
            }

            return true;
        } else if (logicMode == LogicMode.OR) {
            for (ItemFilter filter : filters) {
                if (filter.isFit(fitEquip)) {
                    return true;
                }
            }

            return false;
        }

        // Crash if logic mode not supported
        final RuntimeException exception = new RuntimeException("Unknown logicMode in TAG:" + TAG + " currently="+logicMode+" Setting mode to default(first in enum) and crash(this exception)");
        Logger.e(TAG, "Unknown logic mode! Setting to a default. And crash.", exception);
        logicMode = LogicMode.values()[0];
        throw exception;
    }

    @Override
    public boolean isFit(final FitEquip fitEquip) {
        return reverse != _isFitInternalNoReverse(fitEquip); // apply reverse
    }

    public int getFilterPosition(ItemFilter filter) {
        return filters.indexOf(filter);
    }

    public int add(ItemFilter itemFilter) {
        filters.add(itemFilter);
        return getFilterPosition(itemFilter);
    }

    public int remove(ItemFilter filter) {
        final int i = getFilterPosition(filter);
        filters.remove(filter);
        return i;
    }

    public ItemFilter[] getFilters() {
        return filters.toArray(new ItemFilter[0]);
    }

    public void setLogicMode(LogicMode logicMode) {
        if (logicMode == null) throw new NullPointerException("loginMode can't be null!");
        this.logicMode = logicMode;
    }

    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    public LogicMode getLogicMode() {
        return logicMode;
    }

    public boolean isReverse() {
        return reverse;
    }

    @Override
    public ItemFilter copy() {
        return new LogicContainerItemFilter(this);
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String s) {
        if (s == null) throw new NullPointerException("description can't be null!");
        this.description = s;
    }

    public enum LogicMode {
        AND,
        OR
    }
}
