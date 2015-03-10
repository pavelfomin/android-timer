package com.pvlf.android.timer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.pvlf.android.timer.model.Run;
import com.pvlf.android.timer.model.Statistic;
import com.pvlf.android.timer.util.FormatUtility;

/**
 * Activity that shows runs statistic.
 */
public class StatisticActivity extends Activity {
	private static final String TAG = StatisticActivity.class.toString();
	
	/**
	 * Called when the activity is starting.
	 */
    @Override
	public void onCreate(Bundle bundle) {

    	super.onCreate(bundle);
		setContentView(R.layout.statistic);
		
		//extract run parameter
		Intent intent = getIntent();
		Run run = (Run) intent.getSerializableExtra(TimerActivity.RUN_PARAMETER);
		if (run != null) {
			updateStatistic(run);
		}
	}

    /**
     * Updates statistic components.
     * @param run
     */
	private void updateStatistic(Run run) {

		Statistic statistic = run.getRunStatistic();
    	Log.v(TAG, "statistic="+ statistic);
		
		TextView textRunDate = (TextView) findViewById(R.id.textRunDate);
		textRunDate.setText(run.getDateFormatted());

		TextView textRunDistance = (TextView) findViewById(R.id.textRunDistance);
		textRunDistance.setText(statistic.getDistanceFormatted());
		
		TextView textRunDuration = (TextView) findViewById(R.id.textRunDuration);
		textRunDuration.setText(run.getDurationFormatted());
		
		TextView textLapTimeMinimum = (TextView) findViewById(R.id.textLapTimeMinimum);
		textLapTimeMinimum.setText(FormatUtility.formatDurationAsMinutesAndSeconds(statistic.getMinimum()));
		
		TextView textLapTimeAverage = (TextView) findViewById(R.id.textLapTimeAverage);
		textLapTimeAverage.setText(FormatUtility.formatDurationAsMinutesAndSeconds(statistic.getAverage()));
		
		TextView textLapTimeMaximum = (TextView) findViewById(R.id.textLapTimeMaximum);
		textLapTimeMaximum.setText(FormatUtility.formatDurationAsMinutesAndSeconds(statistic.getMaximum()));

		TextView textLapSpeedFastest = (TextView) findViewById(R.id.textLapSpeedFastest);
		textLapSpeedFastest.setText(FormatUtility.formatDurationAsMinutesAndSeconds((long) statistic.getFastestSpeed()));
		
		TextView textLapSpeedAverage = (TextView) findViewById(R.id.textLapSpeedAverage);
		textLapSpeedAverage.setText(FormatUtility.formatDurationAsMinutesAndSeconds((long) statistic.getAverageSpeed()));
		
		TextView textLapSpeedSlowest = (TextView) findViewById(R.id.textLapSpeedSlowest);
		textLapSpeedSlowest.setText(FormatUtility.formatDurationAsMinutesAndSeconds((long) statistic.getSlowestSpeed()));
	}
}
