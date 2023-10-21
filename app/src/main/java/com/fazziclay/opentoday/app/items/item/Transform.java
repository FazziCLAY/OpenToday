package com.fazziclay.opentoday.app.items.item;

import java.util.function.Supplier;

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
    public static Result transform(Item item, ItemType to) {
        ItemType from = ItemUtil.getItemType(item);

        if (to == ItemType.SLEEP_TIME) {
            return Result.allow(() -> new SleepTimeItem((TextItem) item));
        }

        if (to == ItemType.TEXT) {
            if (item instanceof TextItem textItem) {
                return Result.allow(() -> new TextItem(textItem));
            }
        }

        if (to == ItemType.COUNTER) {
            if (item instanceof TextItem textItem) {
                return Result.allow(() -> new CounterItem(textItem));
            }
        }

        if (to == ItemType.CHECKBOX_DAY_REPEATABLE) {
            if (from == ItemType.CHECKBOX) {
                return Result.allow(() -> new DayRepeatableCheckboxItem((CheckboxItem) item, false, 0));

            } else if (item instanceof TextItem) {
                return Result.allow(() -> new DayRepeatableCheckboxItem(new CheckboxItem((TextItem) item, false), false, 0));
            }
        }

        if (to == ItemType.CHECKBOX) {
            if (item instanceof CheckboxItem) {
                return Result.allow(() -> new CheckboxItem((CheckboxItem) item));
            }

            if (item instanceof TextItem) {
                return Result.allow(() -> new CheckboxItem((TextItem) item, false));
            }
        }

        if (to == ItemType.LONG_TEXT) {
            if (item instanceof ContainerItem containerItem) {
                return Result.allow(() -> {
                    String text = "";
                    for (Item intItem : containerItem.getAllItems()) {
                        String t = ((intItem instanceof CheckboxItem checkboxItem) ? (checkboxItem.isChecked() ? "[*]" : "[ ]") : "") + intItem.getText();
                        text += t + "\n\n";
                    }
                    return new LongTextItem((TextItem) item, text);
                });
            }

            if (item instanceof TextItem) {
                return Result.allow(() -> new LongTextItem((TextItem) item, item.getText()));
            }
        }

        if (to == ItemType.GROUP) {
            return Result.allow(() -> new GroupItem((TextItem) item, getContainer(item)));
        }

        if (to == ItemType.CYCLE_LIST) {
            return Result.allow(() -> new CycleListItem((TextItem) item, getContainer(item)));
        }

        if (to == ItemType.FILTER_GROUP) {
            return Result.allow(() -> new FilterGroupItem((TextItem) item, getContainer(item)));
        }

        if (to == ItemType.MATH_GAME) {
            if (from.isInherit(ItemType.TEXT)) {
                return Result.allow(() -> new MathGameItem((TextItem) item));
            }
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

    public static boolean isAllow(Item item, ItemType type) {
        return transform(item, type).isAllow();
    }

    /**
     * Return of {@link #transform(Item, ItemType)} function
     */
    public static class Result {
        public static Result NOT_ALLOW = new Result(false, null);

        public static Result allow(Supplier<Item> item) {
            return new Result(true, item);
        }

        private final boolean allow;
        private final Supplier<Item> result;

        public Result(boolean allow, Supplier<Item> result) {
            this.allow = allow;
            this.result = result;
        }

        public boolean isAllow() {
            return allow;
        }

        public Item generate() {
            return result.get();
        }
    }
}
