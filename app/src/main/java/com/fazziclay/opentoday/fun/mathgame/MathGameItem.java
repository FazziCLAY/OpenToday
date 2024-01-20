package com.fazziclay.opentoday.fun.mathgame;

import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.ItemFactory;
import com.fazziclay.opentoday.app.items.item.TextItem;
import com.fazziclay.opentoday.app.items.item.Transform;
import com.fazziclay.opentoday.app.items.tick.TickSession;
import com.fazziclay.opentoday.app.items.tick.TickTarget;

public class MathGameItem extends TextItem {
    public static final MathGameItemCodec CODEC = new MathGameItemCodec();
    public static final ItemFactory<MathGameItem> FACTORY = new MathGameItemFactory();



    public MathGameItem() {
        super();
    }

    public MathGameItem(@Nullable MathGameItem copy) {
        super(copy);
        if (copy != null) {

        }
    }

    public MathGameItem(@Nullable TextItem append) {
        super(append);
    }

    @Override
    public void tick(TickSession tickSession) {
        if (!tickSession.isAllowed(this)) return;

        super.tick(tickSession);

        if (tickSession.isTickTargetAllowed(TickTarget.ITEM_MATH_GAME_UPDATE)) {
            profilerPush(tickSession, "math_game_update");
            profilerPop(tickSession);
        }
    }


    public boolean isOperationEnabled(Operation o) {
        return true;
    }

    public void setOperationEnabled(Operation o, boolean b) {

    }

    public float getQuestTextSize() {
        return 20;
    }

    public int getQuestTextGravity() {
        return Gravity.CENTER;
    }

    public void generateQuest() {
    }


    public String getQuestText() {
        return "No impl :(";
    }

    public boolean isResultRight(int currentNumber) {
        return true;
    }

    public int getResult() {
        return 0;
    }

    public void postResult(int currentNumber) {
    }

    public int getPrimitiveNumber1Min() {
        return 0;
    }

    public void setPrimitiveNumber1Min(int primitiveNumber1Min) {
    }

    public int getPrimitiveNumber1Max() {
        return 1;
    }

    public void setPrimitiveNumber1Max(int primitiveNumber1Max) {
    }

    public int getPrimitiveNumber2Min() {
        return 2;
    }

    public void setPrimitiveNumber2Min(int primitiveNumber2Min) {
    }

    public int getPrimitiveNumber2Max() {
        return 3;
    }

    public void setPrimitiveNumber2Max(int primitiveNumber2Max) {
    }


    private static class MathGameItemCodec extends TextItemCodec {
        @NonNull
        @Override
        public Cherry exportItem(@NonNull Item item) {
            MathGameItem mathGameItem = (MathGameItem) item;
            return super.exportItem(item);
        }

        private static final MathGameItem defaultValues = new MathGameItem();
        @NonNull
        @Override
        public Item importItem(@NonNull Cherry cherry, Item item) {
            MathGameItem mathGameItem = fallback(item, MathGameItem::new);
            super.importItem(cherry, mathGameItem);

            return mathGameItem;
        }
    }

    private static class MathGameItemFactory implements ItemFactory<MathGameItem> {
        @Override
        public MathGameItem create() {
            return new MathGameItem();
        }

        @Override
        public MathGameItem copy(Item item) {
            return new MathGameItem((MathGameItem) item);
        }

        @Override
        public Transform.Result transform(Item from) {
            if (from instanceof TextItem textItem) {
                return Transform.Result.allow(() -> new MathGameItem(textItem));
            }
            return Transform.Result.NOT_ALLOW;
        }
    }
}
