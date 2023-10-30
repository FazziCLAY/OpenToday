package com.fazziclay.opentoday.app.icons;

import androidx.annotation.DrawableRes;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.util.Logger;

import java.util.Arrays;

public class IconsRegistry {
    private static final String TAG = "IconsRegistry";
    public static final IconsRegistry REGISTRY = new IconsRegistry();

    private final Icon[] ICONS = new Icon[]{
            new Icon("opentoday", R.mipmap.ic_launcher),
            new Icon("opentoday_beta", R.mipmap.ic_launcher_beta),
            new Icon("none", R.mipmap.ic_launcher),
            new Icon("baseline_celebration_24", R.drawable.baseline_celebration_24),
            new Icon("baseline_thumb_up_24", R.drawable.baseline_thumb_up_24),
            new Icon("baseline_upcoming_24", R.drawable.baseline_upcoming_24),
            new Icon("baseline_waves_24", R.drawable.baseline_waves_24),
            new Icon("baseline_weekend_24", R.drawable.baseline_weekend_24),
            new Icon("baseline_window_24", R.drawable.baseline_window_24),
            new Icon("baseline_yard_24", R.drawable.baseline_yard_24),
            new Icon("baseline_add_alert_24", R.drawable.baseline_add_alert_24),
            new Icon("baseline_account_tree_24", R.drawable.baseline_account_tree_24),
            new Icon("emoji_food_beverage", R.drawable.emoji_food_beverage),

            new Icon("barefoot", R.drawable.barefoot),
            new Icon("bedtime", R.drawable.bedtime),
            new Icon("calendar_clock", R.drawable.calendar_clock),
            new Icon("cloudy_snowing", R.drawable.cloudy_snowing),
            new Icon("emoji_flags", R.drawable.emoji_flags),
            new Icon("face_2", R.drawable.face_2),
            new Icon("fluid", R.drawable.fluid),
            new Icon("gastroenterology", R.drawable.gastroenterology),
            new Icon("home_health", R.drawable.home_health),
            new Icon("info_i", R.drawable.info_i),
            new Icon("recommend", R.drawable.recommend),
            new Icon("rocket_launch", R.drawable.rocket_launch),
            new Icon("sentiment_very_dissatisfied", R.drawable.sentiment_very_dissatisfied),
            new Icon("shopping_basket", R.drawable.shopping_basket),
            new Icon("surgical", R.drawable.surgical),
            new Icon("taunt", R.drawable.taunt),
            new Icon("pill_24px", R.drawable.pill_24px),
            new Icon("tune_24px", R.drawable.tune_24px),

    };
    public final Icon OPENTODAY = getById("opentoday");
    public final Icon NONE = getById("none");

    private IconsRegistry() {
        long l = System.currentTimeMillis();
        Arrays.sort(ICONS, (icon1, icon2) -> {
            if (icon2.getId().contains("opentoday")) return 1;
            return icon1.getId().compareToIgnoreCase(icon2.getId());
        });
        long d = System.currentTimeMillis() - l;
        Logger.i(TAG, "<init> d = " + d);
    }


    public class Icon {
        private final String id;
        private final int resId;

        public Icon(String id, @DrawableRes int resId) {
            this.id = id;
            this.resId = resId;
        }

        public int getResId() {
            return resId;
        }

        public String getId() {
            return id;
        }

        public boolean isNone() {
            return this == NONE;
        }
    }

    public Icon[] getIconsList() {
        return ICONS.clone();
    }

    public Icon getById(String id) {
        for (Icon icon : ICONS) {
            if (icon.id.equalsIgnoreCase(id)) {
                return icon;
            }
        }
        return null;
    }
}
