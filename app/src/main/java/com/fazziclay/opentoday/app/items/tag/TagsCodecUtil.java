package com.fazziclay.opentoday.app.items.tag;

import com.fazziclay.opentoday.app.data.Cherry;
import com.fazziclay.opentoday.app.data.CherryOrchard;

import java.util.ArrayList;
import java.util.List;

public class TagsCodecUtil {
    public static List<ItemTag> importTagsList(final CherryOrchard orchard) {
        final List<ItemTag> ret = new ArrayList<>();

        orchard.forEachCherry((index, cherry) -> {
            var tag = new ItemTag(cherry.getString("name"), cherry.getString("value"));
            ret.add(tag);
        });

        return ret;
    }

    public static CherryOrchard exportTagsList(final List<ItemTag> tags) {
        final CherryOrchard orchard = new CherryOrchard();
        for (final ItemTag tag : tags) {
            final Cherry cherry = orchard.createAndAdd();
            cherry.put("tagType", "pre_registry_generic_tag");
            cherry.put("name", tag.getName());
            cherry.put("value", tag.getValue());
        }

        return orchard;
    }
}
