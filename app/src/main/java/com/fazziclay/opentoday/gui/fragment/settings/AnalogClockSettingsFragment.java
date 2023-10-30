package com.fazziclay.opentoday.gui.fragment.settings;

import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.settings.ColorOption;
import com.fazziclay.opentoday.app.settings.SettingsManager;
import com.fazziclay.opentoday.databinding.FragmentSettingsAnalogClockBinding;
import com.fazziclay.opentoday.gui.ActivitySettings;
import com.fazziclay.opentoday.gui.ColorPicker;
import com.fazziclay.opentoday.gui.UI;

public class AnalogClockSettingsFragment extends Fragment {
    private FragmentSettingsAnalogClockBinding binding;
    private SettingsManager sm;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.sm = App.get(requireContext()).getSettingsManager();
        UI.getUIRoot(this).pushActivitySettings(a -> {
            a.setClockVisible(true);
            a.analogClockForceVisible(true);
            a.setToolbarSettings(ActivitySettings.ToolbarSettings.createBack(R.string.settings_analogClock_title, () -> UI.rootBack(AnalogClockSettingsFragment.this)));
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsAnalogClockBinding.inflate(inflater, container, false);
        setupView();
        return binding.getRoot();
    }

    private void setupView() {
        binding.alpha.setProgress(SettingsManager.ANALOG_CLOCK_TRANSPARENCY.get(sm));
        binding.alpha.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                SettingsManager.ANALOG_CLOCK_TRANSPARENCY.set(sm, i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sm.save();
            }
        });


        binding.size.setProgress(SettingsManager.ANALOG_CLOCK_SIZE.get(sm));
        binding.size.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                SettingsManager.ANALOG_CLOCK_SIZE.set(sm, i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                sm.save();
            }
        });
        updateButtonsTint();
        setupButtonHand(binding.secondHandColor, SettingsManager.ANALOG_CLOCK_COLOR_SECONDS);
        setupButtonHand(binding.minuteHandColor, SettingsManager.ANALOG_CLOCK_COLOR_MINUTE);
        setupButtonHand(binding.hourHandColor, SettingsManager.ANALOG_CLOCK_COLOR_HOUR);
    }

    public void setupButtonHand(View view, ColorOption option) {
        view.setOnClickListener(v -> new ColorPicker(requireContext(), option.get(sm))
                .setColorHistoryManager(App.get().getColorHistoryManager())
                .setting(true, true, true)
                .showDialog(option.getSaveKey(), "", "Apply", color -> {
                    option.set(sm, color);
                    updateButtonsTint();
                    sm.save();
                }));
    }

    public void updateButtonsTint() {
        binding.secondHandColor.setBackgroundTintList(ColorStateList.valueOf(SettingsManager.ANALOG_CLOCK_COLOR_SECONDS.get(sm)));
        binding.minuteHandColor.setBackgroundTintList(ColorStateList.valueOf(SettingsManager.ANALOG_CLOCK_COLOR_MINUTE.get(sm)));
        binding.hourHandColor.setBackgroundTintList(ColorStateList.valueOf(SettingsManager.ANALOG_CLOCK_COLOR_HOUR.get(sm)));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UI.getUIRoot(this).popActivitySettings();
    }
}
