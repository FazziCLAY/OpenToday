package com.fazziclay.opentoday.app.items;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.core.app.RemoteInput;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.settings.SettingsManager;
import com.fazziclay.opentoday.app.items.tab.TabsManager;
import com.fazziclay.opentoday.gui.GuiItemsHelper;
import com.fazziclay.opentoday.gui.fragment.item.ItemsTabIncludeFragment;
import com.fazziclay.opentoday.util.RandomUtil;

import java.util.UUID;

public class QuickNoteReceiver extends BroadcastReceiver {
    public static final String REMOTE_INPUT_KEY = "opentoday_quick_note_remote_input";
    public static final int NOTIFICATION_ID = 10;
    public static final String NOTIFICATION_CHANNEL = "quick_note";

    public static void sendQuickNoteNotification(final Context context) {
        final int flags;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            flags = PendingIntent.FLAG_MUTABLE;
        } else {
            flags = 0;
        }
        NotificationCompat.Action action = new NotificationCompat.Action.Builder(R.drawable.ic_launcher_foreground, context.getString(R.string.quickNote), PendingIntent.getBroadcast(context, RandomUtil.nextInt(), new Intent(context, QuickNoteReceiver.class), flags))
                .addRemoteInput(new RemoteInput.Builder(QuickNoteReceiver.REMOTE_INPUT_KEY).setLabel(context.getString(R.string.quickNote)).build())
                .build();

        context.getSystemService(NotificationManager.class).notify(QuickNoteReceiver.NOTIFICATION_ID,
                new NotificationCompat.Builder(context, QuickNoteReceiver.NOTIFICATION_CHANNEL)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setSilent(true)
                        .setSound(null)
                        .setShowWhen(false)
                        .addAction(action)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .build());
    }

    public static void cancelQuickNoteNotification(final Context context) {
        context.getSystemService(NotificationManager.class).cancel(QuickNoteReceiver.NOTIFICATION_ID);
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final App app = App.get(context);
        final SettingsManager settingsManager = app.getSettingsManager();
        final TabsManager tabsManager = app.getTabsManager();
        final boolean rawTextMode;
        final String rawText;

        if (intent.getExtras() != null && intent.getExtras().containsKey("rawText")) {
            rawTextMode = true;
            rawText = intent.getExtras().getString("rawText");

        } else {
            rawTextMode = false;
            final Bundle bundle = RemoteInput.getResultsFromIntent(intent);
            if (bundle != null) {
                rawText = String.valueOf(bundle.getCharSequence(REMOTE_INPUT_KEY));
            } else {
                rawText = null;
            }
        }
        if (rawText != null) {
            final Item item = GuiItemsHelper.createItem(context, settingsManager.getDefaultQuickNoteType(), context.getString(R.string.quickNote_notificationPattern, rawText), settingsManager);
            if (settingsManager.isParseTimeFromQuickNote()) {
                item.addNotifications(ItemsTabIncludeFragment.QUICK_NOTE_NOTIFICATIONS_PARSE.run(rawText));
            }
            UUID itemsStorageIdForQuickNote = settingsManager.getQuickNoteNotificationItemsStorageId();
            ItemsStorage itemsStorage;
            if (itemsStorageIdForQuickNote == null) {
                itemsStorage = tabsManager.getFirstTab();
            } else {
                itemsStorage = tabsManager.getItemsStorageById(itemsStorageIdForQuickNote);
                if (itemsStorage == null) itemsStorage = tabsManager.getFirstTab();
            }
            switch (settingsManager.getItemAddPosition()) {
                case TOP -> itemsStorage.addItem(item, 0);
                case BOTTOM -> itemsStorage.addItem(item);
            }

            if (!rawTextMode) sendQuickNoteNotification(context);
        }
    }
}
