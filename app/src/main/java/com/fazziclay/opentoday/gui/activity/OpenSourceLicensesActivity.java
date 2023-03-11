package com.fazziclay.opentoday.gui.activity;

import static com.fazziclay.opentoday.util.InlineUtil.viewClick;

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

import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.util.License;

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
        setTitle(getString(R.string.openSourceLicenses_title));
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

        // Description
        if (license.getDescription() != null) {
            TextView descriptionView = new TextView(context);
            descriptionView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            descriptionView.setText(license.getDescription());
            Linkify.addLinks(descriptionView, Linkify.ALL);
            linearLayout.addView(descriptionView);
        }

        viewClick(linearLayout, () -> context.startActivity(OpenSourceLicenseActivity.createLaunchIntent(context, license.getAssetPath(), license.getTitle())));
        return linearLayout;
    }
}