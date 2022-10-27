package ru.fazziclay.opentoday.ui.fragment;

import static ru.fazziclay.opentoday.util.InlineUtil.fcu_viewOnClick;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ru.fazziclay.opentoday.R;
import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.databinding.FragmentAboutBinding;
import ru.fazziclay.opentoday.ui.UI;
import ru.fazziclay.opentoday.ui.activity.OpenSourceLicensesActivity;
import ru.fazziclay.opentoday.util.NetworkUtil;

public class AboutFragment extends Fragment {
    public static Fragment create() {
        return new AboutFragment();
    }

    private FragmentAboutBinding binding;
    private long easterEggLastClick = 0;
    private int easterEggCounter = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // View
        binding = FragmentAboutBinding.inflate(inflater);
        binding.textVersion.setText(App.VERSION_NAME);
        binding.textPackage.setText(App.APPLICATION_ID);

        fcu_viewOnClick(binding.title, this::manuallyCrashInteract);
        fcu_viewOnClick(binding.sourceCode, () -> NetworkUtil.openBrowser(requireActivity(), "https://github.com/fazziclay/opentoday"));
        fcu_viewOnClick(binding.issues, () -> NetworkUtil.openBrowser(requireActivity(), "https://github.com/fazziclay/opentoday/issues"));
        fcu_viewOnClick(binding.licenses, () -> requireActivity().startActivity(OpenSourceLicensesActivity.createLaunchIntent(requireContext())));
        fcu_viewOnClick(binding.ok, () -> UI.back(this));

        return binding.getRoot();
    }

    private void manuallyCrashInteract() {
        if (System.currentTimeMillis() - easterEggLastClick < 1000) {
            easterEggCounter++;
            if (easterEggCounter == 3) {
                Toast.makeText(requireContext(), R.string.manuallyCrash_7tap, Toast.LENGTH_SHORT).show();
            }
            if (easterEggCounter >= 10) {
                easterEggCounter = 0;
                Toast.makeText(requireContext(), R.string.manuallyCrash_crash, Toast.LENGTH_SHORT).show();
                EditText message = new EditText(requireContext());
                message.setHint(R.string.manuallyCrash_dialog_inputHint);
                Dialog dialog = new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.manuallyCrash_dialog_title)
                        .setView(message)
                        .setMessage(R.string.manuallyCrash_dialog_message)
                        .setPositiveButton(R.string.manuallyCrash_dialog_apply, (var1, var2) -> {
                            throw new RuntimeException("Manually AboutDialog EasterEgg crash: " + message.getText().toString());
                        })
                        .setNegativeButton(R.string.manuallyCrash_dialog_cancel, null)
                        .create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
            }
        } else {
            easterEggCounter = 0;
        }
        easterEggLastClick = System.currentTimeMillis();
    }
}
