package com.fazziclay.opentoday.gui.fragment;

import static com.fazziclay.opentoday.util.InlineUtil.viewClick;
import static com.fazziclay.opentoday.util.InlineUtil.viewLong;
import static com.fazziclay.opentoday.util.InlineUtil.viewVisible;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.fazziclay.opentoday.Debug;
import com.fazziclay.opentoday.R;
import com.fazziclay.opentoday.app.App;
import com.fazziclay.opentoday.app.ColorHistoryManager;
import com.fazziclay.opentoday.app.items.ItemsRoot;
import com.fazziclay.opentoday.app.items.item.LongTextItem;
import com.fazziclay.opentoday.app.items.item.TextItem;
import com.fazziclay.opentoday.app.settings.SettingsManager;
import com.fazziclay.opentoday.databinding.FragmentItemTextEditorBinding;
import com.fazziclay.opentoday.gui.ActivitySettings;
import com.fazziclay.opentoday.gui.ColorPicker;
import com.fazziclay.opentoday.gui.UI;
import com.fazziclay.opentoday.gui.interfaces.ActivitySettingsMember;
import com.fazziclay.opentoday.gui.interfaces.BackStackMember;
import com.fazziclay.opentoday.util.ColorUtil;
import com.fazziclay.opentoday.util.MinTextWatcher;
import com.fazziclay.opentoday.util.ResUtil;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

public class ItemTextEditorFragment extends Fragment implements BackStackMember, ActivitySettingsMember {
    public static final int EDITABLE_TYPE_TEXT = 0;
    public static final int EDITABLE_TYPE_LONG_TEXT = 1;
    public static final int EDITABLE_TYPE_AUTO = 2;
    private static final CharSequence[] AVAILABLE_TEXT_SIZES = new CharSequence[]{
            "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16",
            "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30"
    };

    private static final String KEY_ID = "ItemTextEditorFragment_itemId";
    private static final String KEY_EDITABLE_TYPE = "ItemTextEditorFragment_editableType";
    private static final String KEY_OVERRIDE_PREVIEW_BACKGROUND = "ItemTextEditorFragment_overridePreviewBackground";
    private static final String TAG = "ItemTextEditorFragment";
    private int themeForeColor;


    public static ItemTextEditorFragment create(UUID id) {
        return create(id, EDITABLE_TYPE_AUTO, null);
    }

    public static ItemTextEditorFragment create(@NotNull UUID id,
                                                @MagicConstant(intValues = {EDITABLE_TYPE_AUTO, EDITABLE_TYPE_TEXT, EDITABLE_TYPE_LONG_TEXT}) int editableType,
                                                @Nullable String overridePreviewBackground) {
        ItemTextEditorFragment f = new ItemTextEditorFragment();

        Bundle args = new Bundle();
        args.putString(KEY_ID, id.toString());
        args.putInt(KEY_EDITABLE_TYPE, editableType);
        if (overridePreviewBackground != null) args.putString(KEY_OVERRIDE_PREVIEW_BACKGROUND, overridePreviewBackground);

        f.setArguments(args);

        return f;
    }


    private FragmentItemTextEditorBinding binding;
    private MenuItem previewMenuItem = null;
    private TextItem item;
    private boolean isLongText;
    private boolean showPreview;
    private String overridePreviewBackground;
    private ColorHistoryManager colorHistoryManager;

    // CURRENT SYSTEM (BEGIN)
    private int systemStart;
    private int systemEnd;
    private String system;
    // CURRENT SYSTEM (END)


    private ItemTextEditorFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final App app = App.get(requireContext());
        final ItemsRoot itemsRoot = app.getItemsRoot();
        final Bundle args = getArguments();
        this.colorHistoryManager = app.getColorHistoryManager();
        final SettingsManager settingsManager = app.getSettingsManager();

        if (args.containsKey(KEY_OVERRIDE_PREVIEW_BACKGROUND)) {
            overridePreviewBackground = args.getString(KEY_OVERRIDE_PREVIEW_BACKGROUND);
        }

        final UUID id = UUID.fromString(args.getString(KEY_ID));
        final int editableType = args.getInt(KEY_EDITABLE_TYPE);

