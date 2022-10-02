package ru.fazziclay.opentoday.ui.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.items.item.TextItem;
import ru.fazziclay.opentoday.databinding.DialogTextItemEditTextBinding;
import ru.fazziclay.opentoday.util.MinTextWatcher;

public class DialogTextItemEditText {
    private final Activity activity;
    private Dialog dialog;
    private final TextItem textItem;
    private View view;

    private boolean canceled;
    private boolean unsaved;

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
        binding.editText.addTextChangedListener(new MinTextWatcher(){
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                super.onTextChanged(s, start, before, count);
                unsaved = true;
            }
        });



        binding.apply.setOnClickListener(v -> {
            textItem.setText(binding.editText.getText().toString());
            textItem.save();
            unsaved = false;
            textItem.visibleChanged();
            cancel();
        });

        binding.cancel.setOnClickListener(v -> requestCancel());

        return binding.getRoot();
    }

    private void requestCancel() {
        if (!unsaved) {
            cancel();
            return;
        }

        new AlertDialog.Builder(activity)
                .setTitle(R.string.dialogItem_cancel_unsaved_title)
                .setNegativeButton(R.string.dialogItem_cancel_unsaved_contunue, null)
                .setPositiveButton(R.string.dialogItem_cancel_unsaved_discard, ((fd23, which) -> cancel()))
                .show();
    }

    private void cancel() {
        canceled = true;
        dialog.cancel();
    }

    private Dialog generateDialog() {
        Dialog dialog = new Dialog(this.activity);
        dialog.setContentView(this.view);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setOnCancelListener(dialog1 -> {
            if (!canceled) {
                if (unsaved) {
                    dialog.show();
                    requestCancel();
                }
            }
        });
        return dialog;
    }
}
