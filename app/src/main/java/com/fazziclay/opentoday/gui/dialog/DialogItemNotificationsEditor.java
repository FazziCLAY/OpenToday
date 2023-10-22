package com.fazziclay.opentoday.gui.dialog;

import static com.fazziclay.opentoday.util.InlineUtil.viewClick;
import static com.fazziclay.opentoday.util.InlineUtil.viewVisible;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.icons.IconsRegistry;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.notification.DayItemNotification;
import com.fazziclay.opentoday.app.items.notification.ItemNotification;
import com.fazziclay.opentoday.databinding.DialogItemNotificationBinding;
import com.fazziclay.opentoday.databinding.DialogItemNotificationsEditorBinding;
import com.fazziclay.opentoday.databinding.ItemNotificationBinding;
import com.fazziclay.opentoday.util.MinBaseAdapter;
import com.fazziclay.opentoday.util.MinTextWatcher;
import com.fazziclay.opentoday.util.RandomUtil;
import com.fazziclay.opentoday.util.time.ConvertMode;
import com.fazziclay.opentoday.util.time.HumanTimeType;
import com.fazziclay.opentoday.util.time.TimeUtil;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.function.Consumer;

public class DialogItemNotificationsEditor {
    private final Activity activity;
    private final Item item;
    private final Runnable onApply;
    private final Dialog dialog;
    private final DialogItemNotificationsEditorBinding binding;
    private final View view;

