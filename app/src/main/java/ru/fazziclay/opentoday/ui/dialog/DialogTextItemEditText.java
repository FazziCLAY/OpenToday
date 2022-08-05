package ru.fazziclay.opentoday.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;

import ru.fazziclay.opentoday.app.items.TextItem;
import ru.fazziclay.opentoday.databinding.DialogTextItemEditTextBinding;

public class DialogTextItemEditText {
    private final Activity activity;
    private Dialog dialog;
    private final TextItem textItem;
    private View view;

    public DialogTextItemEditText(Activity activity, TextItem textItem) {
        this.activity = activity;
        this.textItem = textItem;
    }

    public void show() {
        view = generateView();

        dialog = generateDialog();
        dialog.show();
    }

    private View generateView() {
        DialogTextItemEditTextBinding binding = DialogTextItemEditTextBinding.inflate(activity.getLayoutInflater());

        binding.editText.setText(textItem.getText());

        binding.apply.setOnClickListener(v -> {
            textItem.setText(binding.editText.getText().toString());
            textItem.save();
            textItem.updateUi();
            dialog.cancel();
        });

        return binding.getRoot();
    }

    private Dialog generateDialog() {
        Dialog dialog = new Dialog(this.activity, android.R.style.ThemeOverlay_Material);
        dialog.setContentView(this.view);
        dialog.setCanceledOnTouchOutside(false);
        return dialog;
    }
}
