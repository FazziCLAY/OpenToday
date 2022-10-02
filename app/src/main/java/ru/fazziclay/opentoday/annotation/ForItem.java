package ru.fazziclay.opentoday.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

import ru.fazziclay.opentoday.app.items.item.Item;

@Target(ElementType.METHOD)
public @interface ForItem {
    Class<? extends Item>[] key();
}
