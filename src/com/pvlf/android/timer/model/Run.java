package com.pvlf.android.timer.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the data for a run.
 */
public class Run {

	private final List<Lap> laps;
	private String description;
	private boolean completed;

	public Run() {
		this.laps = new ArrayList<Lap>();
	}

	public List<Lap> getLaps() {
		return laps;
	}

	public List<Lap> addLap(Lap lap) {
		laps.add(lap);
		return laps;
	}

	/**
	 * Returns current lap.
	 * @return current lap.
	 */
	public Lap getCurrentLap() {
		return (laps.isEmpty() ? null : laps.get(laps.size() - 1));
	}

	/**
	 * Ends current lap.
	 * @param currentTimeMillis
	 * @return current lap
	 */
	public Lap endLap(long currentTimeMillis) {
	
		Lap lap = getCurrentLap();
		if (lap != null) {
			// end the current lap
			lap.end(currentTimeMillis);
		}
		return lap;
	}
	
	public long getDuration() {
		
		long duration = 0;
		
		for (Lap lap : laps) {
			duration += lap.getDuration();
		}

		return duration;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void end() {
		completed = true;
	}

	public void resume() {
		completed = false;
	}
	
	public boolean isCompleted() {
		return completed;
	}

	/**
	 * Returns run statistic for completed laps.
	 * @return run statistic for completed laps.
	 */
	public Statistic getRunStatistic() {
		
		long minimum = Long.MAX_VALUE;
		long maximum = Long.MIN_VALUE;
		long totalDuration = 0;
		int totalCompleted = 0;
		
		for (Lap lap : laps) {
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
			statistic = new Statistic(minimum, maximum, totalDuration / totalCompleted);
		}
		
		return statistic;
	}
	
	@Override
	public String toString() {
		return String.format("Run [laps=%s, description=%s]", laps, description);
	}
	
}
