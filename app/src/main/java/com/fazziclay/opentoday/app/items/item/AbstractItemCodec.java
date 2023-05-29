package com.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fazziclay.opentoday.app.data.Cherry;

public abstract class AbstractItemCodec {
    @NonNull public abstract Cherry exportItem(@NonNull Item item);
    @NonNull public abstract Item importItem(@NonNull Cherry cherry, @Nullable Item item);
}