    // TODO: 07.06.2023 fix all
    public DialogItemNotificationsEditor(Activity activity, Item item, Runnable o) {

        this.activity = activity;
        this.item = item;
        this.onApply = o;

        dialog = new Dialog(activity, android.R.style.ThemeOverlay_Material_ActionBar);
        dialog.setOnCancelListener(dialog -> onApply.run());

        binding = DialogItemNotificationsEditorBinding.inflate(activity.getLayoutInflater());
        view = binding.getRoot();

        viewClick(binding.cancelButton, dialog::cancel);
        updateEmptyView(item.getNotifications().length == 0);
        binding.list.setAdapter(new MinBaseAdapter() {
            @Override
            public int getCount() {
                return item.getNotifications().length;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ItemNotification itemNotification = item.getNotifications()[position];

                ItemNotificationBinding b = ItemNotificationBinding.inflate(activity.getLayoutInflater(), parent, false);

                if (itemNotification instanceof DayItemNotification d) {
                    b.text.setText(String.format("#%s - %s - %s", d.getNotificationId(), activity.getString(R.string.itemNotification_day), TimeUtil.convertToHumanTime(d.getTime(), ConvertMode.HHMM)));
                }

                b.delete.setOnClickListener(v -> {
                    new AlertDialog.Builder(activity)
                            .setTitle(R.string.fragment_itemEditor_delete_title)
                            .setPositiveButton(R.string.fragment_itemEditor_delete_apply, (ee, eee) -> {
                                item.removeNotifications(itemNotification);
                                item.save();
                                notifyDataSetChanged();
                                updateEmptyView(item.getNotifications().length == 0);
                            })
                            .setNegativeButton(R.string.fragment_itemEditor_delete_cancel, null)
                            .show();
                });


                b.getRoot().setOnClickListener(v -> {
                    DialogItemNotificationBinding l = DialogItemNotificationBinding.inflate(activity.getLayoutInflater());

                    DayItemNotification d = (DayItemNotification) itemNotification;

                    Drawable drawable = AppCompatResources.getDrawable(activity, d.getIcon().getResId());
                    l.icon.setImageDrawable(drawable);
                    l.icon.setScaleType(ImageView.ScaleType.FIT_XY);
                    l.icon.setOnClickListener(ignore -> new IconSelectorDialog(activity, icon -> {
                        d.setIcon(icon);
                        Drawable drawable1 = AppCompatResources.getDrawable(activity, d.getIcon().getResId());
                        l.icon.setImageDrawable(drawable1);
                    }).show());

                    l.notificationId.setText(String.valueOf(d.getNotificationId()));
                    MinTextWatcher.after(l.notificationId, () -> {
                        try {
                            int i = Integer.parseInt(l.notificationId.getText().toString());
                            d.setNotificationId(i);
                        } catch (Exception ignored) {
                            d.setNotificationId(0);
                            l.notificationId.setText("0");
                        }
                    });
                    l.text.setText(d.getNotifyText());
                    l.textFromItem.setChecked(d.isNotifyTextFromItemText());
                    l.textFromItem.setOnClickListener(vvv -> {
                        l.text.setEnabled(!l.textFromItem.isChecked());
                        d.setNotifyTextFromItemText(l.textFromItem.isChecked());
                    });
                    l.text.setEnabled(!l.textFromItem.isChecked());
                    MinTextWatcher.after(l.text, () -> d.setNotifyText(l.text.getText().toString()));
                    l.title.setText(d.getNotifyTitle());
                    l.titleFromItem.setChecked(d.isNotifyTitleFromItemText());
                    l.titleFromItem.setOnClickListener(vvv -> {
                        l.title.setEnabled(!l.titleFromItem.isChecked());
                        d.setNotifyTitleFromItemText(l.titleFromItem.isChecked());
                    });
                    l.title.setEnabled(!l.titleFromItem.isChecked());
                    MinTextWatcher.after(l.title, () -> d.setNotifyTitle(l.title.getText().toString()));

                    l.notifySubText.setText(d.getNotifySubText());
                    MinTextWatcher.after(l.notifySubText, () -> d.setNotifySubText(l.notifySubText.getText().toString()));

                    l.test.setOnClickListener(v2132321 -> {
                        d.sendNotify(App.get(activity).getItemNotificationHandler());
                    });
                    l.time.setText(activity.getString(R.string.dialog_itemNotification_time, TimeUtil.convertToHumanTime(d.getTime(), ConvertMode.HHMM)));
                    l.time.setOnClickListener(_ignore -> new TimePickerDialog(activity, (view, hourOfDay, minute) -> {
                        d.setTime((hourOfDay * 60 * 60) + (minute * 60));
                        d.setLatestDayOfYear(0);
                        item.save();
                        l.time.setText(activity.getString(R.string.dialog_itemNotification_time, TimeUtil.convertToHumanTime(d.getTime(), ConvertMode.HHMM)));
                    }, TimeUtil.getHumanValue(d.getTime(), HumanTimeType.HOUR), TimeUtil.getHumanValue(d.getTime(), HumanTimeType.MINUTE_OF_HOUR), true).show());


                    l.fullScreen.setOnCheckedChangeListener((compoundButton, b1) -> {
                        l.previewViewOnly.setEnabled(b1);
                        l.sound.setEnabled(b1);
                    });
                    l.fullScreen.setOnClickListener(i____ -> d.setFullScreen(l.fullScreen.isChecked()));
                    l.fullScreen.setChecked(d.isFullScreen());
                    l.previewViewOnly.setEnabled(d.isFullScreen());
                    l.sound.setEnabled(d.isFullScreen());

                    l.previewViewOnly.setChecked(d.isPreRenderPreviewMode());
                    l.previewViewOnly.setOnClickListener(i___ -> d.setPreRenderPreviewMode(l.previewViewOnly.isChecked()));

                    l.sound.setChecked(d.isSound());
                    l.sound.setOnClickListener(i____ -> d.setSound(l.sound.isChecked()));

                    new AlertDialog.Builder(activity)
                            .setView(l.getRoot())
                            .setPositiveButton(R.string.dialog_itemNotification_apply, (_refre, _werwer) -> {
                                if (d.getTime() > TimeUtil.getDaySeconds()) d.setLatestDayOfYear(0);
                                notifyDataSetChanged();
                                item.save();
                            })
                            .setNegativeButton(R.string.dialog_itemNotification_cancel, null)
                            .show();
                });

                return b.getRoot();
            }
        });

        binding.add.setOnClickListener(v -> {
            DayItemNotification dayItemNotification = new DayItemNotification();
            dayItemNotification.setNotificationId(RandomUtil.nextIntPositive());
            dayItemNotification.setLatestDayOfYear(new GregorianCalendar().get(Calendar.DAY_OF_YEAR));
            item.addNotifications(dayItemNotification);
            item.save();
            updateEmptyView(item.getNotifications().length == 0);
            ((BaseAdapter) binding.list.getAdapter()).notifyDataSetChanged();
        });
    }

    private void updateEmptyView(boolean empty) {
        viewVisible(binding.empty, empty, View.GONE);
    }

    public void show() {
        dialog.setContentView(view);
        dialog.show();
    }
}
