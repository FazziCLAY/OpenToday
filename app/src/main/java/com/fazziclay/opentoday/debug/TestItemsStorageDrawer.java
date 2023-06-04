package com.fazziclay.opentoday.debug;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.fazziclay.opentoday.app.SettingsManager;
import com.fazziclay.opentoday.app.Translation;
import com.fazziclay.opentoday.app.items.selection.SelectionManager;
import com.fazziclay.opentoday.app.items.tab.TabsManager;
import com.fazziclay.opentoday.app.items.item.CycleListItem;
import com.fazziclay.opentoday.app.items.item.FilterGroupItem;
import com.fazziclay.opentoday.app.items.item.GroupItem;
import com.fazziclay.opentoday.gui.interfaces.ItemInterface;
import com.fazziclay.opentoday.gui.interfaces.StorageEditsActions;
import com.fazziclay.opentoday.gui.item.ItemsStorageDrawer;

import java.io.File;
import java.util.UUID;

public class TestItemsStorageDrawer extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TabsManager tabsManager = new TabsManager(new File(getExternalCacheDir(), "/tests/testItemViewGenerator.json"), new File(getExternalCacheDir(), "/tests/testItemViewGenerator.gz"), new Translation() {
            @Override
            public String get(Object key, Object... args) {
                return null;
            }
        });
        ItemInterface onClick = item -> Toast.makeText(this, "item = " + item.toString(), Toast.LENGTH_SHORT).show();
        boolean previewMode = false;
        StorageEditsActions edits = new StorageEditsActions() {
            @Override
            public void onGroupEdit(GroupItem groupItem) {
                Toast.makeText(TestItemsStorageDrawer.this, "Edit: Group: " + groupItem.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCycleListEdit(CycleListItem cycleListItem) {
                Toast.makeText(TestItemsStorageDrawer.this, "Edit: CycleList: " + cycleListItem.toString(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFilterGroupEdit(FilterGroupItem filterGroupItem) {
                Toast.makeText(TestItemsStorageDrawer.this, "Edit: FilterGroup: " + filterGroupItem.toString(), Toast.LENGTH_SHORT).show();
            }
        };

        ItemsStorageDrawer itemsStorageDrawer = new ItemsStorageDrawer(this,
                tabsManager,
                new SettingsManager(null),
                new SelectionManager(),
                tabsManager.getTabById(new UUID(0, 0)),
                onClick,
                item -> Toast.makeText(TestItemsStorageDrawer.this, "unsupported", Toast.LENGTH_SHORT).show(),
                previewMode,
                edits);


        Button add = new Button(this);
        View view = itemsStorageDrawer.getView();


        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(add);
        linearLayout.addView(view);
        setContentView(linearLayout);

        itemsStorageDrawer.create();

        add.setOnClickListener(v -> tabsManager.getTabById(new UUID(0, 0)).addItem(new CycleListItem("123132231213")));
    }
}
