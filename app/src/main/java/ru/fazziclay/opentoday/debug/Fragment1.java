package ru.fazziclay.opentoday.debug;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import ru.fazziclay.opentoday.R;

public class Fragment1 extends BaseFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            getChildFragmentManager()
                    .beginTransaction()
                    .replace(R.id.opentoday, new Fragment2())
                    .commit();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Context c = requireActivity();

        LinearLayout linearLayout = new LinearLayout(c);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        TextView t = new TextView(c);
        t.setText("This is Fragment1");
        linearLayout.addView(t);

        Button b = new Button(c);
        b.setText("replace");
        b.setOnClickListener(v -> {
            getChildFragmentManager()
                    .beginTransaction()
                    .addToBackStack(null)
                    .replace(R.id.opentoday, new Fragment3())
                    .commit();
        });
        linearLayout.addView(b);

        FrameLayout frameLayout = new FrameLayout(c);
        frameLayout.setId(R.id.opentoday);
        linearLayout.addView(frameLayout);

        return linearLayout;
    }
}
