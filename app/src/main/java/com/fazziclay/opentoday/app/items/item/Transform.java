package com.fazziclay.opentoday.app.items.item;

public class Transform {
    /**
     * <h1>Transforms</h1>
     * <h2>TextItem</h2>
     * <p>TextItem -> CounterItem</p>
     * <p>TextItem -> DayRepeatableCheckboxItem</p>
     * <p>TextItem -> CheckboxItem</p>
     * <p>TextItem -> LongTextItem</p>
     * <p>TextItem -> GroupItem</p>
     * <p>TextItem -> CycleListItem</p>
     * <p>TextItem -> FilterGroupItem</p>
     * <h2>Checkbox</h2>
     * <p>CheckboxItem -> DayRepeatableCheckboxItem</p>

     * <h2>Containers</h2>
     * <p>*any container* -> GroupItem</p>
     * <p>*any container* -> FilterGroupItem</p>
     * <p>*any container* -> CycleListItem</p>

     *
     * @param item from
     * @param to to
     * @return {@link Result}
     */
    public static Result transform(Item item, Class<? extends Item> to) {
        Class<? extends Item> from = item.getClass();

        if (to == CounterItem.class) {
            if (from == TextItem.class) {
                TextItem textItem = (TextItem) item;
                return Result.allow(new CounterItem(textItem));
            }
        }

        if (to == DayRepeatableCheckboxItem.class) {
            if (from == CheckboxItem.class) {
                CheckboxItem checkboxItem = (CheckboxItem) item;
                return Result.allow(new DayRepeatableCheckboxItem(checkboxItem, false, 0));

            } else if (from == TextItem.class) {
                TextItem textItem = (TextItem) item;
                return Result.allow(new DayRepeatableCheckboxItem(new CheckboxItem(textItem, false), false, 0));
            }
        }

        if (to == CheckboxItem.class) {
            if (from == TextItem.class) {
                TextItem textItem = (TextItem) item;
                return Result.allow(new CheckboxItem(textItem, false));
            }
        }

        if (to == LongTextItem.class) {
            if (from == TextItem.class) {
                TextItem textItem = (TextItem) item;
                return Result.allow(new LongTextItem(textItem, ""));
            }
        }

        if (to == GroupItem.class) {
            GroupItem groupItem = new GroupItem((TextItem) item, getContainer(item));
            return Result.allow(groupItem);
        }

        if (to == CycleListItem.class) {
            CycleListItem cycleListItem = new CycleListItem((TextItem) item, getContainer(item));
            return Result.allow(cycleListItem);
        }

        if (to == FilterGroupItem.class) {
            FilterGroupItem filterGroupItem = new FilterGroupItem((TextItem) item, getContainer(item));
            return Result.allow(filterGroupItem);
        }
        return Result.NOT_ALLOW;
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

    /**
     * Return of {@link #transform(Item, Class)} function
     */
    public static class Result {
        public static Result NOT_ALLOW = new Result(false, null);

        public static Result allow(Item item) {
            return new Result(true, item);
        }

        private final boolean allow;
        private final Item result;

        public Result(boolean allow, Item result) {
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
