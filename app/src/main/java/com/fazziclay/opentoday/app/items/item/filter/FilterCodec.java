package com.fazziclay.opentoday.app.items.item.filter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fazziclay.opentoday.app.data.Cherry;

public abstract class FilterCodec {
    @NonNull public abstract Cherry exportFilter(@NonNull ItemFilter filter);
    @NonNull public abstract ItemFilter importFilter(@NonNull Cherry cherry, @Nullable ItemFilter d);
}
