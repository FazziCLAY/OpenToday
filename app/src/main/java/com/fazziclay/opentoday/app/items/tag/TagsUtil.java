package com.fazziclay.opentoday.app.items.tag;

import java.util.ArrayList;
import java.util.List;

public class TagsUtil {
    public static List<ItemTag> copy(final List<ItemTag> tags) {
        final List<ItemTag> ret = new ArrayList<>();

        for (final ItemTag tag : tags) {
            ret.add(copy(tag));
        }

        return ret;
    }

    public static ItemTag copy(final ItemTag itemTag) {
        return itemTag.copy();
    }
}
