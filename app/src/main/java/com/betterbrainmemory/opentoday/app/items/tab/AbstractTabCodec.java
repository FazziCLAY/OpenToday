package com.betterbrainmemory.opentoday.app.items.tab;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.betterbrainmemory.opentoday.app.data.Cherry;

public abstract class AbstractTabCodec {
    @NonNull public abstract Cherry exportTab(@NonNull Tab tab);
    @NonNull public abstract Tab importTab(@NonNull Cherry cherry, @Nullable Tab tab);
}
