package com.fazziclay.opentoday.app;

import com.fazziclay.javaneoutil.FileUtil;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class ColorHistoryManagerTest {
    @Test
    public void test() {
        File file = new File("tests/colorhistorymanager");
        FileUtil.delete(file);

        ColorHistoryManager colorHistoryManager = new ColorHistoryManager(file, 10);
        Assert.assertArrayEquals(colorHistoryManager.getHistory(10), new int[0]);
        Assert.assertArrayEquals(colorHistoryManager.getHistory(100), new int[0]);
        Assert.assertArrayEquals(colorHistoryManager.getHistory(0), new int[0]);

        colorHistoryManager.addColor(771);
        Assert.assertArrayEquals(colorHistoryManager.getHistory(0), new int[0]);
        Assert.assertArrayEquals(colorHistoryManager.getHistory(1), new int[]{771});
        Assert.assertArrayEquals(colorHistoryManager.getHistory(100), new int[]{771});

        // Expected: nothing while addColor(...)
        colorHistoryManager.setLocked(true);
        colorHistoryManager.addColor(234783);
        Assert.assertArrayEquals(colorHistoryManager.getHistory(100), new int[]{771});

        colorHistoryManager.setLocked(false);


        // Expected: no duplicated colors
        colorHistoryManager.addColor(771);
        Assert.assertArrayEquals(colorHistoryManager.getHistory(0), new int[0]);
        Assert.assertArrayEquals(colorHistoryManager.getHistory(1), new int[]{771});
        Assert.assertArrayEquals(colorHistoryManager.getHistory(100), new int[]{771});

        // Expected: no duplicated colors
        colorHistoryManager.addColor(222);
        Assert.assertArrayEquals(colorHistoryManager.getHistory(0), new int[0]);
        Assert.assertArrayEquals(colorHistoryManager.getHistory(1), new int[]{222});
        Assert.assertArrayEquals(colorHistoryManager.getHistory(100), new int[]{222, 771});
    }
}
