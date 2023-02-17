package com.fazziclay.opentoday.util.annotation;

import com.fazziclay.opentoday.app.items.item.Item;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
public @interface ForItem {
    Class<? extends Item>[] key();
}
