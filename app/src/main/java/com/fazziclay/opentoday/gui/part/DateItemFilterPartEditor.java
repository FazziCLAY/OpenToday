package com.fazziclay.opentoday.gui.part;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.filter.DateItemFilter;
import com.fazziclay.opentoday.app.items.item.filter.FitEquip;
import com.fazziclay.opentoday.app.items.item.filter.IntegerValue;
import com.fazziclay.opentoday.databinding.IntegerValueRowBinding;
import com.fazziclay.opentoday.databinding.PartDateItemFilterEditorBinding;
import com.fazziclay.opentoday.gui.interfaces.Destroy;
import com.fazziclay.opentoday.util.MinTextWatcher;
import com.fazziclay.opentoday.util.ResUtil;
import com.fazziclay.opentoday.util.SimpleSpinnerAdapter;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

public class DateItemFilterPartEditor implements Destroy {
    private final int COLOR_FILTER_GROUP_ACTIVE;
    private final int COLOR_FILTER_GROUP_INACTIVE;
    private final Context context;
    private final PartDateItemFilterEditorBinding binding;
    private final Runnable saveSignal;
    private GregorianCalendar calendar;
    private final Handler handler;
    private final List<Runnable> runnableList = new ArrayList<>();


    @Override
    public void destroy() {
        for (Runnable runnable : runnableList) {
            handler.removeCallbacks(runnable);
        }
        runnableList.clear();
    }

    public View getRootView() {
        return binding.getRoot();
    }

