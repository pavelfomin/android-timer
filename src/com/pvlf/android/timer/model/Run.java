package com.pvlf.android.timer.model;

import android.annotation.SuppressLint;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pvlf.android.timer.util.FormatUtility;

/**
 * Holds the data for a run.
 */
public class Run implements Serializable {
	private static final long serialVersionUID = -7490949038534165892L;

	private final RunContext runContext;
	private final List<Lap> laps;
	private boolean saved;
	
	public Run(RunContext runContext) {
		
		this.laps = new ArrayList<Lap>();
		this.runContext = runContext;
	}

	public RunContext getRunContext() {
		return runContext;
	}

	public List<Lap> getLaps() {
		return laps;
	}
	
	public int getLapsNumber() {
		return getLaps().size();
	}

	public Lap getLap(int position) {
		
		return (position >= 0 && position < getLaps().size() ? getLaps().get(position) : null);
	}
	
	public boolean removeLap(Lap lapToRemove) {
		
		int index = laps.indexOf(lapToRemove);
		lapToRemove = getLaps().remove(index);

		boolean removed = (lapToRemove != null);
		
		if (removed) {
			// get previous lap
			Lap previous = getLap(--index);
			if (previous != null) {
				// transfer start time to the previous lap
				previous.resetStart(lapToRemove.getStart());
				
				//reset positions for all preceding completed laps
				for (int i = index; i >= 0; i--) {
					Lap lap = getLap(i);
					lap.setPosition(lap.getPosition() - 1);
				}
			}
		}

		return removed;
	}
	
	/**
	 * Adds new lap at the beginning of the list.
	 * @param lap
	 */
	public void addLap(Lap lap) {

		getLaps().add(0, lap);
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
		
		for (Lap lap : getLaps()) {
			duration += lap.getDuration();
		}

		return duration;
	}
	
	@SuppressLint("SimpleDateFormat")
	public String getDescription() {
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		Lap lap = (getLaps().isEmpty() ? null: getLaps().get(getLaps().size() - 1));
		
		StringBuilder sb = new StringBuilder();
		if (lap == null) {
			sb.append("Empty run");
		} else {
			Statistic statistic = getRunStatistic();
			sb.append(format.format(new Date(lap.getStart())));
			sb.append(" - ").append(FormatUtility.formatDurationAsMinutesAndSeconds(getDuration()));
			if (statistic != null) {
				sb.append(" (");
				sb.append(FormatUtility.formatDistance(statistic.getDistance()));
				sb.append(" / ");
				sb.append(FormatUtility.formatDurationAsMinutesAndSeconds((long) statistic.getAverageSpeed()));
				sb.append(")");
			}
		}
		return sb.toString();
	}
	
	public boolean isCompleted() {
		
		Lap lap = getCurrentLap();
		return (lap != null && lap.isCompleted());
	}

	/**
	 * Returns run statistic for completed laps.
	 * @return run statistic for completed laps.
	 */
	public Statistic getRunStatistic() {
		
		return Statistic.createStatistic(this);
	}
	
	public boolean isSaved() {
		return saved;
	}

	public void setSaved(boolean saved) {
		this.saved = saved;
	}

	@Override
	public String toString() {
		return getDescription();
	}

}
