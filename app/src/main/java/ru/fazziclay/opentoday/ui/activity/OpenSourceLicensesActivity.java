package ru.fazziclay.opentoday.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.app.License;

public class OpenSourceLicensesActivity extends AppCompatActivity {
    private License[] licenses;

    public static Intent createLaunchIntent(Context context) {
        return new Intent(context, OpenSourceLicensesActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        licenses = App.get(this).getOpenSourcesLicenses();
        for (License license : licenses) {
            linearLayout.addView(toView(this, license));
        }

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(linearLayout);
        setContentView(scrollView);
        setTitle(getString(R.string.openSouceLicenses_title));
    }

    private View toView(Context context, License license) {
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setPadding(3, 3, 3, 3);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setBackgroundColor(Color.parseColor("#33888888"));
        LinearLayout.LayoutParams l = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        l.setMargins(10, 10, 10, 10);
        linearLayout.setLayoutParams(l);

        // Title
        TextView title = new TextView(context);
        title.setPadding(0, 10, 0, 10);
        title.setTextSize(20);
        title.setText(license.getTitle());
        linearLayout.addView(title);

        // URL
        if (license.getUrl() != null) {
            TextView url = new TextView(context);
            url.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            url.setText(license.getUrl());
            Linkify.addLinks(url, Linkify.ALL);
            linearLayout.addView(url);
        }

        linearLayout.setOnClickListener(v -> context.startActivity(OpenSourceLicenseActivity.createLaunchIntent(context, license.getAssetPath(), license.getTitle())));
        return linearLayout;
    }
}