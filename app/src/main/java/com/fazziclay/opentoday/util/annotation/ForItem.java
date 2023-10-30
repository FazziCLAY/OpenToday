package com.fazziclay.opentoday.util.annotation;

import com.fazziclay.opentoday.app.items.item.ItemType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Specify item
 */
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface ForItem {
    ItemType[] k();
}
