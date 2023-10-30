package com.fazziclay.opentoday.gui.fragment.settings;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.ColorHistoryManager;
import com.fazziclay.opentoday.app.settings.ColorOption;
import com.fazziclay.opentoday.app.settings.SettingsManager;
import com.fazziclay.opentoday.databinding.FragmentSettingsAnalogClockBinding;
import com.fazziclay.opentoday.gui.ActivitySettings;
import com.fazziclay.opentoday.gui.ColorPicker;
import com.fazziclay.opentoday.gui.UI;
import com.fazziclay.opentoday.gui.interfaces.ActivitySettingsMember;

public class AnalogClockSettingsFragment extends Fragment implements ActivitySettingsMember {
    private FragmentSettingsAnalogClockBinding binding;
    private Context context;
    private App app;
    private SettingsManager sm;
    private ColorHistoryManager colorHistoryManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = requireContext();
        this.app = App.get(context);
        this.sm = app.getSettingsManager();
        this.colorHistoryManager = app.getColorHistoryManager();

        if (savedInstanceState == null) {
            setupActivitySettings();
        }
    }

    private void setupActivitySettings() {
        UI.getUIRoot(this).pushActivitySettings(a -> {
            a.setClockVisible(true);
            a.analogClockForceVisible(true);
            a.setToolbarSettings(ActivitySettings.ToolbarSettings.createBack(R.string.settings_analogClock_title, () -> UI.rootBack(this)));
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
        setupButtonHand(binding.secondHandColor, R.string.fragment_settings_analogClockSettings_handColor_second, SettingsManager.ANALOG_CLOCK_COLOR_SECONDS);
        setupButtonHand(binding.minuteHandColor, R.string.fragment_settings_analogClockSettings_handColor_minute, SettingsManager.ANALOG_CLOCK_COLOR_MINUTE);
        setupButtonHand(binding.hourHandColor, R.string.fragment_settings_analogClockSettings_handColor_hour, SettingsManager.ANALOG_CLOCK_COLOR_HOUR);
    }

    public void setupButtonHand(View view, @StringRes int title, ColorOption option) {
        view.setOnClickListener(v -> new ColorPicker(requireContext(), option.get(sm))
                .setColorHistoryManager(colorHistoryManager)
                .setting(true, true, true)
                .setNeutralDialogButton(R.string.fragment_settings_analogClockSettings_handColor_reset, () -> {
                    option.def(sm);
                    updateButtonsTint();
                    sm.save();
                })
                .showDialog(title, R.string.abc_cancel, R.string.abc_ok, color -> {
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
}