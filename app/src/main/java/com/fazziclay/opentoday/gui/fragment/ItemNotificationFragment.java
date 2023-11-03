package com.fazziclay.opentoday.gui.fragment;

import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.items.ItemsRoot;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.notification.DayItemNotification;
import com.fazziclay.opentoday.app.items.notification.ItemNotification;
import com.fazziclay.opentoday.databinding.DialogItemNotificationBinding;
import com.fazziclay.opentoday.gui.ActivitySettings;
import com.fazziclay.opentoday.gui.ColorPicker;
import com.fazziclay.opentoday.gui.UI;
import com.fazziclay.opentoday.gui.dialog.IconSelectorDialog;
import com.fazziclay.opentoday.gui.interfaces.ActivitySettingsMember;
import com.fazziclay.opentoday.util.MinTextWatcher;
import com.fazziclay.opentoday.util.time.ConvertMode;
import com.fazziclay.opentoday.util.time.HumanTimeType;
import com.fazziclay.opentoday.util.time.TimeUtil;

import java.util.UUID;

public class ItemNotificationFragment extends Fragment implements ActivitySettingsMember {
    private static final String KEY_ITEM_ID = "itemId";
    private static final String KEY_NOTIFY_ID = "notifyId";

    public static ItemNotificationFragment create(UUID itemId, UUID notificationId) {
        Bundle b = new Bundle();
        b.putString(KEY_ITEM_ID, itemId.toString());
        b.putString(KEY_NOTIFY_ID, notificationId.toString());

        ItemNotificationFragment f = new ItemNotificationFragment();
        f.setArguments(b);
        return f;
    }

    private Context context;
    private App app;
    private ItemsRoot itemsRoot;
    private Item item;
    private DayItemNotification notification;


    private DialogItemNotificationBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.context = requireContext();
        this.app = App.get(context);
        this.itemsRoot = app.getItemsRoot();

        Bundle args = getArguments();
        UUID itemId = UUID.fromString(args.getString(KEY_ITEM_ID));
        UUID notifyId = UUID.fromString(args.getString(KEY_NOTIFY_ID));

        this.item = itemsRoot.getItemById(itemId);
        this.notification = (DayItemNotification) item.getNotificationById(notifyId);