    public DateItemFilterPartEditor(Context context, LayoutInflater layoutInflater, DateItemFilter dateItemFilter, Item item, Runnable saveSignal) {
        this.context = context;
        this.binding = PartDateItemFilterEditorBinding.inflate(layoutInflater);
        this.saveSignal = saveSignal;
        this.calendar = new GregorianCalendar();
        this.handler = new Handler(Looper.getMainLooper());

        COLOR_FILTER_GROUP_ACTIVE = ResUtil.getAttrColor(context, R.attr.itemFilterState_true);
        COLOR_FILTER_GROUP_INACTIVE = ResUtil.getAttrColor(context, R.attr.itemFilterState_false);

        binding.description.setText(dateItemFilter.getDescription());
        binding.description.addTextChangedListener(new MinTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                dateItemFilter.setDescription(s.toString());
                saveSignal.run();
            }
        });

        DateFormatSymbols dfs = DateFormatSymbols.getInstance(Locale.getDefault());
        String[] months = dfs.getMonths();
        String[] weekdays = dfs.getWeekdays();

        setupRow(new SetupInterface() {
            @Override
            public IntegerValue get() {
                return dateItemFilter.getYear();
            }

            @Override
            public void set(IntegerValue integerValue) {
                dateItemFilter.setYear(integerValue);
            }

            @Override
            public int getCurrentValue() {
                return calendar.get(Calendar.YEAR);
            }
        }, binding.year, R.string.dialog_editItemFilter_year);
        setupRow(new SetupInterface() {
            @Override
            public IntegerValue get() {
                return dateItemFilter.getMonth();
            }

            @Override
            public void set(IntegerValue integerValue) {
                dateItemFilter.setMonth(integerValue);
            }


            @Override
            public int getCurrentValue() {
                return calendar.get(Calendar.MONTH);
            }
        }, binding.month, R.string.dialog_editItemFilter_month, new SimpleSpinnerAdapter<Integer>(context)
                .add("(0) " + months[0], 0)
                .add("(1) " + months[1], 1)
                .add("(2) " + months[2], 2)
                .add("(3) " + months[3], 3)
                .add("(4) " + months[4], 4)
                .add("(5) " + months[5], 5)
                .add("(6) " + months[6], 6)
                .add("(7) " + months[7], 7)
                .add("(8) " + months[8], 8)
                .add("(9) " + months[9], 9)
                .add("(10) " + months[10], 10)
                .add("(11) " + months[11], 11)

        );
        setupRow(new SetupInterface() {
            @Override
            public IntegerValue get() {
                return dateItemFilter.getDayOfMonth();
            }

            @Override
            public void set(IntegerValue integerValue) {
                dateItemFilter.setDayOfMonth(integerValue);
            }

            @Override
            public int getCurrentValue() {
                return calendar.get(Calendar.DAY_OF_MONTH);
            }
        }, binding.dayOfMonth, R.string.itemFilter_dateAndTime_dayOfMonth);
        setupRow(new SetupInterface() {
            @Override
            public IntegerValue get() {
                return dateItemFilter.getDayOfWeek();
            }

            @Override
            public void set(IntegerValue integerValue) {
                dateItemFilter.setDayOfWeek(integerValue);
            }


            @Override
            public int getCurrentValue() {
                return calendar.get(Calendar.DAY_OF_WEEK);
            }
        }, binding.dayOfWeek, R.string.itemFilter_dateAndTime_dayOfWeek, new SimpleSpinnerAdapter<Integer>(context)
                .add("(1) " + weekdays[1], 1)
                .add("(2) " + weekdays[2], 2)
                .add("(3) " + weekdays[3], 3)
                .add("(4) " + weekdays[4], 4)
                .add("(5) " + weekdays[5], 5)
                .add("(6) " + weekdays[6], 6)
                .add("(7) " + weekdays[7], 7)

        );
        setupRow(new SetupInterface() {
            @Override
            public IntegerValue get() {
                return dateItemFilter.getWeekOfYear();
            }

            @Override
            public void set(IntegerValue integerValue) {
                dateItemFilter.setWeekOfYear(integerValue);
            }

            @Override
            public int getCurrentValue() {
                return calendar.get(Calendar.WEEK_OF_YEAR);
            }
        }, binding.weekOfYear, R.string.dialog_editItemFilter_weekOfYear);
        setupRow(new SetupInterface() {
            @Override
            public IntegerValue get() {
                return dateItemFilter.getDayOfYear();
            }

            @Override
            public void set(IntegerValue integerValue) {
                dateItemFilter.setDayOfYear(integerValue);
            }

            @Override
            public int getCurrentValue() {
                return calendar.get(Calendar.DAY_OF_YEAR);
            }
        }, binding.dayOfYear, R.string.itemFilter_dateAndTime_dayOfYear);
        setupRow(new SetupInterface() {
            @Override
            public IntegerValue get() {
                return dateItemFilter.getHour();
            }

            @Override
            public void set(IntegerValue integerValue) {
                dateItemFilter.setHour(integerValue);
            }

            @Override
            public int getCurrentValue() {
                return calendar.get(Calendar.HOUR_OF_DAY);
            }
        }, binding.hour, R.string.itemFilter_dateAndTime_hour);

        setupRow(new SetupInterface() {
            @Override
            public IntegerValue get() {
                return dateItemFilter.getMinute();
            }

            @Override
            public void set(IntegerValue integerValue) {
                dateItemFilter.setMinute(integerValue);
            }

            @Override
            public int getCurrentValue() {
                return calendar.get(Calendar.MINUTE);
            }
        }, binding.minute, R.string.itemFilter_dateAndTime_minute);

        setupRow(new SetupInterface() {
            @Override
            public IntegerValue get() {
                return dateItemFilter.getSecond();
            }

            @Override
            public void set(IntegerValue integerValue) {
                dateItemFilter.setSecond(integerValue);
            }
            @Override
            public int getCurrentValue() {
                return calendar.get(Calendar.SECOND);
            }
        }, binding.second, R.string.dialog_editItemFilter_second);

        runnableList.add(new Runnable() {
            @Override
            public void run() {
                if (!runnableList.contains(this)) return;

                boolean isFitGlobal = dateItemFilter.isFit(new FitEquip(new GregorianCalendar()));
                binding.currentValueTitle.setBackgroundTintList(ColorStateList.valueOf(isFitGlobal ? COLOR_FILTER_GROUP_ACTIVE : COLOR_FILTER_GROUP_INACTIVE));

                handler.postDelayed(this, 1000 / 4);
            }
        });
        for (Runnable runnable : runnableList) {
            handler.post(runnable);
        }
    }

    private void setupRow(SetupInterface setup, IntegerValueRowBinding binding, int resId) {
        setupRow(setup, binding, resId, null);
    }

    private void setupRow(SetupInterface setup, IntegerValueRowBinding binding, int resId, SimpleSpinnerAdapter<Integer> s) {
        setupRow(setup, binding.currentValue, binding.field, resId, binding.mode, binding.value, binding.valueEnum, binding.shift, binding.invert, s);
    }

    private void setupRow(SetupInterface setup, TextView currentValue, TextView field, int resId, Spinner mode, EditText value, Spinner valueEnum, EditText shift, CheckBox isInvert, SimpleSpinnerAdapter<Integer> enumAdapter) {
        boolean enumMode = enumAdapter != null;
        IntegerValue setupValue = setup.get();
        value.setEnabled(setupValue != null);
        valueEnum.setEnabled(setupValue != null);
        if (enumMode) valueEnum.setAdapter(enumAdapter);
        shift.setEnabled(setupValue != null);
        isInvert.setEnabled(setupValue != null);

        field.setText(resId);

        currentValue.setText(String.valueOf(setup.getCurrentValue()));
        runnableList.add(new Runnable() {
            @Override
            public void run() {
                if (!runnableList.contains(this)) return;
                IntegerValue val = setup.get();
                ColorStateList background = val == null ? null : (val.isFit(setup.getCurrentValue()) ? ColorStateList.valueOf(COLOR_FILTER_GROUP_ACTIVE) : ColorStateList.valueOf(COLOR_FILTER_GROUP_INACTIVE));

                calendar = new GregorianCalendar();
                currentValue.setText(String.valueOf(setup.getCurrentValue()));
                currentValue.setBackgroundTintList(background);
                handler.postDelayed(this, 1000 / 4);
            }
        });

        SimpleSpinnerAdapter<String> simpleSpinnerAdapter = new SimpleSpinnerAdapter<String>(context)
                .add(context.getString(R.string.itemFilter_dateAndTime_disable), "disable")
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
            if (enumMode) valueEnum.setSelection(enumAdapter.getValuePosition(setupValue.getValue()));
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
                    valueEnum.setEnabled(false);
                    shift.setEnabled(false);
                    shift.setText("");
                    isInvert.setEnabled(false);
                    setup.set(null);
                    saveSignal.run();
                    return;
                }

                value.setEnabled(true);
                valueEnum.setEnabled(true);
                shift.setEnabled(true);
                isInvert.setEnabled(true);
                IntegerValue integerValue = setup.get();
                if (integerValue == null) {
                    setup.set(integerValue = new IntegerValue());
                }
                if (enumMode) integerValue.setValue(enumAdapter.getItem(valueEnum.getSelectedItemPosition()));
                integerValue.setMode(selected);
                saveSignal.run();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });


        value.setVisibility(!enumMode ? View.VISIBLE : View.GONE);
        valueEnum.setVisibility(enumMode ? View.VISIBLE : View.GONE);

        if (!enumMode) {
            value.addTextChangedListener(new MinTextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String text = value.getText().toString();
                    int val = 0;
                    try {
                        val = Integer.parseInt(text);
                    } catch (Exception ignored) {
                    }

                    IntegerValue integerValue = setup.get();
                    if (integerValue != null) {
                        integerValue.setValue(val);
                        saveSignal.run();
                    }
                }
            });
        } else {
            valueEnum.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    int val = enumAdapter.getItem(position);
                    IntegerValue integerValue = setup.get();
                    if (integerValue != null) {
                        integerValue.setValue(val);
                        saveSignal.run();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        }

        shift.addTextChangedListener(new MinTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = shift.getText().toString();
                int val = 0;
                try {
                    val = Integer.parseInt(text);
                } catch (Exception ignored) {}

                IntegerValue integerValue = setup.get();
                if (integerValue != null) {
                    integerValue.setShift(val);
                    saveSignal.run();
                }
            }
        });

        isInvert.setOnClickListener(v -> {
            boolean b = isInvert.isChecked();
            IntegerValue integerValue = setup.get();
            if (integerValue != null) {
                integerValue.setInvert(b);
                saveSignal.run();
            }
        });
    }

    private interface SetupInterface {
        IntegerValue get();
        void set(IntegerValue integerValue);
        int getCurrentValue();
    }

}
