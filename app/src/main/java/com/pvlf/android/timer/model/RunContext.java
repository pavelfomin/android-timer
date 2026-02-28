package com.pvlf.android.timer.model;

import java.io.Serializable;

import android.content.SharedPreferences;

/**
 * Run context that holds information relevant to a run. 
 */
public class RunContext implements Serializable {
	private static final long serialVersionUID = 7435756341369408564L;

	public static final String LAPS_PER_UNIT_OF_DISTANCE_KEY = "lapsPerUnitOfDistance";
	public static final String REFRESH_RATE_KEY = "refreshRate";

	public RunContext() {
	}

	public RunContext(SharedPreferences sharedPreferences) {
		
		reset(sharedPreferences);
	}
	
	private float lapsPerUnitOfDistance;
	private int refreshRate;

	public float getLapsPerUnitOfDistance() {
		return lapsPerUnitOfDistance;
	}

	public void setLapsPerUnitOfDistance(float lapsPerUnitOfDistance) {
		this.lapsPerUnitOfDistance = lapsPerUnitOfDistance;
	}

	public int getRefreshRate() {
		return refreshRate;
	}

	public void setRefreshRate(int refreshRate) {
		this.refreshRate = refreshRate;
	}

	/**
	 * Resets the context from shared preferences.
	 * @param sharedPreferences shared preferences
	 */
	public void reset(SharedPreferences sharedPreferences) {

		//get currently configured value for laps per unit of distance
    	float lapsPerUnitOfDistance = 1;
		
		try {
			lapsPerUnitOfDistance = Float.parseFloat(sharedPreferences.getString(LAPS_PER_UNIT_OF_DISTANCE_KEY, "1"));
		} catch (NumberFormatException e) {
			//use default value
		}
		
		setLapsPerUnitOfDistance(lapsPerUnitOfDistance);
		
		int refreshRate = 10;

		try {
			refreshRate = Integer.parseInt(sharedPreferences.getString(REFRESH_RATE_KEY, "10"));
		} catch (NumberFormatException e) {
			//use default value
		}
		
		setRefreshRate(refreshRate);
	}

}
