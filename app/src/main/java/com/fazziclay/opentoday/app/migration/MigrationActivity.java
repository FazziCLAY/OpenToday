package com.fazziclay.opentoday.app.migration;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.util.Linkify;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.databinding.MigrationActivityBinding;
import com.fazziclay.opentoday.ui.fragment.SettingsFragment;
import com.fazziclay.opentoday.util.ColorUtil;
import com.fazziclay.opentoday.util.NetworkUtil;

public class MigrationActivity extends AppCompatActivity {
    private MigrationActivityBinding b;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        b = MigrationActivityBinding.inflate(getLayoutInflater());
        setContentView(b.getRoot());

        Migration.is((isTime, m, e) -> runOnUiThread(() -> {
            boolean close = !isTime || m == null || e != null;
            if (close) {
                if (e == null) {
                    Toast.makeText(this, "no time", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Error: " + e, Toast.LENGTH_SHORT).show();
                }
                finish();
                return;
            }

            b.loading.setVisibility(View.GONE);
            b.loadedContent.setVisibility(View.VISIBLE);

            b.firstText.setText(col(m.firstMessage));
            Linkify.addLinks(b.firstText, Linkify.ALL);

            b.downloadTitle.setText(col(m.downloadTitle));
            Linkify.addLinks(b.downloadTitle, Linkify.ALL);

            b.downloadButton.setText(m.downloadButtonText);
            b.downloadButton.setOnClickListener(ignore -> NetworkUtil.openBrowser(this, m.updateApkDirectUrl));

            b.downloadDescription.setText(col(m.downloadDescription));
            Linkify.addLinks(b.downloadDescription, Linkify.ALL);

            b.exportTitle.setText(col(m.exportTitle));
            Linkify.addLinks(b.exportTitle, Linkify.ALL);

            b.exportButton.setText(m.exportButtonText);
            b.exportButton.setOnClickListener(ignore -> SettingsFragment.showExportDialog(this, App.get(this).getSettingsManager(), App.get(this).getColorHistoryManager()));

            b.exportDescription.setText(col(m.exportDescription));
            Linkify.addLinks(b.exportDescription, Linkify.ALL);
        }));
    }

    private SpannableString col(String s) {
        return ColorUtil.colorize(s, Color.WHITE, Color.TRANSPARENT, Typeface.NORMAL);
    }
}
