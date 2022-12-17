package com.fazziclay.opentoday.gui.activity;

import static com.fazziclay.opentoday.util.InlineUtil.viewClick;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.App;

import java.io.File;

import ru.fazziclay.javaneoutil.FileUtil;

public class CrashReportActivity extends Activity {
    private static final String EXTRA_PATH = "crash_report_activity_path";

    public static Intent createLaunchIntent(Context context, String path) {
        return new Intent(context, CrashReportActivity.class)
                .putExtra(EXTRA_PATH, path);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash_report);
        Button sendToDeveloper = findViewById(R.id.sendToDeveloper);
        TextView crashReportText = findViewById(R.id.crashReportText);

        if (sendToDeveloper == null || crashReportText == null) {
            Toast.makeText(this, "App break... (UI)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (getIntent() == null || getIntent().getExtras() == null || !getIntent().getExtras().containsKey(EXTRA_PATH)) {
            Toast.makeText(this, "App break... (Extras)", Toast.LENGTH_SHORT).show();
            return;
        }

        File file = new File(getIntent().getExtras().getString(EXTRA_PATH));
        String string = FileUtil.getText(file);

        crashReportText.setText(string);
        viewClick(sendToDeveloper, () -> {
            try {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.putExtra(Intent.EXTRA_EMAIL, "fazziclay@gmail.com");
                intent.putExtra(Intent.EXTRA_SUBJECT, "OpenToday crash (" + App.VERSION_CODE + ")");
                intent.putExtra(Intent.EXTRA_TEXT, string);
                intent.setType("message/rfc822");
                startActivity(Intent.createChooser(intent, "OpenToday bug"));
            } catch (Exception e) {
                Toast.makeText(this, "Error: " + e, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
