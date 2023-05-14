package com.fazziclay.opentoday;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;

import com.fazziclay.javaneoutil.FileUtil;
import com.fazziclay.opentoday.app.App;

import java.io.File;

public class PinCodeManager {
    private final SharedPreferences sharedPreferences;
    private final File backupFile;


    public PinCodeManager(Context context) {
        this.sharedPreferences = context.getSharedPreferences(App.SHARED_NAME, MODE_PRIVATE);
        this.backupFile = new File(context.getExternalFilesDir(""), "pcb");
    }

    public boolean isPinCodeSet() {
        return sharedPreferences.contains(App.SHARED_KEY_PINCODE);
    }

    public String getPinCode() {
        return sharedPreferences.getString(App.SHARED_KEY_PINCODE, "0000");
    }

    public void disablePinCode() {
        sharedPreferences.edit().remove(App.SHARED_KEY_PINCODE).apply();
        if (FileUtil.isExist(backupFile)) {
            FileUtil.delete(backupFile);
        }
    }

    public void enablePinCode(String pin) {
        if (pin.isEmpty()) {
            throw new ContainNonDigitChars();
        }
        for (char c : pin.toCharArray()) {
            if (!Character.isDigit(c)) {
                throw new ContainNonDigitChars();
            }
        }
        sharedPreferences.edit().putString(App.SHARED_KEY_PINCODE, pin).apply();
        FileUtil.setText(backupFile, pin); // TODO: 2023.05.09 make a #comment in head of backup file with text: ...This is pin-c0de backup file! Do not edit this. ...
    }

    public static class ContainNonDigitChars extends RuntimeException {
        public ContainNonDigitChars() {
            super("Contains non-digit chars in pin-code!");
        }
    }
}
