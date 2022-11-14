package com.fazziclay.opentoday.app.items.item;

public class ExperimentalTransform {
    public static Transform transform(Item item, Class<? extends Item> to) {
        Class<? extends Item> from = item.getClass();


        if (to == DayRepeatableCheckboxItem.class) {
            if (from == CheckboxItem.class) {
                CheckboxItem checkboxItem = (CheckboxItem) item;
                return Transform.allow(new DayRepeatableCheckboxItem(checkboxItem, false, 0));

            } else if (from == TextItem.class) {
                TextItem textItem = (TextItem) item;
                return Transform.allow(new DayRepeatableCheckboxItem(new CheckboxItem(textItem, false), false, 0));
            }
        }

        if (to == CheckboxItem.class) {
            if (from == TextItem.class) {
                TextItem textItem = (TextItem) item;
                return Transform.allow(new CheckboxItem(textItem, false));
            }
        }

        if (to == LongTextItem.class) {
            if (from == TextItem.class) {
                TextItem textItem = (TextItem) item;
                return Transform.allow(new LongTextItem(textItem, ""));
            }
        }

        if (to == GroupItem.class) {
            GroupItem groupItem = new GroupItem((TextItem) item, getContainer(item));
            return Transform.allow(groupItem);
        }

        if (to == CycleListItem.class) {
            CycleListItem cycleListItem = new CycleListItem((TextItem) item, getContainer(item));
            return Transform.allow(cycleListItem);
        }

        if (to == FilterGroupItem.class) {
            FilterGroupItem filterGroupItem = new FilterGroupItem((TextItem) item, getContainer(item));
            return Transform.allow(filterGroupItem);
        }
        return Transform.NOT_ALLOW;
    }

    private static boolean isItemContainer(Item item) {
        return item instanceof ContainerItem;
    }

    private static ContainerItem getContainer(Item item) {
        if (isItemContainer(item)) {
            return (ContainerItem) item;
        }
        return null;
    }

    public static class Transform {
        public static Transform NOT_ALLOW = new Transform(false, null);

        public static Transform allow(Item item) {
            return new Transform(true, item);
        }

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
