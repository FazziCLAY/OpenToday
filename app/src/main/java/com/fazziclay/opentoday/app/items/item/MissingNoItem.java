package com.fazziclay.opentoday.app.items.item;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fazziclay.opentoday.app.data.Cherry;

import java.util.ArrayList;
import java.util.List;

// un-imported item :(
public class MissingNoItem extends Item {
    public static final MissingNoItemCodec CODEC = new MissingNoItemCodec();
    private static class MissingNoItemCodec extends AbstractItemCodec {

        @NonNull
        @Override
        public Cherry exportItem(@NonNull Item item) {
            MissingNoItem missingNoItem = (MissingNoItem) item;
            return missingNoItem.cherry;
        }

        @NonNull
        @Override
        public Item importItem(@NonNull Cherry cherry, @Nullable Item item) {
            return new MissingNoItem(cherry);
        }
    }

    private final Cherry cherry;
    private final List<Exception> exceptionList = new ArrayList<>();

    public MissingNoItem(Cherry cherry) {
        this.cherry = cherry;
    }

    @Override
    public ItemType getItemType() {
        return ItemType.MISSING_NO;
    }

    public MissingNoItem putException(Exception e) {
        exceptionList.add(e);
        return this;
    }

    public Exception[] getExceptionList() {
        return exceptionList.toArray(new Exception[0]);
    }
}
