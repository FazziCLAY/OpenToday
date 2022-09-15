package ru.fazziclay.opentoday.ui.dialog;

import static ru.fazziclay.opentoday.util.InlineUtil.fcu_viewOnClick;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.widget.Toast;

import ru.fazziclay.opentoday.app.App;
import ru.fazziclay.opentoday.app.CrashReport;
import ru.fazziclay.opentoday.databinding.DialogAppAboutBinding;
import ru.fazziclay.opentoday.ui.activity.OpenSourceLicensesActivity;

public class DialogAppAbout {
    private final DialogAppAboutBinding binding;
    private final Dialog dialog;
    private long easterEggLastClick = 0;
    private int easterEggCounter = 0;

    public DialogAppAbout(Activity activity) {
        // View
        binding = DialogAppAboutBinding.inflate(activity.getLayoutInflater());
        binding.textVersion.setText(App.VERSION_NAME);
        binding.textPackage.setText(App.APPLICATION_ID);

        fcu_viewOnClick(binding.title, () -> {
            if (System.currentTimeMillis() - easterEggLastClick < 1000) {
                easterEggCounter++;
                if (easterEggCounter == 3) {
                    Toast.makeText(activity, "Crash 7 clicks", Toast.LENGTH_SHORT).show();
                }
                if (easterEggCounter >= 10) {
                    Toast.makeText(activity, "Crash", Toast.LENGTH_SHORT).show();
                    App.crash(activity, CrashReport.create(Thread.currentThread(), new Exception("Manually AboutDialog EasterEgg crash"), System.currentTimeMillis(), System.nanoTime(), Thread.getAllStackTraces()));
                }
            } else {
                easterEggCounter = 0;
            }
            easterEggLastClick = System.currentTimeMillis();
        });

        // Dialog
        dialog = new Dialog(activity);

        // Buttons
        fcu_viewOnClick(binding.licence, () -> activity.startActivity(OpenSourceLicensesActivity.createLaunchIntent(activity)));
        fcu_viewOnClick(binding.ok, dialog::cancel);
    }

    public View getView() {
        return binding.getRoot();
    }

    public void show() {
        dialog.setContentView(getView());
        dialog.show();
    }
}
