package com.fazziclay.opentoday.app.items.item;

import android.content.Context;
import android.view.Gravity;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.app.items.ItemsUtils;
import com.fazziclay.opentoday.app.items.tick.TickSession;
import com.fazziclay.opentoday.app.items.tick.TickTarget;
import com.fazziclay.opentoday.util.RandomUtil;

public class MathGameItem extends TextItem {
    public static MathGameItemCodec CODEC = new MathGameItemCodec();

    private static class MathGameItemCodec extends TextItemCodec {
        @NonNull
        @Override
        public Cherry exportItem(@NonNull Item item) {
            return super.exportItem(item);
        }

        @NonNull
        @Override
        public Item importItem(@NonNull Cherry cherry, Item item) {
            MathGameItem mathGameItem = item != null ? (MathGameItem) item : new MathGameItem();
            super.importItem(cherry, mathGameItem);
            return mathGameItem;
        }
    }

    private BaseQuest quest = new BaseQuest();

    @NonNull
    public static MathGameItem createEmpty() {
        return new MathGameItem();
    }

    public MathGameItem() {
        this(null);
    }

    public MathGameItem(MathGameItem copy) {

    }

    @Override
    public void tick(TickSession tickSession) {
        if (!tickSession.isAllowed(this)) return;

        super.tick(tickSession);

        if (tickSession.isTickTargetAllowed(TickTarget.ITEM_MATH_GAME_UPDATE)) {
            if (!quest.isInitialize()) {
                generateQuest(tickSession.getContext());
                visibleChanged();
            }
        }
    }

    public float getQuestTextSize() {
        return quest.getTextSize();
    }

    public int getQuestTextGravity() {
        return quest.getGravity();
    }

    public void generateQuest(Context context) {
        quest = new PrimitiveQuest(context);
    }


    public String getQuestText() {
        return quest.getText();
    }

    public boolean isResultRight(int currentNumber) {
        return currentNumber == quest.getResult();
    }

    public int getResult() {
        return quest.getResult();
    }

    public void postResult(Context context, int currentNumber) {
        generateQuest(context);
    }

    private static class BaseQuest {
        public String getText() {
            return "";
        }

        public int getResult() {
            return 0;
        }

        public int getGravity() {
            return Gravity.CENTER;
        }

        public int getTextSize() {
            return 20;
        }

        public boolean isInitialize() {
            return false;
        }
    }

    private static class PrimitiveQuest extends BaseQuest {
        private final Operation operation;
        private final int val1;
        private final int val2;
        private final int result;
        private final String text;

        public PrimitiveQuest(Context context) {
            this.operation = Operation.random();
            val1 = RandomUtil.nextInt(100);
            val2 = RandomUtil.nextInt(100);
            result = operation.apply(val1, val2);
            text = ItemsUtils.getTranslatedText(context, ItemsUtils.TRANSLATE_MATHGAME_PRIMITIVE_OPERATION, val1, operation, val2);
        }

        @Override
        public String getText() {
            return text;
        }

        @Override
        public int getResult() {
            return result;
        }

        @Override
        public int getGravity() {
            return Gravity.CENTER;
        }

        @Override
        public int getTextSize() {
            return 20;
        }

        @Override
        public boolean isInitialize() {
            return true;
        }
    }

    enum Operation {
        PLUS("+"),
        SUBTRACT("-"),
        MULTIPLY("*"),
        DIVIDE("/");

        private final String s;
        Operation(String s) {
            this.s = s;
        }

        public int apply(int i1, int i2) {
            return switch (this) {
                case PLUS -> i1 + i2;
                case SUBTRACT -> i1 - i2;
                case MULTIPLY -> i1 * i2;
                case DIVIDE -> i1 / i2;
            };
        }

        public String str() {
            return s;
        }

        @NonNull
        @Override
        public String toString() {
            return s;
        }

        public static Operation random() {
            return values()[RandomUtil.nextInt(values().length)];
        }
    }
}
