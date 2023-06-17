package com.fazziclay.opentoday.debug;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

public class TestItemsStorageDrawer extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*
        TabsManager tabsManager = new TabsManager(new File(getExternalCacheDir(), "/tests/testItemViewGenerator.json"), new File(getExternalCacheDir(), "/tests/testItemViewGenerator.gz"), new Translation() {
            @Override
            public String get(Object key, Object... args) {
                return null;
            }
        });
        ItemInterface onClick = item -> Toast.makeText(this, "item = " + item.toString(), Toast.LENGTH_SHORT).show();
        boolean previewMode = false;

        ItemsStorageDrawer itemsStorageDrawer = new ItemsStorageDrawer(this,
                new ItemViewGeneratorBehavior() {
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
                },
                new SelectionManager(),
                tabsManager.getTabById(new UUID(0, 0)),
                onClick,
                item -> Toast.makeText(TestItemsStorageDrawer.this, "unsupported", Toast.LENGTH_SHORT).show(),
                null,
                previewMode);


        Button add = new Button(this);
        View view = itemsStorageDrawer.getView();


        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(add);
        linearLayout.addView(view);
        setContentView(linearLayout);

        itemsStorageDrawer.create();

        add.setOnClickListener(v -> tabsManager.getTabById(new UUID(0, 0)).addItem(new CycleListItem("123132231213")));
        */

    }
}
