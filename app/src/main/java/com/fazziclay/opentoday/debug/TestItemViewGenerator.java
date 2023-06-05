package com.fazziclay.opentoday.debug;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.fazziclay.opentoday.app.SettingsManager;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.item.CycleListItem;
import com.fazziclay.opentoday.app.items.item.FilterGroupItem;
import com.fazziclay.opentoday.app.items.item.GroupItem;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.TextItem;
import com.fazziclay.opentoday.app.items.selection.SelectionManager;
import com.fazziclay.opentoday.gui.interfaces.ItemInterface;
import com.fazziclay.opentoday.gui.interfaces.StorageEditsActions;
import com.fazziclay.opentoday.gui.item.ItemViewGenerator;
import com.fazziclay.opentoday.gui.item.ItemViewGeneratorBehavior;
import com.fazziclay.opentoday.gui.item.ItemsStorageDrawer;
import com.fazziclay.opentoday.gui.item.ItemsStorageDrawerGenerator;

import java.io.File;

public class TestItemViewGenerator extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SelectionManager selectionManager = new SelectionManager();
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

        ItemViewGenerator itemViewGenerator = ItemViewGenerator.builder(this, new ItemViewGeneratorBehavior() {
                    @Override
                    public boolean isMinimizeGrayColor() {
                        return false;
                    }

                    @Override
                    public SettingsManager.ItemAction getItemOnClickAction() {
                        return null;
                    }

                    @Override
                    public boolean isScrollToAddedItem() {
                        return false;
                    }

                    @Override
                    public SettingsManager.ItemAction getItemOnLeftAction() {
                        return null;
                    }

                    @Override
                    public boolean isConfirmFastChanges() {
                        return false;
                    }

                    @Override
                    public void setConfirmFastChanges(boolean b) {

                    }

                    @Override
                    public Drawable getForeground(Item item) {
                        return null;
                    }
                }, new ItemsStorageDrawerGenerator() {
                    @Override
                    public ItemsStorageDrawer generate(ItemsStorage itemsStorage) {
                        return null;
                    }
                })
                .setOnItemClick(itemClick)
                .setStorageEditsActions(edits)
                .build();


        CycleListItem item = new CycleListItem("Simple CycleList");
        item.setViewBackgroundColor(Color.RED);
        item.setViewCustomBackgroundColor(true);
        item.addItem(new TextItem("123"));
        item.addItem(new TextItem("1232134r34"));


        CycleListItem root = new CycleListItem("1232134r34");
        root.addItem(TextItem.createEmpty());
        item.addItem(root);


        item.addItem(new GroupItem("Group"));

        View view = itemViewGenerator.generate(item, null);


        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(view);
        setContentView(linearLayout);
    }
}
