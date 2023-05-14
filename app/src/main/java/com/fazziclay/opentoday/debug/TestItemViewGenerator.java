package com.fazziclay.opentoday.debug;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.fazziclay.opentoday.app.items.ItemManager;
import com.fazziclay.opentoday.app.items.item.CycleListItem;
import com.fazziclay.opentoday.app.items.item.FilterGroupItem;
import com.fazziclay.opentoday.app.items.item.GroupItem;
import com.fazziclay.opentoday.app.items.item.TextItem;
import com.fazziclay.opentoday.app.SettingsManager;
import com.fazziclay.opentoday.gui.interfaces.ItemInterface;
import com.fazziclay.opentoday.gui.interfaces.StorageEditsActions;
import com.fazziclay.opentoday.gui.item.ItemViewGenerator;

import java.io.File;

public class TestItemViewGenerator extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ItemManager itemManager = new ItemManager(new File(getExternalCacheDir(), "/tests/testItemViewGenerator.json"), new File(getExternalCacheDir(), "/tests/testItemViewGenerator.gz"));
        SettingsManager settingsManager = new SettingsManager(new File(getExternalCacheDir(), "/tests/settings.json"));
        ItemInterface itemClick = item -> Toast.makeText(TestItemViewGenerator.this, "Click: " + item.toString(), Toast.LENGTH_SHORT).show();
        ItemInterface itemEditor = item -> Toast.makeText(TestItemViewGenerator.this, "Editor: " + item.toString(), Toast.LENGTH_SHORT).show();
        StorageEditsActions edits = new StorageEditsActions() {
            @Override
            public void onGroupEdit(GroupItem groupItem) {
                Toast.makeText(TestItemViewGenerator.this, "Edit: Group: " + groupItem.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCycleListEdit(CycleListItem cycleListItem) {
                Toast.makeText(TestItemViewGenerator.this, "Edit: CycleList: " + cycleListItem.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFilterGroupEdit(FilterGroupItem filterGroupItem) {
                Toast.makeText(TestItemViewGenerator.this, "Edit: FilterGroup: " + filterGroupItem.toString(), Toast.LENGTH_SHORT).show();
            }
        };

        ItemViewGenerator itemViewGenerator = ItemViewGenerator.builder(this, itemManager, settingsManager, itemManager.getSelectionManager())
                .setOnItemClick(itemClick)
                .setStorageEditsActions(edits)
                .setOnItemOpenEditor(itemEditor)
                .build();

        CycleListItem r = new CycleListItem("1232134r34");
        r.addItem(TextItem.createEmpty());
        CycleListItem item = new CycleListItem("Simple CycleList");
        item.addItem(new TextItem("123"));
        item.addItem(new TextItem("1232134r34"));
        item.addItem(r);
        item.addItem(new GroupItem("Group"));

        View view = itemViewGenerator.generate(item, null);


        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(view);
        setContentView(linearLayout);
    }
}
