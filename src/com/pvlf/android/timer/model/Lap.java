package com.pvlf.android.timer.model;

/**
 * Holds the data for a lap.
 */
public class Lap {

	private final RunContext runContext;

	/**
	 * Start time in milliseconds.
	 */
	private long start;
	
	/**
	 * Position in the run, 1-based.
	 */
	private int position;

	/**
	 * Duration in milliseconds.
	 */
	private long duration;
	
	/**
	 * Creates new instance with a specific time.
	 */
	public Lap(long start, RunContext runContext) {
		
		this.start = start;
		this.runContext = runContext;
	}

	public long getStart() {
		return start;
	}

	public void resetStart(long newStart) {

		if (isCompleted()) {
			//adjust duration for a completed lap
			setDuration(getDuration() + getStart() - newStart);
		}
		this.start = newStart;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public long getDuration() {
		return duration;
	}

	public  void setDuration(long duration) {
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
	
	public static String formatDurationAsMinutesAndSeconds(long millis) {
		
		int seconds = (int) (millis / 1000);
		int minutes = seconds / 60;
		seconds = seconds % 60;
		
		return String.format("%d:%02d", minutes, seconds);
	}
	
	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append(getPosition()).append(": ");
		sb.append(formatDuration(getDuration())).append(" - ");
		sb.append(formatDurationAsMinutesAndSeconds((long) (getDuration() * runContext.getLapsPerUnitOfDistance())));

		return sb.toString(); 
	}
}
