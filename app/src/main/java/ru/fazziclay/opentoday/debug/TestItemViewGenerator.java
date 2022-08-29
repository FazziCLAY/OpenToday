package ru.fazziclay.opentoday.debug;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;

import ru.fazziclay.opentoday.app.items.ItemManager;
import ru.fazziclay.opentoday.app.items.item.Item;
import ru.fazziclay.opentoday.app.items.item.TextItem;
import ru.fazziclay.opentoday.ui.other.item.ItemViewGenerator;

public class TestItemViewGenerator extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ItemViewGenerator itemViewGenerator = new ItemViewGenerator(
                this,
                new ItemManager(new File(getExternalCacheDir(), "/tests/testItemViewGenerator.json")),
                "path",
                item -> Toast.makeText(TestItemViewGenerator.this, "item = " + item.toString(), Toast.LENGTH_SHORT).show(),
                false);
        Item item = new TextItem("Text item");

        View view = itemViewGenerator.generate(item, null);


        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(view);
        setContentView(linearLayout);
    }
}
