package ru.fazziclay.opentoday.ui.other;

import android.content.Context;
import android.widget.LinearLayout;

import androidx.recyclerview.widget.RecyclerView;

public class ItemViewHolder extends RecyclerView.ViewHolder {
    public final LinearLayout layout;

    public ItemViewHolder(Context context) {
        super(new LinearLayout(context));
        layout = (LinearLayout) itemView;
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(0, 5, 0, 5);
        layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    }
}