        var _item = itemsRoot.getItemById(id);
        if (!(_item instanceof TextItem)) {
            Toast.makeText(requireContext(), R.string.abc_unknown, Toast.LENGTH_LONG).show();
            UI.rootBack(this);
            return;
        }
        item = (TextItem) _item;
        if (item == null) {
            throw new RuntimeException("Item not found in ItemsRoot by provided UUID");
        }
        if (editableType == EDITABLE_TYPE_AUTO || editableType == EDITABLE_TYPE_LONG_TEXT) {
            isLongText = true;
        } else if (editableType == EDITABLE_TYPE_TEXT) {
            isLongText = false;
        }
        if (!(item instanceof LongTextItem)) isLongText = false;

        themeForeColor = UI.getTheme().getRawForegroundColor();
        if (savedInstanceState == null) {
            setupActivitySettings();
        }
    }

    private void setupActivitySettings() {
        UI.getUIRoot(this)
                .pushActivitySettings(a -> {
                    a.setNotificationsVisible(false);
                    a.setClockVisible(false);
                    a.setToolbarSettings(
                            ActivitySettings.ToolbarSettings.createBack(isLongText ? R.string.fragment_itemTextEditor_toolbar_title_longTextItem : R.string.fragment_itemTextEditor_toolbar_title_textItem, this::cancelRequest)
                                    .setMenu(R.menu.menu_item_text_editor, menu -> {
                                        MenuItem preview = previewMenuItem = menu.findItem(R.id.previewFormatting);
                                        preview.setChecked(showPreview);
                                        preview.setOnMenuItemClickListener(menuItem -> {
                                            menuItem.setChecked(!showPreview);
                                            setShowPreview(!showPreview);
                                            return true;
                                        });

                                        MenuItem formattingHelp = menu.findItem(R.id.helpFormatting);
                                        formattingHelp.setOnMenuItemClickListener(menuItem -> {
                                            openFormattingHelp();
                                            return true;
                                        });

                                        MenuItem saveText = menu.findItem(R.id.saveText);
                                        saveText.setOnMenuItemClickListener(menuItem -> {
                                            applyAndClose();
                                            return true;
                                        });
                                    })
                    );
                });
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentItemTextEditorBinding.inflate(inflater);
        setupView();
        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Debug.itemTextEditor = null;
    }

    private void applyAndClose() {
        setEditableText(binding.editText.getText().toString());
        UI.rootBack(this);
    }

    // return true if block
    @Override
    public boolean popBackStack() {
        if (isUnsavedChanges()) {
            cancelRequest();
            return true;
        }

        return false;
    }

    private void cancelRequest() {
        if (isUnsavedChanges()) {
            new AlertDialog.Builder(requireContext())
                    .setTitle(R.string.fragment_itemTextEditor_cancel_unsaved_title)
                    .setNegativeButton(R.string.fragment_itemTextEditor_cancel_unsaved_continue, null)
                    .setPositiveButton(R.string.fragment_itemTextEditor_cancel_unsaved_discard, ((dialog1, which) -> {
                        binding.editText.setText(getEditableText());
                        UI.rootBack(this);
                    }))
                    .show();
        } else {
            UI.rootBack(this);
        }
    }

    private void openFormattingHelp() {
        TextView text = new TextView(requireContext());
        text.setPadding(10, 10, 10, 10);
        ScrollView scrollView = new ScrollView(requireContext());
        scrollView.addView(text);

        text.setText(ColorUtil.colorize(
                getString(R.string.fragment_itemTextEditor_formattingHelp_helpText),
                ResUtil.getStyleColor(requireContext(), R.style.Theme_OpenToday, com.google.android.material.R.attr.colorOnBackground).getColor(0, Color.RED),
                Color.TRANSPARENT,
                Typeface.NORMAL,
                false));

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.fragment_itemTextEditor_formattingHelp_title)
                .setView(scrollView)
                .setPositiveButton(R.string.abc_ok, null)
                .show();
    }

    private boolean isUnsavedChanges() {
        if (item == null) return false;
        return !getEditableText().equals(binding.editText.getText().toString());
    }

    private String getEditableText() {
        if (item == null) return "";
        if (isLongText && item instanceof LongTextItem l) {
            return l.getLongText();
        }
        return item.getText();
    }

    private int getEditableTextColor() {
        if (isLongText && item instanceof LongTextItem l) {
            if (!l.isCustomLongTextColor()) return ResUtil.getAttrColor(requireContext(), R.attr.item_text_textColor);
            return l.getLongTextColor();
        }
        if (!item.isCustomTextColor()) return ResUtil.getAttrColor(requireContext(), R.attr.item_text_textColor);
        return item.getTextColor();
    }

    private void setEditableText(String s) {
        if (isLongText && item instanceof LongTextItem l) {
            l.setLongText(s);
        } else {
            item.setText(s);
        }
        item.visibleChanged();
        item.save();
    }

    private void setupView() {
        binding.editText.setText(getEditableText());

        binding.editText.requestFocus();
        InputMethodManager imm = getContext().getSystemService(InputMethodManager.class);
        imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);

        MinTextWatcher.after(binding.editText, this::updatePreview);
        updateCurrentSystem(0);
        binding.editText.setOnSelectionChangedListener((view, start, end) -> {
            if (start == end) updateCurrentSystem(start);
            updatePreview();
        });
        setShowPreview(item.isParagraphColorize() && getEditableText().contains("$"));
        if (overridePreviewBackground != null) {
            binding.formattingPreview.setBackgroundColor(Color.parseColor(overridePreviewBackground));
        } else if (item.isViewCustomBackgroundColor()) {
            binding.formattingPreview.setBackgroundColor(item.getViewBackgroundColor());
        }

        viewClick(binding.addSystem, () -> {
            putText(binding.editText.getSelectionStart(), "$[]");
            if (!isShowPreview()) {
                setShowPreview(true);
            }
        });
        viewClick(binding.deleteSystem, this::clearCurrentSystem);

        viewClick(binding.foregroundColor, () -> new ColorPicker(requireContext(), getSystemColorValue("-"))
                .setting(true, true, true)
                .setColorHistoryManager(colorHistoryManager)
                .setColorHistoryMax(20)
                .setNeutralDialogButton(getString(R.string.fragment_itemTextEditor_foregroundColor_reset), () -> resetSystem("-"))
                .showDialog(R.string.fragment_itemTextEditor_foregroundColor_title, R.string.fragment_itemTextEditor_foregroundColor_cancel, R.string.fragment_itemTextEditor_foregroundColor_apply, (color) -> setSystemValue("-", ColorUtil.colorToHex(color))));
        viewLong(binding.foregroundColor, () -> setSystemValue("-", "reset"));


        viewClick(binding.backgroundSystem, () -> new ColorPicker(requireContext(), getSystemColorValue("="))
                .setting(true, true, true)
                .setColorHistoryManager(colorHistoryManager)
                .setColorHistoryMax(20)
                .setNeutralDialogButton(getString(R.string.fragment_itemTextEditor_backgroundColor_reset), () -> resetSystem("="))
                .showDialog(R.string.fragment_itemTextEditor_backgroundColor_title, R.string.fragment_itemTextEditor_backgroundColor_cancel, R.string.fragment_itemTextEditor_backgroundColor_apply, (color) -> setSystemValue("=", ColorUtil.colorToHex(color))));
        viewLong(binding.backgroundSystem, () -> setSystemValue("=", "reset"));

        viewClick(binding.isBold, () -> {
            String atContent = getSystemStringValue("@", "");
            boolean bold = atContent.contains("bold");
            boolean italic = atContent.contains("italic");
            boolean strikeOut = atContent.contains("~");
            setSystemValue("@", getStyleContent(!bold, italic, strikeOut));
        });

        viewClick(binding.isItalic, () -> {
            String atContent = getSystemStringValue("@", "");
            boolean bold = atContent.contains("bold");
            boolean italic = atContent.contains("italic");
            boolean strikeOut = atContent.contains("~");
            setSystemValue("@", getStyleContent(bold, !italic, strikeOut));
        });

        viewClick(binding.isStrikeOut, () -> {
            String atContent = getSystemStringValue("@", "");
            boolean bold = atContent.contains("bold");
            boolean italic = atContent.contains("italic");
            boolean strikeOut = atContent.contains("~");
            setSystemValue("@", getStyleContent(bold, italic, !strikeOut));
        });

        for (View view : new View[]{binding.isItalic, binding.isStrikeOut, binding.isBold}) {
            viewLong(view, () -> setSystemValue("@", "reset"));
        }

        viewClick(binding.formatTextSize, () -> {
            int size;
            try {
                size = Integer.parseInt(getSystemStringValue("S", "-1"));
            } catch (Exception e) {
                size = -1;
            }
            new TextSizeDialog()
                    .setReset(() -> resetSystem("S"))
                    .setSelectedSize(size)
                    .show((s) -> setSystemValue("S", String.valueOf(s)));
        });
        viewLong(binding.formatTextSize, () -> setSystemValue("S", "reset"));
    }

    private String getStyleContent(boolean bold, boolean italic, boolean strikeOut) {
        if (bold && italic) {
            return "bolditalic" + (strikeOut ? "~" : "");
        } else if (bold) {
            return "bold" + (strikeOut ? "~" : "");
        } else if (italic) {
            return "italic" + (strikeOut ? "~" : "");
        } else {
            return "" + (strikeOut ? "~" : "");
        }
    }

    private void updateCurrentSystem(int start) {
        String s = binding.editText.getText().toString();
        systemStart = -1;
        systemEnd = -1;

        int i = start-1;
        while (i >= 0) {
            if (s.charAt(i) == ']') break;
            if (s.charAt(i) == '[') {
                if (i-1 >= 0 && s.charAt(i-1) == '$') {
                    systemStart = i;
                    break;
                }
            }
            i--;
        }

        i = start;
        while (i < s.length()) {
            if (s.charAt(i) == '$') break;
            if (s.charAt(i) == ']') {
                systemEnd = i;
                break;
            }
            i++;
        }

        if (systemStart >= 0 && systemEnd >= 0) {
            system = s.substring(systemStart, systemEnd);
            if (!system.isEmpty()) system = system.substring(1);
        } else {
            system = null;
        }

        Debug.itemTextEditor = String.format("SysSTART=%s; SysEND=%s s=%s", systemStart, systemEnd, system);

        viewVisible(binding.addSystem, !isSystem(), View.GONE);
        viewVisible(binding.deleteSystem, isSystem(), View.GONE);

        binding.foregroundColor.setImageTintList(ColorStateList.valueOf(getSystemColorValue("-")));
        viewVisible(binding.foregroundColor, isSystem(), View.GONE);

        binding.backgroundSystem.setImageTintList(ColorStateList.valueOf(getSystemColorValue("=")));
        viewVisible(binding.backgroundSystem, isSystem(), View.GONE);


        viewVisible(binding.isBold, isSystem(), View.GONE);
        binding.isBold.setBackgroundTintList(ColorStateList.valueOf(getStyleTintColor("bold")));


        viewVisible(binding.isItalic, isSystem(), View.GONE);
        binding.isItalic.setBackgroundTintList(ColorStateList.valueOf(getStyleTintColor("italic")));

        viewVisible(binding.isStrikeOut, isSystem(), View.GONE);
        binding.isStrikeOut.setBackgroundTintList(ColorStateList.valueOf(getStyleTintColor("~")));

        viewVisible(binding.formatTextSize, isSystem(), View.GONE);
    }

    private int getStyleTintColor(String type) {
        var colorStyleReset = ResUtil.getAttrColor(requireContext(), R.attr.itemTextEditor_style_reset);
        if (system != null && system.equals("||")) return colorStyleReset;

        String styleValue = getSystemStringValue("@", "");
        if (styleValue.equals("reset")) return colorStyleReset;

        var colorStyleTrue = ResUtil.getAttrColor(requireContext(), R.attr.itemTextEditor_style_true);
        var colorStyleFalse = ResUtil.getAttrColor(requireContext(), R.attr.itemTextEditor_style_false);

        return styleValue.contains(type) ? colorStyleTrue : colorStyleFalse;
    }

    private int getSystemColorValue(String chas) {
        if (system == null) return themeForeColor;
        for (String s : system.split(";")) {
            if (s.startsWith(chas) && s.length() > 1) {
                String colorValue = s.substring(1);
                try {
                    return Color.parseColor(colorValue);
                } catch (Exception e) {
                    return themeForeColor;
                }
            }
        }
        return themeForeColor;
    }

    private String getSystemStringValue(String chas, String def) {
        if (system == null) return def;
        for (String s : system.split(";")) {
            if (s.startsWith(chas) && s.length() > 1) {
                return s.substring(1);
            }
        }
        return def;
    }

    private void setSystemValue(String c, String val) {
        if (system == null || c.isEmpty()) return;
        editSystem(ColorUtil.sysSet(system, c.charAt(0), val));
    }

    private void resetSystem(String c) {
        if (system == null || c.isEmpty()) return;
        editSystem(ColorUtil.sysReset(system, c.charAt(0)));
    }

    private void editSystem(String s) {
        if (system == null) return;
        String first = binding.editText.getText().toString().substring(0, systemStart);
        String end = binding.editText.getText().toString().substring(systemEnd);


        int pos = systemStart + 1;
        binding.editText.setText(first + "[" +s + end);
        binding.editText.setSelection(pos);
    }

    private void putText(int pos, String text) {
        String first = binding.editText.getText().toString().substring(0, pos);
        String end = binding.editText.getText().toString().substring(pos);


        binding.editText.setText(first + text + end);
        binding.editText.setSelection(pos + text.length()-1);
    }

    private void clearCurrentSystem() {
        if (system == null) return;
        String first = binding.editText.getText().toString().substring(0, systemStart-1);
        String end = binding.editText.getText().toString().substring(systemEnd+1);


        int pos = systemStart - 1;
        binding.editText.setText(first + end);
        binding.editText.setSelection(pos);
    }

    private void updatePreview() {
        String s = binding.editText.getText().toString();
        binding.editText.setTextSize(s.length()>100 ? 15 : 20);

        if (!showPreview) return;
        int i = binding.editText.getSelectionStart();
        int line = getLinePosition(s, i);
        binding.formattingPreview.setText(ColorUtil.colorize(lineArea(s, line), getEditableTextColor(), Color.TRANSPARENT, Typeface.NORMAL));
    }

    private void setShowPreview(boolean b) {
        this.showPreview = b;
        viewVisible(binding.formattingPreview, b, View.GONE);
        updatePreview();
        if (previewMenuItem != null) previewMenuItem.setChecked(b);
    }

    public boolean isShowPreview() {
        return showPreview;
    }

    private boolean isSystemXExist(String anChar) {
        if (system == null) return false;
        for (String s : system.split(";")) {
            if (s.startsWith(anChar)) return true;
        }
        return false;
    }

    private int getLinePosition(String s, int charPos) {
        if (charPos > s.length()) throw new IndexOutOfBoundsException("charPos index out bounds provided string");
        if (charPos < 0) throw new IllegalArgumentException("charPos can't be negative");
        String[] lines = s.split("\n");
        int length = 0;
        int i = 0;
        for (String line : lines) {
            length += line.length()+1; // +1 for \n symbol
            if (length >= charPos) return i;
            i++;
        }
        return 0;
    }

    private int getLines(String s) {
        return s.split("\n").length;
    }

    private String lineArea(final String s, final int line) {
        if (line < 0) throw new IndexOutOfBoundsException("argument line can't be negative");
        final String[] lines = s.split("\n");

        return concatLines(
                getLineIfExist(lines, line-2),
                getLineIfExist(lines, line-1),
                getLineIfExist(lines, line),
                getLineIfExist(lines, line+1),
                getLineIfExist(lines, line+2)
        );
    }

    private String getLineIfExist(String[] lines, int line) {
        if (line < 0) return null;
        if (line >= lines.length) return null;
        return lines[line];
    }

    private String concatLines(String... lines) {
        StringBuilder b = new StringBuilder();

        for (String line : lines) {
            if (line == null) continue;
            b.append(line).append("\n");
        }

        return b.substring(0, b.lastIndexOf("\n"));
    }


    public boolean isSystem() {
        return system != null;
    }

    private class TextSizeDialog {
        private Runnable resetRunnable;
        private int selectedSize = -1;

        public TextSizeDialog setReset(Runnable s) {
            this.resetRunnable = s;
            return this;
        }

        public TextSizeDialog setSelectedSize(int selectedSize) {
            this.selectedSize = selectedSize;
            return this;
        }

        public void show(Consumer<Integer> applyConsumer) {
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.fragment_itemTextEditor_size_title)
                    .setSingleChoiceItems(AVAILABLE_TEXT_SIZES, Math.max(0, findElementPositionInArray(AVAILABLE_TEXT_SIZES, String.valueOf(selectedSize))), (dialogInterface, i) -> {
                        if (i < 0) return;
                        int selectedSize = Integer.parseInt(String.valueOf(AVAILABLE_TEXT_SIZES[i]));
                        applyConsumer.accept(selectedSize);
                        dialogInterface.cancel();
                    })
                    .setPositiveButton(R.string.abc_cancel, null)
                    .setNegativeButton(R.string.fragment_itemTextEditor_size_reset, (dialogInterface, i) -> resetRunnable.run())
                    .show();
        }
    }

    private int findElementPositionInArray(Object[] array, Object element) {
        int i = 0;
        for (Object o : array) {
            if (o.equals(element)) return i;
            i++;
        }
        return -1;
    }
}
