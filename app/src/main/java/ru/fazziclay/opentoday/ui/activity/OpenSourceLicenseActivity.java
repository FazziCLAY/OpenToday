package ru.fazziclay.opentoday.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import ru.fazziclay.opentoday.R;

public class OpenSourceLicenseActivity extends AppCompatActivity {
    public static Intent createLaunchIntent(Context context, String assetPath, String title) {
        Intent intent = new Intent(context, OpenSourceLicenseActivity.class);
        intent.putExtra("assetPath", assetPath);
        intent.putExtra("title", title);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent() == null || getIntent().getExtras() == null) {
            Toast.makeText(this, R.string.openSourceLicense_cantStart, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        String assetPath = getIntent().getExtras().getString("assetPath");
        if (assetPath == null) {
            Toast.makeText(this, R.string.openSourceLicense_cantStart_assesPath, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String title = getIntent().getExtras().getString("title");
        if (title == null) {
            title = getString(R.string.openSourceLicense_title);
        }

        TextView textView = new TextView(this);
        textView.setPadding(15, 15, 15, 15);

        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(textView);

        HorizontalScrollView horizontalScrollView = new HorizontalScrollView(this);
        horizontalScrollView.addView(scrollView);

        setContentView(horizontalScrollView);
        setTitle(title);

        AssetManager assetManager = getAssets();
        try {
            String result = read(assetManager.open(assetPath));
            textView.setText(result);

        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.openSourceLicense_cantStart_exception, e.toString()), Toast.LENGTH_SHORT).show();
            Log.e("LicenceActivity", "Exception", e);
            finish();
        }
    }

    // IDEA: move to JavaNeoUtil as `readString(InputStream is)`
    private String read(InputStream inputStream) throws IOException {
        Reader reader = new InputStreamReader(inputStream);
        final StringBuilder result = new StringBuilder();

        final char[] buff = new char[1024];
        int i;
        while ((i = reader.read(buff)) > 0) {
            result.append(new String(buff, 0, i));
        }
        reader.close();
        return result.toString();
    }
}