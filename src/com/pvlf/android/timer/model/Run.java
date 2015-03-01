package com.pvlf.android.timer.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Holds the data for a run.
 */
public class Run {

	private final List<Lap> laps;
	private boolean completed;

	public Run() {
		this.laps = new ArrayList<Lap>();
	}

	public List<Lap> getLaps() {
		return laps;
	}
	
	public int getLapsNumber() {
		return getLaps().size();
	}

	public Lap getLap(int position) {
		
		return (position < getLaps().size() ? getLaps().get(position) : null);
	}
	
	public boolean removeLap(Lap lapToRemove) {
		
		int index = laps.indexOf(lapToRemove);
		lapToRemove = getLaps().remove(index);

		boolean removed = (lapToRemove != null);
		
		if (removed) {
			// get previous lap
			Lap previous = getLap(--index);
			
			// transfer start time to the previous lap
			previous.resetStart(lapToRemove.getStart());
			
			//reset positions for all preceding completed laps
			for (int i = index; i >= 0; i--) {
				Lap lap = getLap(i);

				if (lap.isCompleted()) {
					lap.setPosition(lap.getPosition() - 1);
				}
			}
			
		}

		return removed;
	}
	
	public List<Lap> addLap(Lap lap) {

		getLaps().add(0, lap);
		return getLaps();
	}

	/**
	 * Returns current lap.
	 * @return current lap.
	 */
	public Lap getCurrentLap() {
		
		return (getLaps().isEmpty() ? null : getLaps().get(0));
	}

	/**
	 * Ends current lap.
	 * @param currentTimeMillis
	 * @param position 1-based lap position in a run 
	 * @return current lap
	 */
	public Lap endLap(long currentTimeMillis, int position) {
	
		Lap lap = getCurrentLap();
		if (lap != null) {
			// end the current lap
			lap.end(currentTimeMillis);
			lap.setPosition(position);
		}
		return lap;
	}
	
	public long getDuration() {
		
		long duration = 0;
		
		for (Lap lap : getLaps()) {
			duration += lap.getDuration();
		}

		return duration;
	}
	
	public String getDescription() {
		
		Lap lap = (getLaps().isEmpty() ? null: getLaps().get(getLaps().size() - 1));
		return (lap == null ? "Empty run" : new Date(lap.getStart()) + ": "+ Lap.formatDuration(getDuration()));
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
		
		for (Lap lap : getLaps()) {
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
		return String.format("Run [laps=%s, description=%s]", getLaps(), getDescription());
	}

}
