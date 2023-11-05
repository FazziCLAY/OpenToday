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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.fazziclay.javaneoutil.FileUtil;
import com.fazziclay.opentoday.Debug;
import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.databinding.FragmentDeveloperBinding;
import com.fazziclay.opentoday.gui.ActivitySettings;
import com.fazziclay.opentoday.gui.UI;
import com.fazziclay.opentoday.gui.UINotification;
import com.fazziclay.opentoday.gui.activity.MainActivity;
import com.fazziclay.opentoday.gui.dialog.IconSelectorDialog;
import com.fazziclay.opentoday.gui.interfaces.ActivitySettingsMember;
import com.fazziclay.opentoday.gui.interfaces.NavigationHost;
import com.fazziclay.opentoday.util.ColorUtil;

import java.io.File;

public class DeveloperFragment extends Fragment implements NavigationHost, ActivitySettingsMember {

    private Context context;
    private FragmentDeveloperBinding binding;
    private App app;
    private boolean popBackStackFlag = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.context = requireContext();
        this.app = App.get(context);
        UI.getUIRoot(this).pushActivitySettings(a -> {
            a.setClockVisible(true);
            a.setDateClickCalendar(true);
            a.setNotificationsVisible(true);
            a.setToolbarSettings(ActivitySettings.ToolbarSettings.createBack("OwO!~", () -> {
                Toast.makeText(context, "back in toolbar clicked! (1000ms delay)", Toast.LENGTH_SHORT).show();
                UI.postDelayed(() -> {
                    popBackStackFlag = false;
                    UI.rootBack(this);
                }, 1000);
            }));
        });
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
        binding.icons.setOnClickListener(_ignore -> new IconSelectorDialog(context, icon -> {
            Toast.makeText(context, "icon = " + icon.getId(), Toast.LENGTH_SHORT).show();
        }).show());
        binding.profiler.setOnClickListener((v) -> {
            binding.debugText.setText(ColorUtil.PROFILER.getResult(-1));
        });
        binding.resetUpdateCheckerTimeout.setOnClickListener(fhsfs -> {
            File cacheFile = new File(context.getExternalCacheDir(), "latest_update_check");
            FileUtil.delete(cacheFile);
            Toast.makeText(context, R.string.abc_success, Toast.LENGTH_SHORT).show();
        });
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
                    app.initPlugins();
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

    @Override
    public boolean popBackStack() {
        if (!popBackStackFlag) return false;
        Toast.makeText(context, "Attempt to popBackStack! (1000ms delayed)", Toast.LENGTH_SHORT).show();
        UI.postDelayed(() -> {
            popBackStackFlag = false;
            UI.rootBack(this);
        }, 1000);
        return popBackStackFlag;
    }

    @Override
    public void navigate(@NonNull Fragment fragment, boolean addToBackStack) {
        Toast.makeText(context, "Attempt to navigate in developer fragment lol", Toast.LENGTH_SHORT).show();
    }
}
