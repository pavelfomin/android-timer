package com.pvlf.android.timer.model;

import com.pvlf.android.timer.util.FormatUtility;

/**
 * Run statistic.
 */
public class Statistic {
	private final long minimum;
	private final long maximum;
	private final long duration;
	private final int laps;
	private final RunContext runContext;
	
	public Statistic(long minimum, long maximum, long duration, int laps, RunContext runContext) {

		this.minimum = minimum;
		this.maximum = maximum;
		this.duration = duration;
		this.laps = laps;
		this.runContext = runContext;
	}

	public long getMinimum() {
		return minimum;
	}
	
	public long getMaximum() {
		return maximum;
	}
	
	public long getDuration() {
		return duration;
	}

	public int getLaps() {
		return laps;
	}

	public long getAverage() {
		return (getLaps() > 0 ? getDuration() / getLaps() : 0);
	}

	public float getAverageSpeed() {
		return getAverage() * runContext.getLapsPerUnitOfDistance();
	}

	public float getSlowestSpeed() {
		return getMaximum() * runContext.getLapsPerUnitOfDistance();
	}
	
	public float getFastestSpeed() {
		return getMinimum() * runContext.getLapsPerUnitOfDistance();
	}
	
	public float getDistance() {
		return getLaps() / runContext.getLapsPerUnitOfDistance();
	}
	
	public String getDistanceFormatted() {
		return FormatUtility.formatDistance(getDistance());
	}
	
	/**
	 * Creates statistic for a run passed.
	 * @param run
	 * @return statistic
	 */
	public static Statistic createStatistic(Run run) {
		
		if (run == null) {
			throw new IllegalArgumentException("Run can't be null");
		}
		
		long minimum = Long.MAX_VALUE;
		long maximum = Long.MIN_VALUE;
		long totalDuration = 0;
		int totalCompleted = 0;
		
		for (Lap lap : run.getLaps()) {
			if (lap.isCompleted()) {
				long duration = lap.getDuration();
				if (duration > maximum) {
					maximum = duration;
				}
				if (duration < minimum) {
					minimum = duration;
				}
				totalDuration += duration;
				totalCompleted++;
			}
		}

		Statistic statistic = null;
		if (totalCompleted > 0) {
			statistic = new Statistic(minimum, maximum, totalDuration, totalCompleted, run.getRunContext());
		}
		
		return statistic;
	}
	
	@Override
	public String toString() {
		return String.format("Statistic [minimum=%s, maximum=%s, duration=%s, laps=%s]", 
				minimum, maximum, getDuration(), getLaps());
	}
}
