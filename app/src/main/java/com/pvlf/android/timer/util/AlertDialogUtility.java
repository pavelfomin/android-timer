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
	 * @param titleId title id
	 * @param positiveAction positive action
	 * @return alert dialog instance
	 */
	public static AlertDialog showAlertDialog(Context context, int titleId, OnClickListener positiveAction) {
		
		return showAlertDialog(context, titleId, null, positiveAction);
	}

	/**
	 * Shows the alert dialog.
	 * @param context context
	 * @param titleId title id
	 * @param message message
	 * @param positiveAction positive action
	 * @return alert dialog instance
	 */
	public static AlertDialog showAlertDialog(Context context, int titleId, String message, OnClickListener positiveAction) {
		
		AlertDialog.Builder adb = new AlertDialog.Builder(context);
		adb.setTitle(titleId);
		adb.setMessage(message);
		//adb.setNegativeButton(context.getString(R.string.cancel), null);
		adb.setPositiveButton(context.getString(R.string.ok), positiveAction);
		
		return adb.show();
		
	}
}
