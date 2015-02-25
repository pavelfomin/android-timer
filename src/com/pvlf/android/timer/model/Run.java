package com.pvlf.android.timer.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the data for a run.
 */
public class Run {

	private final long start;
	private final List<Lap> laps;
	private long duration;
	private String description;

	public Run(long start) {
		this.start = start;
		this.laps = new ArrayList<Lap>();
	}

	public long getStart() {
		return start;
	}

	public List<Lap> getLaps() {
		return laps;
	}

	public List<Lap> addLap(Lap lap) {
		laps.add(lap);
		return laps;
	}

	public long getDuration() {
		return duration;
	}

	public long getLapsDuration() {
		
		long duration = 0;
		
		for (Lap lap : laps) {
			duration += lap.getDuration();
		}

		return duration;
	}
	
	public void setDuration(long duration) {
		this.duration = duration;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void end(long end) {
		setDuration(end - getStart());
	}
	
	public boolean isCompleted() {
		return getDuration() > 0;
	}
	
	@Override
	public String toString() {
		return String
				.format("Run [start=%s, laps=%s, duration=%s, description=%s]", start, laps, duration, description);
	}
	
}
