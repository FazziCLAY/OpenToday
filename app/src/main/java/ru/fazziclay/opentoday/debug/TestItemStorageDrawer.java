package ru.fazziclay.opentoday.debug;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.io.File;
import java.util.UUID;

import ru.fazziclay.opentoday.app.items.ItemManager;
import ru.fazziclay.opentoday.app.items.item.CycleListItem;
import ru.fazziclay.opentoday.app.items.item.FilterGroupItem;
import ru.fazziclay.opentoday.app.items.item.GroupItem;
import ru.fazziclay.opentoday.app.settings.SettingsManager;
import ru.fazziclay.opentoday.ui.interfaces.IVGEditButtonInterface;
import ru.fazziclay.opentoday.ui.item.ItemStorageDrawer;
import ru.fazziclay.opentoday.ui.interfaces.OnItemClick;

public class TestItemStorageDrawer extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ItemManager itemManager = new ItemManager(new File(getExternalCacheDir(), "/tests/testItemViewGenerator.json"));
        OnItemClick onItemClick = item -> Toast.makeText(this, "item = " + item.toString(), Toast.LENGTH_SHORT).show();
        boolean previewMode = false;
        IVGEditButtonInterface edits = new IVGEditButtonInterface() {
            @Override
            public void onGroupEdit(GroupItem groupItem) {
                Toast.makeText(TestItemStorageDrawer.this, "Edit: Group: " + groupItem.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCycleListEdit(CycleListItem cycleListItem) {
                Toast.makeText(TestItemStorageDrawer.this, "Edit: CycleList: " + cycleListItem.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFilterGroupEdit(FilterGroupItem filterGroupItem) {
                Toast.makeText(TestItemStorageDrawer.this, "Edit: FilterGroup: " + filterGroupItem.toString(), Toast.LENGTH_SHORT).show();
            }
        };

        ItemStorageDrawer itemStorageDrawer = new ItemStorageDrawer(this,
                itemManager,
                new SettingsManager(null),
                itemManager.getTab(new UUID(0, 0)),
                onItemClick,
                previewMode,
                edits);


        Button add = new Button(this);
        View view = itemStorageDrawer.getView();


        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(add);
        linearLayout.addView(view);
        setContentView(linearLayout);

        itemStorageDrawer.create();

        add.setOnClickListener(v -> {
            itemManager.getTab(new UUID(0, 0)).addItem(new CycleListItem("123132231213"));
        });
    }
}
