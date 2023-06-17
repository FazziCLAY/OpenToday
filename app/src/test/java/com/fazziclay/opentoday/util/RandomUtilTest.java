package com.fazziclay.opentoday.util;

import org.junit.Test;

public class RandomUtilTest {
    @Test
    public void test() {
        int i = 0;
        while (i < 7000) {
            int b = RandomUtil.bounds(-977, 1000);
            if (b > 1000 || b < -977) throw new RuntimeException("Index out bounds: " + b);
            if (b == -977 || b == 1000) System.out.println("b="+b);
            i++;
        }
    }
}
