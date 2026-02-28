package com.pvlf.android.timer.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import android.util.Log;

/**
 * Storage Utility.
 * Marked as abstract to emphasize the static usage only i.e. can't be instantiated.
 */
public abstract class StorageUtility {
	private static final String TAG = StorageUtility.class.toString();
	
	/**
	 * Saves the data into the file.
	 * @param file
	 * @param data
	 * @throws Exception if an error occurs.
	 */
	public static void save(File file, String data) throws Exception {
		
		FileOutputStream output = null;
		try {
			output = new FileOutputStream(file);
			output.write(data.getBytes());
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (Exception e) {
					Log.e(TAG, "Failed to close the output for file="+ file, e);
				}
			}
		}
		
	}

	/**
	 * Reads the content of the file and returns it as a String.
	 * @param file
	 * @return content of the file
	 * @throws Exception if an error occurs
	 */
	public static String read(File file) throws Exception {
		FileInputStream input = null;
		try {
			input = new FileInputStream(file);
			StringBuilder builder = new StringBuilder();

			byte[] buffer = new byte[1024];

			while (input.read(buffer) != -1) {
			    builder.append(new String(buffer));
			}
			return builder.toString();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (Exception e) {
					Log.e(TAG, "Failed to close the input for file="+ file, e);
				}
			}
		}
	}

}
