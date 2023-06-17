package com.fazziclay.opentoday.tests.cherry;

import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.app.data.CherryOrchard;

import org.junit.Assert;
import org.junit.Test;

public class CherryTest {
    @Test
    public void emptyCherry() {
        Cherry cherry = new Cherry();
        Assert.assertEquals("empty cherry", print(cherry), "Cherry{}");
    }

    @Test
    public void notificationsTest() {
        Cherry cherry = new Cherry();
        CherryOrchard orchard = cherry.getOrchard("sdgfgfdgfd");
        Assert.assertNull(orchard);

        long[] arr = CherryOrchard.parseLongArray(orchard, new long[]{50, 100, 50, 200});
        Assert.assertArrayEquals(arr, new long[]{50, 100, 50, 200});

        cherry.put("wtrtf", CherryOrchard.of(arr));

        CherryOrchard orchard2 = cherry.getOrchard("wtrtf");
        long[] arr2 = CherryOrchard.parseLongArray(orchard2, new long[]{50, -1});
        Assert.assertArrayEquals(arr2, new long[]{50, 100, 50, 200});
    }

    @Test
    public void putValues() {
        Cherry cherry = new Cherry();
        cherry.put("key1", "values");
        Assert.assertEquals("put value", print(cherry), "Cherry{\"key1\":\"values\"}");

        print("State: Set null to 'key1'");
        cherry.put("key1", (String) null);
        cherry.put("key1", (CherryOrchard) null);
        cherry.put("key1", (Enum<?>) null);
        cherry.put("key1", (Cherry) null);

        Assert.assertEquals(print(cherry), "Cherry{}");

        cherry.createOrchard("orchard");
        Assert.assertEquals(print(cherry), "Cherry{\"orchard\":[]}");
    }

    @Test
    public void orchardShadowAccess() {
        Cherry cherry = new Cherry();
        CherryOrchard orchard = cherry.createOrchard("orchard");

        Assert.assertEquals(print(cherry), "Cherry{\"orchard\":[]}");

        orchard.put("Hello!");
        Assert.assertEquals(print(cherry), "Cherry{\"orchard\":[\"Hello!\"]}");

    }

    @Test
    public void stringsOrchard() {
        CherryOrchard.of(new String[0]);
        CherryOrchard.of(new String[]{"arg1"});
        CherryOrchard.of(new String[]{"arg1", "arg2"});
        CherryOrchard o = CherryOrchard.of(new String[]{"arg1", "arg2", "arg3"});
        o.forEachString((index, value) -> {
            if (index == 0) Assert.assertEquals(value, "arg1");
            if (index == 1) Assert.assertEquals(value, "arg2");
            if (index == 2) Assert.assertEquals(value, "arg3");
        });
    }

    private String print(Object s) {
        System.out.println(s);
        return s + "";
    }
}
