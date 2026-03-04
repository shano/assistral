package org.shano.assistral;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class GooglePolicyNotice {
    static void showWarningOnUpgrade(Context context, int versionCode) {
        SharedPreferences prefManager = PreferenceManager.getDefaultSharedPreferences(context);
        int lastVersionCode = prefManager.getInt("googlePolicyNoticeVersionCode", 0);

        if (prefManager.contains("googlePolicyNoticeVersionCode") && versionCode > lastVersionCode) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            alertDialogBuilder.setMessage(R.string.dialog_google_policy_notice);
            alertDialogBuilder.setPositiveButton(context.getString(R.string.dialog_OK_button), null);
            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();
        }

        SharedPreferences.Editor editor = prefManager.edit();
        editor.putInt("googlePolicyNoticeVersionCode", versionCode);
        editor.apply();
    }
}
