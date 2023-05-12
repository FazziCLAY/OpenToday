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

    private String print(Object s) {
        System.out.println(s);
        return s + "";
    }
}
