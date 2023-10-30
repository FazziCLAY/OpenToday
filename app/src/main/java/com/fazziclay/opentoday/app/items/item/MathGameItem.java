package com.fazziclay.opentoday.app.items.item;

import android.view.Gravity;

import androidx.annotation.NonNull;

import com.fazziclay.opentoday.app.Translation;
import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.app.data.CherryOrchard;
import com.fazziclay.opentoday.app.items.tick.TickSession;
import com.fazziclay.opentoday.app.items.tick.TickTarget;
import com.fazziclay.opentoday.util.RandomUtil;
import com.fazziclay.opentoday.util.annotation.RequireSave;
import com.fazziclay.opentoday.util.annotation.SaveKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MathGameItem extends TextItem {
    public static final MathGameItemCodec CODEC = new MathGameItemCodec();

    private static class MathGameItemCodec extends TextItemCodec {
        @NonNull
        @Override
        public Cherry exportItem(@NonNull Item item) {
            MathGameItem mathGameItem = (MathGameItem) item;
            return super.exportItem(item)
                    .put("primitiveNumber1Min", mathGameItem.primitiveNumber1Min)
                    .put("primitiveNumber1Max", mathGameItem.primitiveNumber1Max)
                    .put("primitiveNumber2Min", mathGameItem.primitiveNumber2Min)
                    .put("primitiveNumber2Max", mathGameItem.primitiveNumber2Max)
                    .put("primitiveAllowedOperations", CherryOrchard.of(mathGameItem.primitiveAllowedOperations.toArray(new String[0])));
        }

        private static final MathGameItem defaultValues = createEmpty();
        @NonNull
        @Override
        public Item importItem(@NonNull Cherry cherry, Item item) {
            MathGameItem mathGameItem = item != null ? (MathGameItem) item : new MathGameItem();
            super.importItem(cherry, mathGameItem);

            mathGameItem.primitiveNumber1Min = cherry.optInt("primitiveNumber1Min", defaultValues.primitiveNumber1Min);
            mathGameItem.primitiveNumber1Max = cherry.optInt("primitiveNumber1Max", defaultValues.primitiveNumber1Max);
            mathGameItem.primitiveNumber2Min = cherry.optInt("primitiveNumber2Min", defaultValues.primitiveNumber2Min);
            mathGameItem.primitiveNumber2Max = cherry.optInt("primitiveNumber2Max", defaultValues.primitiveNumber2Max);
            mathGameItem.primitiveAllowedOperations.clear();
            mathGameItem.primitiveAllowedOperations.addAll(Arrays.asList(CherryOrchard.parseStringArray(cherry.getOrchard("primitiveAllowedOperations"), new String[]{"+"})));

            return mathGameItem;
        }
    }

    @RequireSave @SaveKey(key = "primitiveNumber1Min") private int primitiveNumber1Min = 0;
    @RequireSave @SaveKey(key = "primitiveNumber1Max") private int primitiveNumber1Max = 100;
    @RequireSave @SaveKey(key = "primitiveNumber2Min") private int primitiveNumber2Min = 2;
    @RequireSave @SaveKey(key = "primitiveNumber2Max") private int primitiveNumber2Max = 20;
    @RequireSave @SaveKey(key = "primitiveAllowedOperations") private final List<String> primitiveAllowedOperations = new ArrayList<>();


    private BaseQuest quest;

    @NonNull
    public static MathGameItem createEmpty() {
        return new MathGameItem();
    }

    public MathGameItem() {
        this(null);
    }

    public MathGameItem(MathGameItem copy) {
        super(copy);
        this.quest = new BaseQuest();
        if (copy != null) {
            this.primitiveNumber1Min = copy.primitiveNumber1Min;
            this.primitiveNumber1Max = copy.primitiveNumber1Max;
            this.primitiveNumber2Min = copy.primitiveNumber2Min;
            this.primitiveNumber2Max = copy.primitiveNumber2Max;
            this.primitiveAllowedOperations.addAll(copy.primitiveAllowedOperations);
        } else {
            this.primitiveAllowedOperations.add("+");
        }
    }

    public MathGameItem(TextItem append) {
        super(append);
        this.quest = new BaseQuest();
        this.primitiveAllowedOperations.add("+");
    }

    @Override
    public ItemType getItemType() {
        return ItemType.MATH_GAME;
    }

    @Override
    public void tick(TickSession tickSession) {
        if (!tickSession.isAllowed(this)) return;

        super.tick(tickSession);

        if (tickSession.isTickTargetAllowed(TickTarget.ITEM_MATH_GAME_UPDATE)) {
            profPush(tickSession, "math_game_update");
            if (!quest.isInitialize()) {
                generateQuest();
                visibleChanged();
            }
            profPop(tickSession);
        }
    }

    public boolean isOperationEnabled(Operation o) {
        String s = o.s;
        return primitiveAllowedOperations.contains(s);
    }

    public void setOperationEnabled(Operation o, boolean b) {
        String s = o.s;
        if (b && !primitiveAllowedOperations.contains(s)) {
            primitiveAllowedOperations.add(s);

        } else if (!b) {
            primitiveAllowedOperations.remove(s);
        }
    }

    public float getQuestTextSize() {
        return quest.getTextSize();
    }

    public int getQuestTextGravity() {
        return quest.getGravity();
    }

    public void generateQuest() {
        quest = new PrimitiveQuest();
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

    public void postResult(int currentNumber) {
        if (isResultRight(currentNumber)) generateQuest();
    }

    public int getPrimitiveNumber1Min() {
        return primitiveNumber1Min;
    }

    public void setPrimitiveNumber1Min(int primitiveNumber1Min) {
        this.primitiveNumber1Min = primitiveNumber1Min;
    }

    public int getPrimitiveNumber1Max() {
        return primitiveNumber1Max;
    }

    public void setPrimitiveNumber1Max(int primitiveNumber1Max) {
        this.primitiveNumber1Max = primitiveNumber1Max;
    }

    public int getPrimitiveNumber2Min() {
        return primitiveNumber2Min;
    }

    public void setPrimitiveNumber2Min(int primitiveNumber2Min) {
        this.primitiveNumber2Min = primitiveNumber2Min;
    }

    public int getPrimitiveNumber2Max() {
        return primitiveNumber2Max;
    }

    public void setPrimitiveNumber2Max(int primitiveNumber2Max) {
        this.primitiveNumber2Max = primitiveNumber2Max;
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

    // TODO: 14.06.2023 move ui login to ItemViewGenerator.
    private class PrimitiveQuest extends BaseQuest {
        private final Operation operation;
        private final int val1;
        private final int val2;
        private final int result;
        private final String text;

        public PrimitiveQuest() {
            this.operation = Operation.random(Operation.parse(primitiveAllowedOperations));
            val1 = RandomUtil.bounds(primitiveNumber1Min, primitiveNumber1Max);
            val2 = RandomUtil.bounds(primitiveNumber2Min, primitiveNumber2Max);
            result = operation.apply(val1, val2);
            text = MathGameItem.this.getRoot().getTranslation().get(Translation.KEY_MATHGAME_PRIMITIVE_OPERATION, val1, operation, val2);
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

    public enum Operation {
        PLUS("+"),
        SUBTRACT("-"),
        MULTIPLY("*"),
        DIVIDE("/"),
        UNKNOWN("?");

        private final String s;
        Operation(String s) {
            this.s = s;
        }

        public int apply(int i1, int i2) {
            if (i2 == 0 && this == DIVIDE) return 0;
            return switch (this) {
                case PLUS -> i1 + i2;
                case SUBTRACT -> i1 - i2;
                case MULTIPLY -> i1 * i2;
                case DIVIDE -> i1 / i2;
                case UNKNOWN -> i1;
            };
        }

        @NonNull
        @Override
        public String toString() {
            return s;
        }

        public static Operation random(List<Operation> allowed) {
            int MAX_ITER = 1000;

            if (allowed.isEmpty()) return Operation.UNKNOWN;
            int i = 0;
            while (i < MAX_ITER) {
                Operation o = values()[RandomUtil.nextInt(values().length)];
                if (allowed.contains(o)) return o;
                i++;
            }
            throw new RuntimeException("iterations count > MAX_ITER.");
        }

        public static Operation fromString(String s) {
            for (Operation value : values()) {
                if (value.s.equals(s)) return value;
            }
            return null;
        }

        public static List<Operation> parse(List<String> strings) {
            List<Operation> operations = new ArrayList<>();
            for (String string : strings) {
                operations.add(fromString(string));
            }
            return operations;
        }
    }
}
