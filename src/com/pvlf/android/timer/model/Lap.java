package com.pvlf.android.timer.model;

import java.io.Serializable;

/**
 * Holds the data for a lap.
 */
public class Lap implements Serializable {
	private static final long serialVersionUID = 5289031188887391211L;

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
	 * Creates new instance with a specific time, context and position.
	 */
	public Lap(long start, RunContext runContext, int position) {
		
		this.start = start;
		this.runContext = runContext;
		setPosition(position);
	}

	/**
	 * Creates new instance with a specific time, context, position and duration.
	 */
	public Lap(long start, RunContext runContext, int position, long duration) {
		
		this.start = start;
		this.runContext = runContext;
		setPosition(position);
		setDuration(duration);
	}
	
	/**
	 * Creates new instance with a specific time and context.
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

	private void setDuration(long duration) {
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

        return String.format("%d:%02d:%02d:%02d", hours, minutes, seconds, (millis % 1000) / 10);
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
	
	@Override
	public String toString() {

		StringBuilder sb = new StringBuilder();
		sb.append(getPosition()).append(": ");
		sb.append(formatDuration(getDuration())).append(" - ");
		sb.append(formatDurationAsMinutesAndSeconds((long) (getDuration() * runContext.getLapsPerUnitOfDistance())));

		return sb.toString(); 
	}
}
