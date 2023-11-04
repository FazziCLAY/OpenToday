package com.fazziclay.opentoday.plugins.gcp;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.fazziclay.javaneoutil.ArrayUtil;
import com.fazziclay.opentoday.api.Event;
import com.fazziclay.opentoday.api.EventExceptionEvent;
import com.fazziclay.opentoday.api.EventHandler;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.BeautifyColorManager;
import com.fazziclay.opentoday.app.events.gui.CurrentItemsStorageContextChanged;
import com.fazziclay.opentoday.app.events.gui.toolbar.AppToolbarSelectionClickEvent;
import com.fazziclay.opentoday.app.items.ItemsStorage;
import com.fazziclay.opentoday.app.items.item.CheckboxItem;
import com.fazziclay.opentoday.app.items.item.CycleListItem;
import com.fazziclay.opentoday.app.items.item.FilterGroupItem;
import com.fazziclay.opentoday.app.items.item.GroupItem;
import com.fazziclay.opentoday.app.items.item.Item;
import com.fazziclay.opentoday.app.items.item.ItemCodecUtil;
import com.fazziclay.opentoday.app.items.item.ItemType;
import com.fazziclay.opentoday.app.items.item.ItemsRegistry;
import com.fazziclay.opentoday.app.items.item.TextItem;
import com.fazziclay.opentoday.app.items.selection.SelectionManager;
import com.fazziclay.opentoday.app.items.tag.ItemTag;
import com.fazziclay.opentoday.databinding.ToolbarMoreSelectionBinding;
import com.fazziclay.opentoday.gui.ColorPicker;
import com.fazziclay.opentoday.gui.GuiItemsHelper;
import com.fazziclay.opentoday.gui.item.Destroyer;
import com.fazziclay.opentoday.gui.item.ItemViewGenerator;
import com.fazziclay.opentoday.gui.item.ItemViewGeneratorBehavior;
import com.fazziclay.opentoday.gui.item.ItemsStorageDrawerBehavior;
import com.fazziclay.opentoday.util.MinBaseAdapter;
import com.fazziclay.opentoday.util.QuickNote;
import com.fazziclay.opentoday.util.RandomUtil;
import com.fazziclay.opentoday.util.profiler.Profiler;
import com.fazziclay.opentoday.util.time.TimeUtil;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.json.JSONException;

import java.util.Arrays;
import java.util.Comparator;

public class GcpEventHandler extends EventHandler {
    private final GlobalChangesPlugin plugin;
    private final Profiler PROFILER = App.createProfiler("plugin://gcp");

    public GcpEventHandler(GlobalChangesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void handle(Event event) {
        super.handle(event);

        // TODO: 21.10.2023 move another
        if (event instanceof EventExceptionEvent e) {
            Toast.makeText(App.get(), "Exception in plugin : " + e.getException().toString(), Toast.LENGTH_SHORT).show();
        }

        if (event instanceof AppToolbarSelectionClickEvent e) {
            injectSelections(e);
        }
    }

    private void injectSelections(AppToolbarSelectionClickEvent e) {
        ToolbarMoreSelectionBinding localBinding = e.getLocalBinding();
        Context context = localBinding.getRoot().getContext();
        SelectionManager selectionManager = e.getSelectionManager();

        Button massBgColor = new Button(context);
        massBgColor.setText("Mass BG color");
        massBgColor.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        massBgColor.setOnClickListener(i -> new ColorPicker(context, 0xffff00)
                .setting(true, true, true)
                .setColorHistoryManager(App.get(context).getColorHistoryManager())
                .setNeutralDialogButton("Reset ALL", () -> {
                    for (Item item : selectionManager.getItems()) {
                        item.setViewCustomBackgroundColor(false);
                        item.visibleChanged();
                    }
                })
                .showDialog("Mass background", "Cancel", "Apply", color -> {
                    for (Item item : selectionManager.getItems()) {
                        item.setViewBackgroundColor(color);
                        item.setViewCustomBackgroundColor(true);
                        item.visibleChanged();
                    }
                }));







        Button massRandomBgColor = new Button(context);
        massRandomBgColor.setText("Mass random BG");
        massRandomBgColor.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        massRandomBgColor.setOnClickListener(i -> {
            for (Item item : selectionManager.getItems()) {
                item.setViewBackgroundColor(BeautifyColorManager.randomBackgroundColor(context));
                item.setViewCustomBackgroundColor(true);
                item.visibleChanged();
            }
        });

        massRandomBgColor.setOnLongClickListener(i -> {
            for (Item item : selectionManager.getItems()) {
                item.setViewBackgroundColor(RandomUtil.nextInt() | 0xff000000);
                item.setViewCustomBackgroundColor(true);
                item.visibleChanged();
            }
            return true;
        });




        Button selectAll = new Button(context);
        selectAll.setText("Select all");
        selectAll.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));


