package com.pvlf.android.timer.model;

/**
 * Holds the data for a lap.
 */
public class Lap {

	private final long start;
	private long duration;
	
	public Lap() {
		this.start = System.currentTimeMillis();
	}

	public Lap(long start) {
		this.start = start;
	}

	public long getStart() {
		return start;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public void end(long end) {
		setDuration(end - getStart());
	}

	/**
	 * Returns true if the lap is completed.
	 * @return true if the lap is completed.
	 */
	public boolean isCompleted() {
		return getDuration() > 0;
	}

	public static String formatDuration(long millis) {

        int seconds = (int) (millis / 1000);
        int minutes = seconds / 60;
        seconds = seconds % 60;
        int hours = minutes / 60;
        minutes = minutes % 60;

        return String.format("%d:%d:%02d:%02d", hours, minutes, seconds, (millis % 1000) / 10);
    }
	
	public String toFormattedString() {
		return formatDuration(getDuration());
	}

	@Override
	public String toString() {
		return String.format("Lap [start=%s, duration=%s]", getStart(), getDuration());
	}
}
