package com.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.util.Checks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// un-imported item :(
public class MissingNoItem extends Item {
    public static final MissingNoItemCodec CODEC = new MissingNoItemCodec();

    private final Cherry cherry;
    private final List<Exception> exceptionList = new ArrayList<>();

    public MissingNoItem(Cherry cherry) {
        this.cherry = cherry;
    }

    public MissingNoItem putException(Exception... e) {
        exceptionList.addAll(Arrays.asList(e));
        return this;
    }

    public Exception[] getExceptionList() {
        return exceptionList.toArray(new Exception[0]);
    }

    public static class MissingNoItemCodec extends AbstractItemCodec {
        @NonNull
        @Override
        public Cherry exportItem(@NonNull Item item) {
            MissingNoItem missingNoItem = (MissingNoItem) item;
            return missingNoItem.cherry;
        }

        @NonNull
        @Override
        public Item importItem(@NonNull Cherry cherry, @Nullable Item item) {
            Checks.throwIsNotNull(item, "missing_no item not support inherits!");
            return new MissingNoItem(cherry);
        }
    }
}