        selectAll.setOnClickListener(i -> {
            var currentItemsStorage = plugin.getIslp().getCurrentItemsStorage();
            if (currentItemsStorage != null) {
                for (Item item : currentItemsStorage.getAllItems()) {
                    selectionManager.deselectItem(item);
                    selectionManager.selectItem(item);
                }
            }
        });



        Button massTextColor = new Button(context);
        massTextColor.setText("Mass TeXT color");
        massTextColor.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        massTextColor.setOnClickListener(i -> new ColorPicker(context, 0xffff00)
                .setting(true, true, true)
                .setColorHistoryManager(App.get(context).getColorHistoryManager())
                .setNeutralDialogButton("Reset ALL", () -> {
                    for (Item item : selectionManager.getItems()) {
                        if (item instanceof TextItem textItem) {
                            textItem.setCustomTextColor(false);
                            textItem.visibleChanged();
                        }
                    }

                })
                .showDialog("Mass text color", "Cancel", "Apply", color -> {
                    for (Item item : selectionManager.getItems()) {
                        if (item instanceof TextItem textItem) {
                            textItem.setCustomTextColor(true);
                            textItem.setTextColor(color);
                            textItem.visibleChanged();
                        }
                    }
                }));





        Button massRemoveNotification = new Button(context);
        massRemoveNotification.setText("Mass remove notifications");
        massRemoveNotification.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));


        massRemoveNotification.setOnClickListener(i -> {
            var view = new LinearLayout(context);
            view.setOrientation(LinearLayout.VERTICAL);

            ItemViewGenerator itemViewGenerator = new ItemViewGenerator(e.getActivity(), true);

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
                    return null;
                }

                @Override
                public void onGroupEdit(GroupItem groupItem) {

                }

                @Override
                public void onCycleListEdit(CycleListItem cycleListItem) {

                }

                @Override
                public void onFilterGroupEdit(FilterGroupItem filterGroupItem) {

                }

                @Override
                public ItemsStorageDrawerBehavior getItemsStorageDrawerBehavior(Item item) {
                    return null; // null because isRenderMinimized is true
                }

                @Override
                public boolean isRenderMinimized(Item item) {
                    return true;
                }

                @Override
                public boolean isRenderNotificationIndicator(Item item) {
                    return item.isNotifications();
                }
            };
            Destroyer destroyer = new Destroyer();
            for (Item item : selectionManager.getItems()) {
                View iv = itemViewGenerator.generate(item, view, behavior, destroyer, item1 -> {});

                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.HORIZONTAL);

                int size = item.getNotifications().length;
                TextView t = new TextView(context);
                t.setText(String.valueOf(size));
                if (size > 0) {
                    t.setTextColor(Color.RED);
                }
                t.setTextSize(size > 0 ? 27 : 25);
                layout.addView(t);
                layout.addView(iv);
                var p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                p.setMargins(0, 5, 0, 5);
                layout.setLayoutParams(p);
                view.addView(layout);
            }

            var scroll = new ScrollView(context);
            scroll.addView(view);

            new AlertDialog.Builder(context)
                    .setTitle("Preview")
                    .setView(scroll)
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Apply", (dialogInterface, i1) -> {
                        for (Item item : selectionManager.getItems()) {
                            item.removeAllNotifications();
                            item.visibleChanged();
                        }
                    })
                    .show();
        });




        MaterialButton massJSON = new MaterialButton(context);
        massJSON.setText("Mass JSON");
        massJSON.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));


        massJSON.setOnClickListener(i -> {
            var orchard = ItemCodecUtil.exportItemList(selectionManager.getItems());
            try {
                var currentItemsStorage = plugin.getIslp().getCurrentItemsStorage();
                String s = "currItemsStorage=" + currentItemsStorage + "; root=" + plugin.getIslp().getItemsRoot() + "\n\n" + orchard.toJSONArray().toString(2);
                new MaterialAlertDialogBuilder(context)
                        .setTitle("Export")
                        .setMessage(s)
                        .setPositiveButton("Close", null)
                        .show();
            } catch (JSONException ex) {
                Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG).show();
            }
        });

        MaterialButton sortItems = new MaterialButton(context);
        sortItems.setText("Sort items");
        sortItems.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));


        sortItems.setOnClickListener(viewIgnore -> {
            final Comparator<Item>[] comparator = new Comparator[]{Comparator.comparingInt((Item item) -> item.getText().length())};
            final ItemsStorage finalParent = plugin.getIslp().getCurrentItemsStorage();


            var view = new LinearLayout(context);
            view.setOrientation(LinearLayout.VERTICAL);
            view.setPadding(5, 5, 5, 5);

            var reverted = new CheckBox(context);
            reverted.setText("Reversed");

            var currentSelected = new TextView(context);
            currentSelected.setText("Current selected: sort by text length");

            ListView listView = new ListView(context);
            listView.setAdapter(new MinBaseAdapter() {
                @Override
                public int getCount() {
                    return 10;
                }

                @Override
                public View getView(int i, View view, ViewGroup viewGroup) {
                    return switch (i) {
                        case 0:
                            var textLength = new TextView(context);
                            textLength.setText("Text length");
                            textLength.setOnClickListener(fuwbttrth -> {
                                currentSelected.setText("Current selected: sort by text length");
                                comparator[0] = Comparator.comparingInt(item -> item.getText().length());
                            });
                            yield textLength;

                        case 1:
                            var notificationsCount = new TextView(context);
                            notificationsCount.setText("Notifications count");
                            notificationsCount.setOnClickListener(fgdgsdqwr -> {
                                currentSelected.setText("Current selected: sort by notification count");
                                comparator[0] = Comparator.comparingInt(item -> item.getNotifications().length);
                            });
                            yield notificationsCount;

                        case 2:
                            var isChecked = new TextView(context);
                            isChecked.setText("Is checked");
                            isChecked.setOnClickListener((rewrewrw) -> {
                                currentSelected.setText("Current selected: sort by isChecked");
                                comparator[0] = new Comparator<Item>() {
                                    int isChecked(Item item) {
                                        if (item instanceof CheckboxItem checkboxItem) {
                                            return checkboxItem.isChecked() ? 1 : 0;
                                        } else {
                                            return 0;
                                        }
                                    }

                                    @Override
                                    public int compare(Item item, Item t1) {
                                        if (item.getItemType().isInherit(ItemType.CHECKBOX) && !item.getItemType().isInherit(ItemType.CHECKBOX)) {
                                            return 1;
                                        }
                                        return isChecked(item) - isChecked(t1);
                                    }
                                };
                            });
                            yield isChecked;

                        case 3:
                            var tagValue = new TextView(context);
                            tagValue.setText("Tag value");

                            tagValue.setOnClickListener(asfghgfj -> {
                                EditText editText = new EditText(context);
                                new AlertDialog.Builder(context)
                                        .setTitle("Select tag name")
                                        .setView(editText)
                                                .show();
                                currentSelected.setText("Current selected: sort by tag value");
                                comparator[0] = new Comparator<Item>() {
                                    @Override
                                    public int compare(Item item, Item t1) {
                                        ItemTag tag1 = null;
                                        for (ItemTag tag : item.getTags()) {
                                            if (tag.getName().equals(editText.getText().toString())) {
                                                tag1 = tag;
                                                break;
                                            }
                                        }
                                        ItemTag tag2 = null;
                                        for (ItemTag tag : t1.getTags()) {
                                            if (tag.getName().equals(editText.getText().toString())) {
                                                tag2 = tag;
                                                break;
                                            }
                                        }

                                        if (tag1 == null || tag2 == null) {
                                            return tag1 == null ? 0 : -1;
                                        }
                                        if (tag1.getValueType() == ItemTag.ValueType.NUMBER) {
                                            if (tag2.getValueType() == ItemTag.ValueType.NUMBER) {
                                                int i1 = Integer.parseInt(tag1.getValue());
                                                int i2 = Integer.parseInt(tag2.getValue());
                                                return i1 - i2;
                                            }
                                        }

                                        if (tag1.getValueType() == ItemTag.ValueType.STRING) {
                                            if (tag2.getValueType() == ItemTag.ValueType.STRING) {
                                                String i1 = tag1.getValue();
                                                String i2 = tag2.getValue();
                                                return compareStrs(i1, i2);
                                            }
                                        }
                                        return 0;
                                    }

                                    private int compareStrs(String i1, String i2) {
                                        try {
                                            if (i1.contains(":")) {
                                                String[] split = i1.split(":");
                                                int hour;
                                                int minute;
                                                int second;
                                                if (split.length > 2) {
                                                    hour = Integer.parseInt(split[0]);
                                                    minute = Integer.parseInt(split[1]);
                                                    second = Integer.parseInt(split[2]);
                                                } else {
                                                    hour = Integer.parseInt(split[0]);
                                                    minute = Integer.parseInt(split[1]);
                                                    second = 0;
                                                }

                                                int h1seconds = second + (minute * 60) + (hour * 60 * 60);

                                                String[] split2 = i2.split(":");
                                                if (split2.length > 2) {
                                                    hour = Integer.parseInt(split2[0]);
                                                    minute = Integer.parseInt(split2[1]);
                                                    second = Integer.parseInt(split2[2]);
                                                } else {
                                                    hour = Integer.parseInt(split2[0]);
                                                    minute = Integer.parseInt(split2[1]);
                                                    second = 0;
                                                }
                                                int h2seconds = second + (minute * 60) + (hour * 60 * 60);
                                                return h1seconds - h2seconds;
                                            }
                                            return 0;
                                        } catch (Exception e) {
                                            return 0;
                                        }
                                    }
                                };
                            });

                            yield tagValue;

                        default:
                            yield new CheckBox(context);
                    };
                }
            });
            listView.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Toast.makeText(context, "i="+i, Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });

            view.addView(reverted);
            view.addView(currentSelected);
            view.addView(listView);


            new MaterialAlertDialogBuilder(context)
                    .setTitle("Sort")
                    .setView(view)
                    .setPositiveButton("START", (irn1, ir112) -> {
                        final Item[] array = selectionManager.getItems();
                        Arrays.sort(array, (item, t1) -> comparator[0].compare(item, t1) * (reverted.isChecked() ? -1 : 1));

                        GroupItem sorted = new GroupItem("Sorted!");
                        GuiItemsHelper.applyInitRandomColorIfNeeded(context, sorted, App.get(context).getSettingsManager());
                        for (Item item : array) {
                            Item copy = ItemsRegistry.REGISTRY.copyItem(item);
                            sorted.addItem(copy);
                        }
                        finalParent.addItem(sorted);

                    })
                    .show();
        });


        TextView gcp = new TextView(context);
        gcp.setText("Global Changes Plugin:");
        localBinding.getRoot().addView(gcp);

        var horizont = new LinearLayout(context);
        horizont.setOrientation(LinearLayout.HORIZONTAL);
        horizont.addView(massBgColor);
        horizont.addView(massRandomBgColor);
        horizont.addView(selectAll);
        horizont.addView(massTextColor);
        horizont.addView(massRemoveNotification);
        horizont.addView(massJSON);
        horizont.addView(sortItems);

        var h = new HorizontalScrollView(context);
        h.addView(horizont);
        localBinding.getRoot().addView(h);

    }
}
