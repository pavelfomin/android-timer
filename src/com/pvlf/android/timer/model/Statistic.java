package com.pvlf.android.timer.model;

/**
 * Run statistic.
 */
public class Statistic {
	private final long minimum;
	private final long maximum;
	private final long average;
	
	public Statistic(long minimum, long maximum, long average) {

		this.minimum = minimum;
		this.maximum = maximum;
		this.average = average;
	}

	public long getMinimum() {
		return minimum;
	}
	
	public long getMaximum() {
		return maximum;
	}
	
	public long getAverage() {
		return average;
	}
	
	@Override
	public String toString() {
		return String.format("RunStatistic [minimum=%s, maximum=%s, average=%s]", minimum, maximum, average);
	}
}
