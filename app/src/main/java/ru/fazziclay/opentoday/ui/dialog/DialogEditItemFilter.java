package ru.fazziclay.opentoday.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.items.item.FilterGroupItem;
import ru.fazziclay.opentoday.databinding.DialogEditItemFilterBinding;
import ru.fazziclay.opentoday.databinding.DialogEditItemFilterRowBinding;
import ru.fazziclay.opentoday.util.MinTextWatcher;
import ru.fazziclay.opentoday.util.SimpleSpinnerAdapter;

public class DialogEditItemFilter {
    private final Activity activity;
    private final DialogEditItemFilterBinding binding;
    private final Dialog dialog;
    private final Runnable saveSignal;
    private GregorianCalendar calendar;
    private final Handler handler;
    private final List<Runnable> runnableList = new ArrayList<>();


    public DialogEditItemFilter(Activity activity, FilterGroupItem.ItemFilter itemFilter, Runnable saveSignal) {
        this.activity = activity;

        this.binding = DialogEditItemFilterBinding.inflate(activity.getLayoutInflater());
        this.dialog = new Dialog(activity, android.R.style.ThemeOverlay_Material);
        this.saveSignal = saveSignal;
        this.calendar = new GregorianCalendar();
        this.handler = new Handler(Looper.getMainLooper());
        this.dialog.setContentView(binding.getRoot());
        this.dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                for (Runnable runnable : runnableList) {
                    handler.removeCallbacks(runnable);
                }
                runnableList.clear();
            }
        });

        setupRow(new SetupInterface() {
            @Override
            public FilterGroupItem.ItemFilter.IntegerValue get() {
                return itemFilter.getYear();
            }

            @Override
            public void set(FilterGroupItem.ItemFilter.IntegerValue integerValue) {
                itemFilter.setYear(integerValue);
            }

            @Override
            public int getCurrentValue() {
                return calendar.get(Calendar.YEAR);
            }
        }, binding.year, R.string.dialog_editItemFilter_year);
        setupRow(new SetupInterface() {
            @Override
            public FilterGroupItem.ItemFilter.IntegerValue get() {
                return itemFilter.getMonth();
            }

            @Override
            public void set(FilterGroupItem.ItemFilter.IntegerValue integerValue) {
                itemFilter.setMonth(integerValue);
            }


            @Override
            public int getCurrentValue() {
                return calendar.get(Calendar.MONTH);
            }
        }, binding.month, R.string.dialog_editItemFilter_month);
        setupRow(new SetupInterface() {
            @Override
            public FilterGroupItem.ItemFilter.IntegerValue get() {
                return itemFilter.getDayOfMonth();
            }

            @Override
            public void set(FilterGroupItem.ItemFilter.IntegerValue integerValue) {
                itemFilter.setDayOfMonth(integerValue);
            }

            @Override
            public int getCurrentValue() {
                return calendar.get(Calendar.DAY_OF_MONTH);
            }
        }, binding.dayOfMonth, R.string.dialog_editItemFilter_dayOfMonth);
        setupRow(new SetupInterface() {
            @Override
            public FilterGroupItem.ItemFilter.IntegerValue get() {
                return itemFilter.getDayOfWeek();
            }

            @Override
            public void set(FilterGroupItem.ItemFilter.IntegerValue integerValue) {
                itemFilter.setDayOfWeek(integerValue);
            }


            @Override
            public int getCurrentValue() {
                return calendar.get(Calendar.DAY_OF_WEEK);
            }
        }, binding.dayOfWeek, R.string.dialog_editItemFilter_dayOfWeek);
        setupRow(new SetupInterface() {
            @Override
            public FilterGroupItem.ItemFilter.IntegerValue get() {
                return itemFilter.getWeekOfYear();
            }

            @Override
            public void set(FilterGroupItem.ItemFilter.IntegerValue integerValue) {
                itemFilter.setWeekOfYear(integerValue);
            }

            @Override
            public int getCurrentValue() {
                return calendar.get(Calendar.WEEK_OF_YEAR);
            }
        }, binding.weekOfYear, R.string.dialog_editItemFilter_weekOfYear);
        setupRow(new SetupInterface() {
            @Override
            public FilterGroupItem.ItemFilter.IntegerValue get() {
                return itemFilter.getDayOfYear();
            }

            @Override
            public void set(FilterGroupItem.ItemFilter.IntegerValue integerValue) {
                itemFilter.setDayOfYear(integerValue);
            }

            @Override
            public int getCurrentValue() {
                return calendar.get(Calendar.DAY_OF_YEAR);
            }
        }, binding.dayOfYear, R.string.dialog_editItemFilter_dayOfYear);
        setupRow(new SetupInterface() {
            @Override
            public FilterGroupItem.ItemFilter.IntegerValue get() {
                return itemFilter.getHour();
            }

            @Override
            public void set(FilterGroupItem.ItemFilter.IntegerValue integerValue) {
                itemFilter.setHour(integerValue);
            }

            @Override
            public int getCurrentValue() {
                return calendar.get(Calendar.HOUR_OF_DAY);
            }
        }, binding.hour, R.string.dialog_editItemFilter_hour);

        setupRow(new SetupInterface() {
            @Override
            public FilterGroupItem.ItemFilter.IntegerValue get() {
                return itemFilter.getMinute();
            }

            @Override
            public void set(FilterGroupItem.ItemFilter.IntegerValue integerValue) {
                itemFilter.setMinute(integerValue);
            }

            @Override
            public int getCurrentValue() {
                return calendar.get(Calendar.MINUTE);
            }
        }, binding.minute, R.string.dialog_editItemFilter_minute);

        setupRow(new SetupInterface() {
            @Override
            public FilterGroupItem.ItemFilter.IntegerValue get() {
                return itemFilter.getSecond();
            }

            @Override
            public void set(FilterGroupItem.ItemFilter.IntegerValue integerValue) {
                itemFilter.setSecond(integerValue);
            }
            @Override
            public int getCurrentValue() {
                return calendar.get(Calendar.SECOND);
            }
        }, binding.second, R.string.dialog_editItemFilter_second);

        for (Runnable runnable : runnableList) {
            handler.post(runnable);
        }
    }

    private void setupRow(SetupInterface setup, DialogEditItemFilterRowBinding binding, int resId) {
        setupRow(setup, binding.currentValue, binding.field, resId, binding.mode, binding.value, binding.shift, binding.invert);
    }

    private void setupRow(SetupInterface setup, TextView currentValue, TextView field, int resId, Spinner mode, EditText value, EditText shift, CheckBox isInvert) {
        FilterGroupItem.ItemFilter.IntegerValue setupValue = setup.get();
        value.setEnabled(setupValue != null);
        shift.setEnabled(setupValue != null);
        isInvert.setEnabled(setupValue != null);

        field.setText(resId);

        currentValue.setText(String.valueOf(setup.getCurrentValue()));
        runnableList.add(new Runnable() {
            @Override
            public void run() {
                if (!runnableList.contains(this)) return;
                calendar = new GregorianCalendar();
                currentValue.setText(String.valueOf(setup.getCurrentValue()));
                handler.postDelayed(this, 1000);
            }
        });

        SimpleSpinnerAdapter<String> simpleSpinnerAdapter = new SimpleSpinnerAdapter<String>(activity)
                .add("Disable", "disable")
                .add("==", "==")
                .add(">", ">")
                .add(">=", ">=")
                .add("<", "<")
                .add("<=", "<=")
                .add("%", "%");
        mode.setAdapter(simpleSpinnerAdapter);

        if (setupValue == null) {
            mode.setSelection(simpleSpinnerAdapter.getValuePosition("disable"));
        } else {
            if (setupValue.getMode() == null) {
                mode.setSelection(simpleSpinnerAdapter.getValuePosition("=="));
            } else {
                mode.setSelection(simpleSpinnerAdapter.getValuePosition(setupValue.getMode()));
            }
            value.setText(String.valueOf(setupValue.getValue()));
            shift.setText(String.valueOf(setupValue.getShift()));
            isInvert.setChecked(setupValue.isInvert());
        }

        mode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            boolean first = true;
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (first) {
                    first = false;
                    return;
                }
                String selected = simpleSpinnerAdapter.getItem(position);
                if ("disable".equals(selected)) {
                    value.setEnabled(false);
                    value.setText("");
                    shift.setEnabled(false);
                    shift.setText("");
                    isInvert.setEnabled(false);
                    setup.set(null);
                    saveSignal.run();
                    return;
                }

                value.setEnabled(true);
                shift.setEnabled(true);
                isInvert.setEnabled(true);
                FilterGroupItem.ItemFilter.IntegerValue integerValue = setup.get();
                if (integerValue == null) {
                    setup.set(integerValue = new FilterGroupItem.ItemFilter.IntegerValue());
                }
                integerValue.setMode(selected);
                saveSignal.run();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        value.addTextChangedListener(new MinTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = value.getText().toString();
                int val = 0;
                try {
                    val = Integer.parseInt(text);
                } catch (Exception ignored) {}

                FilterGroupItem.ItemFilter.IntegerValue integerValue = setup.get();
                if (integerValue != null) {
                    integerValue.setValue(val);
                    saveSignal.run();
                }
            }
        });

        shift.addTextChangedListener(new MinTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = shift.getText().toString();
                int val = 0;
                try {
                    val = Integer.parseInt(text);
                } catch (Exception ignored) {}

                FilterGroupItem.ItemFilter.IntegerValue integerValue = setup.get();
                if (integerValue != null) {
                    integerValue.setShift(val);
                    saveSignal.run();
                }
            }
        });

        isInvert.setOnClickListener(v -> {
            boolean b = isInvert.isChecked();
            FilterGroupItem.ItemFilter.IntegerValue integerValue = setup.get();
            if (integerValue != null) {
                integerValue.setInvert(b);
                saveSignal.run();
            }
        });
    }

    private interface SetupInterface {
        FilterGroupItem.ItemFilter.IntegerValue get();
        void set(FilterGroupItem.ItemFilter.IntegerValue integerValue);
        int getCurrentValue();
    }


    public void show() {
        dialog.show();
    }
}
