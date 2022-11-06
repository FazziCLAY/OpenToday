package com.fazziclay.opentoday.debug;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class Fragment3 extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Context c = requireActivity();

        LinearLayout linearLayout = new LinearLayout(c);
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        TextView t = new TextView(c);
        t.setText("This is Fragment3");
        linearLayout.addView(t);

        Button button = new Button(c);
        button.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });
        linearLayout.addView(button);

        return linearLayout;
    }
}
