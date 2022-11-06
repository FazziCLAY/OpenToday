package com.fazziclay.opentoday.app.items.item;

public class ExperimentalTransform {
    public static Transform transform(Item item, Class<? extends Item> to) {
        Class<? extends Item> from = item.getClass();


        if (to == DayRepeatableCheckboxItem.class) {
            if (from == CheckboxItem.class) {
                CheckboxItem checkboxItem = (CheckboxItem) item;
                return new Transform(true, new DayRepeatableCheckboxItem(checkboxItem, false, 0));

            } else if (from == TextItem.class) {
                TextItem textItem = (TextItem) item;
                return new Transform(true, new DayRepeatableCheckboxItem(new CheckboxItem(textItem, false), false, 0));
            }
        }

        if (to == CheckboxItem.class) {
            if (from == TextItem.class) {
                TextItem textItem = (TextItem) item;
                return new Transform(true, new CheckboxItem(textItem, false));
            }
        }

        if (to == LongTextItem.class) {
            if (from == TextItem.class) {
                TextItem textItem = (TextItem) item;
                return new Transform(true, new LongTextItem(textItem, ""));
            }
        }

        return new Transform(false, null);
    }

    public static class Transform {
        private final boolean allow;
        private final Item result;

        public Transform(boolean allow, Item result) {
            this.allow = allow;
            this.result = result;
        }

        public boolean isAllow() {
            return allow;
        }

        public Item getResult() {
            return result;
        }
    }
}
