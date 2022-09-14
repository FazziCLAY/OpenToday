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

import ru.fazziclay.javaneoutil.JavaNeoUtil;
import ru.fazziclay.opentoday.R;

public class OpenSourceLicensesActivity extends AppCompatActivity {
    private final Licence[] licences = new Licence[] {
            new Licence("LICENSE_OpenToday", "OpenToday (this app)", "fazziclay@gmail.com\nhttps://fazziclay.github.io/opentoday"),
            new Licence("LICENSE_JavaNeoUtil", "JavaNeoUtil v" + JavaNeoUtil.VERSION_NAME, "https://github.com/fazziclay/javaneoutil"),
            new Licence("LICENSE_hsv-alpha-color-picker-android", "hsv-alpha-color-picker-android", "https://github.com/martin-stone/hsv-alpha-color-picker-android")
    };

    public static Intent createLaunchIntent(Context context) {
        return new Intent(context, OpenSourceLicensesActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        for (Licence licence : licences) {
            linearLayout.addView(licence.toView(this));
        }
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(linearLayout);
        setContentView(scrollView);
        setTitle(getString(R.string.openSouceLicenses_title));
    }

    private static class Licence {
        public String assetPath;
        public String title;
        public String url;

        public Licence(String assetPath, String title, String url) {
            this.assetPath = assetPath;
            this.title = title;
            this.url = url;
        }

        public View toView(Context context) {
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
            title.setText(this.title);
            linearLayout.addView(title);

            // URL
            if (this.url != null) {
                TextView url = new TextView(context);
                url.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                url.setText(this.url);
                Linkify.addLinks(url, Linkify.ALL);
                linearLayout.addView(url);
            }

            linearLayout.setOnClickListener(v -> context.startActivity(OpenSourceLicenseActivity.createLaunchIntent(context, this.assetPath, this.title)));
            return linearLayout;
        }
    }
}