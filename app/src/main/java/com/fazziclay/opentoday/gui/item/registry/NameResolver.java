package com.fazziclay.opentoday.gui.item.registry;

import android.content.Context;

public interface NameResolver {
    String resolveName(Context context);
    String resolveDescription(Context context);
}
