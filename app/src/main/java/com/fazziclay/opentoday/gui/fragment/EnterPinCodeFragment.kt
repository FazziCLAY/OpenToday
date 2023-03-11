package com.fazziclay.opentoday.gui.fragment;

import static com.fazziclay.opentoday.util.InlineUtil.viewClick;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.databinding.FragmentEnterPincodeBinding;
import com.fazziclay.opentoday.gui.UI;

public class EnterPinCodeFragment extends Fragment {
    private FragmentEnterPincodeBinding binding;
    private App app;
    private boolean isAllowed = false;
    private int tryNumber = 0;
    private String currentPin = "";
    private boolean isKeyboardLock = false;

    public static EnterPinCodeFragment create() {
        return new EnterPinCodeFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.app = App.get(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentEnterPincodeBinding.inflate(getLayoutInflater());
        setupKeyboardEmulator();
        return binding.getRoot();
    }

    private void allow() {
        isAllowed = true;
        UI.findFragmentInParents(this, MainRootFragment.class).navigate(ItemsTabIncludeFragment.create(), false);
    }

    private void setupKeyboardEmulator() {
        viewClick(binding.number0, () -> onNumberPress((byte) 0));
        viewClick(binding.number1, () -> onNumberPress((byte) 1));
        viewClick(binding.number2, () -> onNumberPress((byte) 2));
        viewClick(binding.number3, () -> onNumberPress((byte) 3));
        viewClick(binding.number4, () -> onNumberPress((byte) 4));
        viewClick(binding.number5, () -> onNumberPress((byte) 5));
        viewClick(binding.number6, () -> onNumberPress((byte) 6));
        viewClick(binding.number7, () -> onNumberPress((byte) 7));
        viewClick(binding.number8, () -> onNumberPress((byte) 8));
        viewClick(binding.number9, () -> onNumberPress((byte) 9));
    }

    private void onNumberPress(byte n) {
        if (isKeyboardLock) return;
        currentPin = currentPin + n;
        onPinChanged();
    }

    private void onPinChanged() {
        String s = currentPin;

        binding.pin.setText(s);
        if (s.length() == app.getPinCodeLength()) {
            if (app.isPinCodeAllow(s)) {
                binding.pin.setTextColor(Color.GREEN);
                allow();
            } else {
                tryNumber++;
                if (tryNumber >= 5) {
                    isKeyboardLock = true;
                    binding.pin.setVisibility(View.GONE);
                    binding.timeout.setVisibility(View.VISIBLE);
                }
                currentPin = "";
                onPinChanged();
            }
        }
    }
}
