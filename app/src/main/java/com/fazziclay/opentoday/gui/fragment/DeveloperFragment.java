package com.fazziclay.opentoday.gui.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.fazziclay.opentoday.Debug;
import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.databinding.FragmentDeveloperBinding;
import com.fazziclay.opentoday.gui.UI;
import com.fazziclay.opentoday.gui.UINotification;
import com.fazziclay.opentoday.gui.activity.MainActivity;
import com.fazziclay.opentoday.util.ColorUtil;

public class DeveloperFragment extends Fragment {

    private Context context;
    private FragmentDeveloperBinding binding;
    private App app;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = requireContext();
        this.app = App.get(context);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDeveloperBinding.inflate(inflater);
        setupView();
        return binding.getRoot();
    }

    private void setupView() {
        binding.plugins.setOnClickListener(_ignore -> showPluginsDialog());
        binding.crash.setOnClickListener(_ignore -> UI.Debug.showCrashWithMessageDialog(context, "DeveloperFragment love you <3"));
        binding.featureFlags.setOnClickListener(_ignore -> UI.Debug.showFeatureFlagsDialog(app, context));
        binding.debugText.setText(ColorUtil.colorize(Debug.getDebugInfoText(), Color.WHITE, Color.BLACK, Typeface.NORMAL));
    }

    private void showPluginsDialog() {
        var view = new EditText(context);
        view.setText(app.getSettingsManager().getPlugins());
        new AlertDialog.Builder(context)
                .setTitle(R.string.fragment_developer_plugins_title)
                .setMessage(R.string.fragment_developer_plugins_message)
                .setView(view)
                .setPositiveButton("Set+Restart", (dialogInterface, i) -> {
                    app.getSettingsManager().setPlugins(view.getText().toString());
                    app.getSettingsManager().save();
                    app.reinitPlugins();
                    var notify = new TextView(context);
                    notify.setText(R.string.fragment_developer_plugins_reloadedNotification);
                    notify.setTextSize(20);
                    notify.setBackgroundColor(Color.DKGRAY);
                    UI.getUIRoot(this).addNotification(new UINotification(notify, 5000));
                    UI.postDelayed(() -> {
                        getActivity().finish();
                        startActivity(new Intent(context, MainActivity.class));
                    }, 1500);
                })
                .setNegativeButton(R.string.abc_cancel, null)
                .show();
    }
}
