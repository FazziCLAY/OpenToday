package com.fazziclay.opentoday.debug;

import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

public class TestActivityFragment extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        TextView t = new TextView(this);
        t.setText("This is TestActivityFragment");
        linearLayout.addView(t);

        ViewPager2 viewPager2 = new ViewPager2(this);

        viewPager2.setAdapter(new FragmentStateAdapter(getSupportFragmentManager(), getLifecycle()) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                switch (position) {
                    case 0:
                        return new Fragment1();

                    case 1:
                        return new Fragment2();
                }
                return null;
            }

            @Override
            public int getItemCount() {
                return 2;
            }
        });

        Button tab1 = new Button(this);
        tab1.setOnClickListener(v -> viewPager2.setCurrentItem(0));
        tab1.setText("tab - 1");

        Button tab2 = new Button(this);
        tab2.setOnClickListener(v -> viewPager2.setCurrentItem(1));
        tab2.setText("tab - 2");

        linearLayout.addView(tab1);
        linearLayout.addView(tab2);
        linearLayout.addView(viewPager2);
        setContentView(linearLayout);
    }
}
