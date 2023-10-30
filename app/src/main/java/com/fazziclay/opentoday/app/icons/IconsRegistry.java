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
        return OPENTODAY;
    }
}
