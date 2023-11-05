package com.fazziclay.opentoday.app.icons;

import androidx.annotation.DrawableRes;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.util.Logger;
import com.fazziclay.opentoday.util.RandomUtil;

import java.util.Arrays;

public class IconsRegistry {
    private static final String TAG = "IconsRegistry";
    public static final IconsRegistry REGISTRY = new IconsRegistry();

    private final Icon[] ICONS = new Icon[]{
            new Icon("opentoday", R.mipmap.ic_launcher),
            new Icon("opentoday_beta", R.mipmap.ic_launcher_beta),
            new Icon("none", R.drawable.close_24px), // special icon



            new Icon("add", R.drawable.add_24px),
            new Icon("aspect_ratio", R.drawable.aspect_ratio_24px),
            new Icon("background_replace", R.drawable.background_replace_24px),
            new Icon("barefoot", R.drawable.barefoot_24px),
            new Icon("baseline_account_tree", R.drawable.baseline_account_tree_24),
            new Icon("baseline_add_alert", R.drawable.baseline_add_alert_24),
            new Icon("baseline_celebration", R.drawable.baseline_celebration_24),
            new Icon("baseline_thumb_up", R.drawable.baseline_thumb_up_24),
            new Icon("baseline_upcoming", R.drawable.baseline_upcoming_24),
            new Icon("baseline_waves", R.drawable.baseline_waves_24),
            new Icon("baseline_weekend", R.drawable.baseline_weekend_24),
            new Icon("baseline_window", R.drawable.baseline_window_24),
            new Icon("baseline_yard", R.drawable.baseline_yard_24),
            new Icon("bed", R.drawable.bed_24px),
            new Icon("bedtime", R.drawable.bedtime_24px),
            new Icon("calendar_clock", R.drawable.calendar_clock_24px),
            new Icon("calendar_today", R.drawable.calendar_today_24px),
            new Icon("check_box_outline_blank", R.drawable.check_box_outline_blank_24px),
            new Icon("close", R.drawable.close_24px),
            new Icon("cloudy_snowing", R.drawable.cloudy_snowing_24px),
            new Icon("content_copy", R.drawable.content_copy_24px),
            new Icon("delete", R.drawable.delete_24px),
            new Icon("edit", R.drawable.edit_24px),
            new Icon("edit_note", R.drawable.edit_note_24px),
            new Icon("emoji_flags", R.drawable.emoji_flags_24px),
            new Icon("emoji_food_beverage", R.drawable.emoji_food_beverage_24px),
            new Icon("emoji_objects", R.drawable.emoji_objects_24px),
            new Icon("export_notes", R.drawable.export_notes_24px),
            new Icon("face_2", R.drawable.face_2_24px),
            new Icon("fluid", R.drawable.fluid_24px),
            new Icon("format_bold", R.drawable.format_bold_24px),
            new Icon("format_italic", R.drawable.format_italic_24px),
            new Icon("format_size", R.drawable.format_size_24px),
            new Icon("format_strikethrough", R.drawable.format_strikethrough_24px),
            new Icon("gastroenterology", R.drawable.gastroenterology_24px),
            new Icon("handyman", R.drawable.handyman_24px),
            new Icon("home_health", R.drawable.home_health_24px),
            new Icon("home_work", R.drawable.home_work_24px),
            new Icon("info_i", R.drawable.info_i_24px),
            new Icon("minimize", R.drawable.minimize_24px),
            new Icon("new_label", R.drawable.new_label_24px),
            new Icon("notifications", R.drawable.notifications_24px),
            new Icon("opacity", R.drawable.opacity_24px),
            new Icon("palette", R.drawable.palette_24px),
            new Icon("pause_presentation", R.drawable.pause_presentation_24px),
            new Icon("pill", R.drawable.pill_24px),
            new Icon("procedure", R.drawable.procedure_24px),
            new Icon("psychiatry", R.drawable.psychiatry_24px),
            new Icon("recommend", R.drawable.recommend_24px),
            new Icon("redeem", R.drawable.redeem_24px),
            new Icon("repeat", R.drawable.repeat_24px),
            new Icon("rocket", R.drawable.rocket_24px),
            new Icon("rocket_launch", R.drawable.rocket_launch_24px),
            new Icon("save", R.drawable.save_24px),
            new Icon("select", R.drawable.select_24px),
            new Icon("sentiment_stressed", R.drawable.sentiment_stressed_24px),
            new Icon("sentiment_very_dissatisfied", R.drawable.sentiment_very_dissatisfied_24px),
            new Icon("sentiment_worried", R.drawable.sentiment_worried_24px),
            new Icon("shopping_basket", R.drawable.shopping_basket_24px),
            new Icon("shopping_cart", R.drawable.shopping_cart_24px),
            new Icon("store", R.drawable.store_24px),
            new Icon("surgical", R.drawable.surgical_24px),
            new Icon("swipe_left", R.drawable.swipe_left_24px),
            new Icon("syringe", R.drawable.syringe_24px),
            new Icon("taunt", R.drawable.taunt_24px),
            new Icon("touch_app", R.drawable.touch_app_24px),
            new Icon("tune", R.drawable.tune_24px),

    };
    public final Icon OPENTODAY = getById("opentoday");
    public final Icon NONE = getById("none");

    private IconsRegistry() {
    }

    public Icon randomIcon() {
        return getIconsList()[RandomUtil.bounds(0, getIconsList().length)];
    }


    public class Icon {
        private final String id;
        private final int resId;

        public Icon(String id, @DrawableRes int resId) {
            this.id = id;
            this.resId = resId;
        }

        @DrawableRes
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
