package com.fazziclay.opentoday.debug;

import android.app.Activity;
import android.os.Bundle;

import androidx.annotation.Nullable;

public class TestItemViewGenerator extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
/*
        ItemInterface itemClick = item -> Toast.makeText(TestItemViewGenerator.this, "Click: " + item.toString(), Toast.LENGTH_SHORT).show();
        ItemViewGeneratorBehavior behavior = new ItemViewGeneratorBehavior() {
            @Override
            public boolean isConfirmFastChanges() {
                return false;
            }

            @Override
            public void setConfirmFastChanges(boolean b) {

            }

            @Override
            public Drawable getForeground(Item item) {
                return new ColorDrawable(Color.parseColor("#00ff00ff"));
            }

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

        ItemViewGenerator itemViewGenerator = ItemViewGenerator.builder(this, behavior)
                .setPreviewMode(false)
                .setOnItemClick(itemClick)
                .build();


        CycleListItem item = new CycleListItem("Simple CycleList");
        item.setViewBackgroundColor(Color.RED);
        item.setViewCustomBackgroundColor(true);

        item.addItem(new TextItem("123"));
        item.addItem(new TextItem("1232134r34"));


        CycleListItem root = new CycleListItem("cyclelistincyclelist");
        root.addItem(TextItem.createEmpty());
        root.addItem(new CheckboxItem("Checkbox in nearest what??", false));
        item.addItem(root);


        item.addItem(new GroupItem("Empty Group"));

        GroupItem groupItem = new GroupItem("Group with items");
        groupItem.addItem(new MathGameItem());
        groupItem.addItem(new TextItem("Hello"));
        item.addItem(groupItem);

        View view = itemViewGenerator.generate(item, null);


        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(view);
        setContentView(linearLayout);

        ItemsRoot itemsRoot = new ItemsRoot() {
            @Nullable
            @Override
            public Item getItemById(UUID id) {
                return null;
            }

            @Nullable
            @Override
            public Tab getTabById(UUID id) {
                return null;
            }

            @Nullable
            @Override
            public ItemsStorage getItemsStorageById(UUID id) {
                return null;
            }

            @Override
            public boolean isExistById(UUID id) {
                return false;
            }

            @Nullable
            @Override
            public Type getTypeById(UUID id) {
                return null;
            }

            @Nullable
            @Override
            public Object getById(UUID id) {
                return null;
            }

            @Override
            public ItemPath getPathTo(Object o) {
                return null;
            }

            @NonNull
            @Override
            public UUID generateUniqueId() {
                return UUID.randomUUID();
            }

            @NonNull
            @Override
            public Translation getTranslation() {
                return new TranslationImpl((resId, args) -> getString(resId, args));
            }
        };

        SimpleItemsStorage itemsStorage = new SimpleItemsStorage(itemsRoot) {
            @Override
            public void save() {
                Toast.makeText(TestItemViewGenerator.this, "save", Toast.LENGTH_SHORT).show();
            }
        };
        itemsStorage.addItem(item);
        item.tick(new TickSession(this, new GregorianCalendar(), new GregorianCalendar(), 0, false)); */
    }
}
