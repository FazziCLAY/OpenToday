package com.fazziclay.opentoday.gui.activity;

import static com.fazziclay.opentoday.util.InlineUtil.viewClick;
import static com.fazziclay.opentoday.util.InlineUtil.viewVisible;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.fazziclay.javaneoutil.FileUtil;
import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.settings.SettingsManager;
import com.fazziclay.opentoday.app.Telemetry;
import com.fazziclay.opentoday.app.UpdateChecker;
import com.fazziclay.opentoday.util.NetworkUtil;

import java.io.File;
import java.util.UUID;

public class CrashReportActivity extends Activity {
    private static final String EXTRA_PATH = "crash_report_activity_path";
    private static final String EXTRA_ID = "crash_report_activity_crashId";
    private static final String EXTRA_THROWABLE = "crash_report_activity_crashThrowable";

    public static Intent createLaunchIntent(Context context, String path, UUID id, String throwable) {
        return new Intent(context, CrashReportActivity.class)
                .putExtra(EXTRA_PATH, path)
                .putExtra(EXTRA_ID, id.toString())
                .putExtra(EXTRA_THROWABLE, throwable);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash_report);
        Button sendToDeveloper = findViewById(R.id.sendToDeveloper);
        Button updateAvailable = findViewById(R.id.update_available);
        TextView crashReportText = findViewById(R.id.crashReportText);

        if (sendToDeveloper == null || crashReportText == null) {
            Toast.makeText(this, "App break... (UI)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (getIntent() == null || getIntent().getExtras() == null || !getIntent().getExtras().containsKey(EXTRA_PATH)) {
            Toast.makeText(this, "App break... (Extras)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (updateAvailable != null) {
            try {
                UpdateChecker.check(this, (available, url, name) -> runOnUiThread(() -> {
                    if (available) {
                        updateAvailable.setVisibility(View.VISIBLE);
                        updateAvailable.setOnClickListener(view -> {
                            try {
                                NetworkUtil.openBrowser(this, url);
                            } catch (Exception e) {
                                Toast.makeText(this, "Error: " + e, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }));
            } catch (Exception e) {
                Toast.makeText(this, "Check update exception: " + e, Toast.LENGTH_SHORT).show();
            }
        }

        File file = new File(getIntent().getExtras().getString(EXTRA_PATH));
        String crashString = FileUtil.getText(file);
        UUID crashId = UUID.fromString(getIntent().getExtras().getString(EXTRA_ID));
        String crashThrowable = getIntent().getExtras().getString(EXTRA_THROWABLE);

        crashReportText.setOnLongClickListener(view -> {
            // Do not use ClipboardUtil from crash-report-activity
            ClipboardManager clipboardManager = getSystemService(ClipboardManager.class);
            clipboardManager.setPrimaryClip(ClipData.newPlainText("Crash report", crashString));
            Toast.makeText(this, R.string.abc_coped, Toast.LENGTH_LONG).show();
            return true;
        });

        boolean visibleSendToDeveloper = true;
        try {
            visibleSendToDeveloper = !SettingsManager.IS_TELEMETRY.get(App.get(this).getSettingsManager());
        } catch (Exception ignored) {}

        crashReportText.setText(crashString);
        viewVisible(sendToDeveloper, visibleSendToDeveloper, View.GONE);
        viewClick(sendToDeveloper, () -> new AlertDialog.Builder(this)
                .setTitle(R.string.crash_activity_sendToDeveloper_title)
                .setMessage(R.string.crash_activity_sendToDeveloper_message)
                .setPositiveButton(R.string.crash_activity_sendToDeveloper_agree, (_ignore, _ignore0) -> {
                    try {
                        App app = App.get(this);
                        Telemetry telemetry = app.getTelemetry();
                        boolean firstState = telemetry.isEnabled();
                        telemetry.setEnabled(true);
                        telemetry.send(new Telemetry.CrashReportLPacket(crashId, crashThrowable, crashString));
                        telemetry.setEnabled(firstState);
                    } catch (Exception e) {
                        Toast.makeText(this, "Error: " + e, Toast.LENGTH_LONG).show();
                    }
                    viewVisible(sendToDeveloper, false, View.GONE);
                })
                .setNegativeButton(R.string.abc_cancel, null)
                .show());
    }
}
