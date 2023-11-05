package com.fazziclay.opentoday.util;

import com.fazziclay.opentoday.app.items.notification.DayItemNotification;
import com.fazziclay.opentoday.app.items.notification.ItemNotification;
import com.fazziclay.opentoday.gui.fragment.item.ItemsTabIncludeFragment;

import java.util.ArrayList;
import java.util.List;

public class QuickNote {
    public static final QuickNoteInterface QUICK_NOTE_NOTIFICATIONS_PARSE = s -> {
        List<ItemNotification> notifys = new ArrayList<>();
        boolean parseTime = true;
        if (parseTime) {
            char[] chars = s.toCharArray();
            int i = 0;
            for (char aChar : chars) {
                if (aChar == ':') {
                    try {
                        if (i >= 2 && chars.length >= 5) {
                            int hours = Integer.parseInt(String.valueOf(chars[i - 2]) + chars[i - 1]);
                            int minutes = Integer.parseInt(String.valueOf(chars[i + 1]) + chars[i + 2]);

                            DayItemNotification noti = new DayItemNotification();
                            noti.setNotificationId(RandomUtil.nextIntPositive());
                            noti.setTime((hours * 60 * 60) + (minutes * 60));
                            noti.setNotifyTitleFromItemText(true);
                            notifys.add(noti);
                        }
                    } catch (Exception ignored) {
                    }
                }
                i++;
            }
        }
        return notifys.toArray(new ItemNotification[0]);
    };

    public interface QuickNoteInterface {
        ItemNotification[] run(String text);
    }
}
