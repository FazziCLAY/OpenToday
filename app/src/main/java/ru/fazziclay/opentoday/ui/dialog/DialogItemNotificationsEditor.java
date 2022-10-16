package ru.fazziclay.opentoday.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.app.items.notification.DayItemNotification;
import ru.fazziclay.opentoday.app.items.notification.ItemNotification;
import ru.fazziclay.opentoday.databinding.DialogItemNotificationBinding;
import ru.fazziclay.opentoday.databinding.DialogItemNotificationsEditorBinding;
import ru.fazziclay.opentoday.databinding.ItemNotificationBinding;
import ru.fazziclay.opentoday.util.MinBaseAdapter;
import ru.fazziclay.opentoday.util.time.ConvertMode;
import ru.fazziclay.opentoday.util.time.HumanTimeType;
import ru.fazziclay.opentoday.util.time.TimeUtil;

public class DialogItemNotificationsEditor {
    private final Activity activity;
    private final Item item;
    private final Runnable onApply;
    private final Dialog dialog;
    private final DialogItemNotificationsEditorBinding binding;
    private final View view;

    public DialogItemNotificationsEditor(Activity activity, Item item, Runnable o) {

        this.activity = activity;
        this.item = item;
        this.onApply = o;

        dialog = new Dialog(activity);
        dialog.setOnCancelListener(dialog -> onApply.run());

        binding = DialogItemNotificationsEditorBinding.inflate(activity.getLayoutInflater());
        view = binding.getRoot();

        binding.list.setAdapter(new MinBaseAdapter() {
            @Override
            public int getCount() {
                return item.getNotifications().size();
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                ItemNotification itemNotification = item.getNotifications().get(position);

                ItemNotificationBinding b = ItemNotificationBinding.inflate(activity.getLayoutInflater(), parent, false);

                if (itemNotification instanceof DayItemNotification) {
                    DayItemNotification d = (DayItemNotification) itemNotification;
                    b.text.setText(String.format("#%s - %s - %s", d.getNotificationId(), activity.getString(R.string.itemNotification_day), TimeUtil.convertToHumanTime(d.getTime(), ConvertMode.HHMM)));
                }

                b.delete.setOnClickListener(v -> {
                    new AlertDialog.Builder(activity)
                            .setTitle(R.string.dialogItem_delete_title)
                            .setPositiveButton(R.string.dialogItem_delete_apply, (ee, eee) -> {
                                item.getNotifications().remove(itemNotification);
                                item.save();
                                notifyDataSetChanged();
                            })
                            .setNegativeButton(R.string.dialogItem_delete_cancel, null)
                            .show();
                });


                b.getRoot().setOnClickListener(v -> {
                    DialogItemNotificationBinding l = DialogItemNotificationBinding.inflate(activity.getLayoutInflater());

                    DayItemNotification d = (DayItemNotification) itemNotification;

                    l.notificationId.setText(String.valueOf(d.getNotificationId()));
                    l.text.setText(d.getNotifyText());
                    l.textFromItem.setChecked(d.isNotifyTextFromItemText());
                    l.textFromItem.setOnClickListener(vvv -> {
                        l.text.setEnabled(!l.textFromItem.isChecked());
                    });
                    l.title.setText(d.getNotifyTitle());
                    l.titleFromItem.setChecked(d.isNotifyTitleFromItemText());
                    l.titleFromItem.setOnClickListener(vvv -> {
                        l.title.setEnabled(!l.titleFromItem.isChecked());
                    });
                    l.notifySubText.setText(d.getNotifySubText());
                    l.test.setOnClickListener(v2132321 -> {

                    });
                    l.time.setText(activity.getString(R.string.dialog_itemNotification_time, TimeUtil.convertToHumanTime(d.getTime(), ConvertMode.HHMM)));
                    l.time.setOnClickListener(v421213 -> new TimePickerDialog(activity, (view, hourOfDay, minute) -> {
                        d.setTime((hourOfDay * 60 * 60) + (minute * 60));
                        d.setLatestDayOfYear(0);
                        item.save();
                        l.time.setText(activity.getString(R.string.dialog_itemNotification_time, TimeUtil.convertToHumanTime(d.getTime(), ConvertMode.HHMM)));
                    }, TimeUtil.getHumanValue(d.getTime(), HumanTimeType.HOUR), TimeUtil.getHumanValue(d.getTime(), HumanTimeType.MINUTE_OF_HOUR), true).show());

                    new AlertDialog.Builder(activity)
                            .setView(l.getRoot())
                            .setPositiveButton(R.string.dialog_itemNotification_apply, (refre, werwer) -> {
                                d.setNotifyTitle(l.title.getText().toString());
                                d.setNotifyText(l.text.getText().toString());
                                d.setNotifySubText(l.notifySubText.getText().toString());
                                d.setNotifyTitleFromItemText(l.titleFromItem.isChecked());
                                d.setNotifyTextFromItemText(l.textFromItem.isChecked());
                                try {
                                    int i = Integer.parseInt(l.notificationId.getText().toString());
                                    d.setNotificationId(i);
                                } catch (Exception e) {
                                    Toast.makeText(activity, R.string.dialog_itemNotification_incorrectNotificationId, Toast.LENGTH_SHORT).show();
                                }
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
            item.getNotifications().add(new DayItemNotification());
            item.save();
            ((BaseAdapter) binding.list.getAdapter()).notifyDataSetChanged();
        });
    }

    public void show() {
        dialog.setContentView(view);
        dialog.show();
    }
}
