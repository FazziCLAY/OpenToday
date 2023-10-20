package com.fazziclay.opentoday.app;

import android.content.Context;
import android.graphics.Color;

import com.fazziclay.opentoday.util.RandomUtil;
import com.fazziclay.opentoday.util.StreamUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BeautifyColorManager {
    private final Context context;
    private final List<Integer> colors = new ArrayList<>();

    public BeautifyColorManager(Context context) {
        this.context = context;
        load();
    }

    public static int randomBackgroundColor(Context context) {
        return App.get(context).getBeautifyColorManager().randomBackgroundColor();
    }

    public int randomBackgroundColor() {
        if (colors.isEmpty()) {
            return RandomUtil.nextInt();
        }

        int i = RandomUtil.nextInt(colors.size());
        return colors.get(i);
    }

    private void load() {
        try {
            for (String s : StreamUtil.read(context.getAssets().open("beautify_colors.txt")).split("\n")) {
                if (s.startsWith("//")) continue;
                s = s.split(";")[0];
                try {
                    colors.add(Color.parseColor(s));
                } catch (Exception ignored) {}
            }
        } catch (IOException e) {
            App.exception(App.get(), e);
        }
    }
}
