package ru.fazziclay.opentoday.tests;

import org.junit.Test;

import java.io.File;

import ru.fazziclay.opentoday.app.items.ItemManager;
import ru.fazziclay.opentoday.app.items.item.TextItem;
import ru.fazziclay.opentoday.util.DebugUtil;

public class TestTest {
    @Test
    public void ddd() {
        ItemManager itemManager = new ItemManager(new File("./test/items.json"));
        itemManager.addItem(new TextItem("owo"));
        itemManager.save();
        DebugUtil.sleep(1000);
    }
}
