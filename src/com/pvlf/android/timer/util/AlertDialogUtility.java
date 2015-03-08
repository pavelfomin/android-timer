package com.pvlf.android.timer.util;

import com.pvlf.android.timer.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;

/**
 * Alert Dialog Utility helper class.
 */
public abstract class AlertDialogUtility {

	/**
	 * Shows the alert dialog.
	 * @param context context
	 * @param messageId message id
	 * @param positiveAction positive action
	 * @return alert dialog instance
	 */
	public static AlertDialog showAlertDialog(Context context, int messageId, OnClickListener positiveAction) {
		
		AlertDialog.Builder adb = new AlertDialog.Builder(context);
        adb.setTitle(messageId);
        adb.setNegativeButton(context.getString(R.string.cancel), null);
        adb.setPositiveButton(context.getString(R.string.ok), positiveAction);
		
		return adb.show();
		
	}
}
