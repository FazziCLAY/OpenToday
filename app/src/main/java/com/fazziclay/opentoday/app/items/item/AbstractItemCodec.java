package com.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fazziclay.opentoday.app.data.Cherry;

import java.util.function.Supplier;

public abstract class AbstractItemCodec {
    @NonNull public abstract Cherry exportItem(@NonNull Item item);
    @NonNull public abstract Item importItem(@NonNull Cherry cherry, @Nullable Item item);

    public <T extends Item> T fallback(Item item, Supplier<T> fallback) {
        return item != null ? (T) item : fallback.get();
    }
}