        setupActivitySettings();
    }

    private void setupActivitySettings() {
        UI.getUIRoot(this).pushActivitySettings(a -> {
            a.setToolbarSettings(ActivitySettings.ToolbarSettings.createBack(R.string.fragment_itemNotification_title, () -> UI.rootBack(this)).setMenu(R.menu.menu_notification, new ActivitySettings.ToolbarSettings.MenuInterface() {
                @Override
                public void run(Menu menu) {
                    MenuItem item1 = menu.findItem(R.id.notificationDelete);
                    item1.setOnMenuItemClickListener(menuItem -> {
                        deleteRequest();
                        return true;
                    });
                }
            }));
            a.setClockVisible(true);
            a.setDateClickCalendar(true);
            a.analogClockForceHidden(true);
        });
    }

    private void deleteRequest() {
        showDeleteNotificationDialog(context, () -> {
            item.removeNotifications(notification);
            item.save();
            UI.rootBack(this);
        });
    }

    public static void showDeleteNotificationDialog(Context context, Runnable onDelete) {
        new AlertDialog.Builder(context)
                .setIcon(R.drawable.delete_24px)
                .setTitle(R.string.fragment_itemNotification_delete_title)
                .setNegativeButton(R.string.abc_cancel, null)
                .setPositiveButton(R.string.fragment_itemNotification_delete_apply, (ign1, ign2) -> onDelete.run())
                .show();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DialogItemNotificationBinding.inflate(inflater, container, false);
        setupView();
        return binding.getRoot();
    }

    private void setupView() {
        // icon
        Drawable drawable = AppCompatResources.getDrawable(context, notification.getIcon().getResId());
        binding.icon.setImageDrawable(drawable);
        binding.icon.setScaleType(ImageView.ScaleType.FIT_XY);
        binding.icon.setOnClickListener(ignore -> new IconSelectorDialog(context, icon -> {
            notification.setIcon(icon);
            Drawable drawable1 = AppCompatResources.getDrawable(context, notification.getIcon().getResId());
            binding.icon.setImageDrawable(drawable1);
        }).show());

        // id
        binding.notificationId.setText(String.valueOf(notification.getNotificationId()));
        MinTextWatcher.after(binding.notificationId, () -> {
            try {
                int i = Integer.parseInt(binding.notificationId.getText().toString());
                notification.setNotificationId(i);
            } catch (Exception ignored) {
                notification.setNotificationId(0);
                binding.notificationId.setText("0");
                binding.notificationId.setSelection(1);
            }
        });

        // text
        binding.text.setText(notification.getNotifyText());
        binding.textFromItem.setChecked(notification.isNotifyTextFromItemText());
        binding.textFromItem.setOnClickListener(vvv -> {
            binding.text.setEnabled(!binding.textFromItem.isChecked());
            notification.setNotifyTextFromItemText(binding.textFromItem.isChecked());
        });
        binding.text.setEnabled(!binding.textFromItem.isChecked());
        MinTextWatcher.after(binding.text, () -> notification.setNotifyText(binding.text.getText().toString()));


        // title
        binding.title.setText(notification.getNotifyTitle());
        binding.titleFromItem.setChecked(notification.isNotifyTitleFromItemText());
        binding.titleFromItem.setOnClickListener(vvv -> {
            binding.title.setEnabled(!binding.titleFromItem.isChecked());
            notification.setNotifyTitleFromItemText(binding.titleFromItem.isChecked());
        });
        binding.title.setEnabled(!binding.titleFromItem.isChecked());
        MinTextWatcher.after(binding.title, () -> notification.setNotifyTitle(binding.title.getText().toString()));

        // sub text
        binding.notifySubText.setText(notification.getNotifySubText());
        MinTextWatcher.after(binding.notifySubText, () -> notification.setNotifySubText(binding.notifySubText.getText().toString()));

        // send test notify
        binding.test.setOnClickListener(v2132321 -> {
            notification.sendNotify(App.get(context).getItemNotificationHandler());
        });

        // time
        binding.time.setText(context.getString(R.string.dialog_itemNotification_time, TimeUtil.convertToHumanTime(notification.getTime(), ConvertMode.HHMM)));
        binding.time.setOnClickListener(_ignore -> new TimePickerDialog(context, (view, hourOfDay, minute) -> {
            notification.setTime((hourOfDay * 60 * 60) + (minute * 60));
            notification.setLatestDayOfYear(0);
            item.save();
            binding.time.setText(context.getString(R.string.dialog_itemNotification_time, TimeUtil.convertToHumanTime(notification.getTime(), ConvertMode.HHMM)));
        }, TimeUtil.getHumanValue(notification.getTime(), HumanTimeType.HOUR), TimeUtil.getHumanValue(notification.getTime(), HumanTimeType.MINUTE_OF_HOUR), true).show());

        // fullscreen
        binding.fullScreen.setOnCheckedChangeListener((compoundButton, b1) -> {
            binding.previewViewOnly.setEnabled(b1);
            binding.sound.setEnabled(b1);
        });
        binding.fullScreen.setOnClickListener(i____ -> notification.setFullScreen(binding.fullScreen.isChecked()));
        binding.fullScreen.setChecked(notification.isFullScreen());
        binding.previewViewOnly.setEnabled(notification.isFullScreen());
        binding.sound.setEnabled(notification.isFullScreen());

        binding.previewViewOnly.setChecked(notification.isPreRenderPreviewMode());
        binding.previewViewOnly.setOnClickListener(i___ -> notification.setPreRenderPreviewMode(binding.previewViewOnly.isChecked()));

        binding.sound.setChecked(notification.isSound());
        binding.sound.setOnClickListener(i____ -> notification.setSound(binding.sound.isChecked()));

        // color
        if (notification.getColor() == ItemNotification.DEFAULT_COLOR) {
            binding.notificationColor.setColorSet(false);
        } else {
            binding.notificationColor.setColor(notification.getColor());
        }
        binding.notificationColor.setOnClickListener(vvvvv123daqb -> {
            new ColorPicker(context)
                    .setting(true, true, true)
                    .setStartColor(notification.getColor())
                    .setColorHistoryManager(App.get().getColorHistoryManager())
                    .setNeutralDialogButton(R.string.dialog_itemNotification_defaultColor, () -> {
                        binding.notificationColor.setColorSet(false);
                        notification.setColor(ItemNotification.DEFAULT_COLOR);
                    })
                    .showDialog(R.string.dialog_itemNotification_notificationColor, R.string.abc_cancel, R.string.abc_ok, color -> {
                        binding.notificationColor.setColor(color);
                        notification.setColor(color);
                    });
        });
    }
}
