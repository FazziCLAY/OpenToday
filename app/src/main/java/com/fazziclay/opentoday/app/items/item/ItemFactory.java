package com.fazziclay.opentoday.app.items.item;

public interface ItemFactory <I extends Item> {
    I create();
    I copy(Item item);
    Transform.Result transform(Item from);
}
