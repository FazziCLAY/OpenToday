package com.fazziclay.opentoday.fun.mathgame;

import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.util.RandomUtil;

class PrimitiveQuest extends BaseQuest {
    private final MathGameItem parent;
    private final Settings settings = Settings.createDefault();
    private Operation operation;
    private int val1;
    private int val2;
    private int result;

    public PrimitiveQuest(MathGameItem parent) {
        this.parent = parent;
        regenerate();
    }

    public static PrimitiveQuest importQuest(Cherry primitiveQuest) {
        return null;
    }

    @Override
    public Cherry export() {
        return super.export();
    }

    @Override
    public String getText() {
        return operation.forNumbers(val1, val2);
    }

    @Override
    public int getResult() {
        return result;
    }

    @Override
    public void regenerate() {
        operation = Operation.random();
        val1 = RandomUtil.bounds(settings.first_min, settings.first_max);
        val2 = RandomUtil.bounds(settings.second_min, settings.second_max);
        result = operation.apply(val1, val2);
    }

    public void tick() {

    }

    public static class Settings {
        public int first_min;
        public int first_max;
        public int second_min;
        public int second_max;

        public Settings(int first_min, int first_max, int second_min, int second_max) {
            this.first_min = first_min;
            this.first_max = first_max;
            this.second_min = second_min;
            this.second_max = second_max;
        }

        public static Settings createDefault() {
            return new Settings(0, 10, -5, 10);
        }

        public Cherry exportSettings() {
            return new Cherry()
                    .put("first_min", first_min)
                    .put("first_max", first_max)
                    .put("second_min", second_min)
                    .put("second_max", second_max);
        }

        public static Settings importSettings(Cherry cherry) {
            return new Settings(
                    cherry.optInt("first_min", 0),
                    cherry.optInt("first_max", 10),
                    cherry.optInt("second_min", -5),
                    cherry.optInt("second_max", 10)
            );
        }


    }
}
