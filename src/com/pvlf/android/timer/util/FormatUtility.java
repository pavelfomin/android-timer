package com.pvlf.android.timer.util;

import java.text.DecimalFormat;

/**
 * Format utility.
 */
public abstract class FormatUtility {

	/**
	 * Returns formatted distance.
	 * @param distance
	 * @return formatted distance
	 */
	public static String formatDistance(float distance) {
		
		DecimalFormat format = new DecimalFormat("0.00");
		return format.format(distance);
	}

	/**
	 * Returns formatted duration as h:m:ss:hs.
	 * @param millis
	 * @return formatted duration.
	 */
	public static String formatDuration(long millis) {
	
	    int seconds = (int) (millis / 1000);
	    int minutes = seconds / 60;
	    seconds = seconds % 60;
	    int hours = minutes / 60;
	    minutes = minutes % 60;
	
	    return String.format("%d:%02d:%02d:%1d", hours, minutes, seconds, (millis % 1000) / 100);
	}

	/**
	 * Returns formatted duration as m:ss.
	 * @param millis
	 * @return formatted duration.
	 */
	public static String formatDurationAsMinutesAndSeconds(long millis) {
		
		int seconds = (int) (millis / 1000);
		int minutes = seconds / 60;
		seconds = seconds % 60;
		
		return String.format("%d:%02d", minutes, seconds);
	}

}
