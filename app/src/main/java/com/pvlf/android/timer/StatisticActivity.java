package com.pvlf.android.timer;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.pvlf.android.timer.model.Run;
import com.pvlf.android.timer.model.Statistic;
import com.pvlf.android.timer.util.FormatUtility;

/**
 * Activity that shows runs statistic.
 */
public class StatisticActivity extends Activity {
	private static final String TAG = StatisticActivity.class.toString();
	
	private TextView textLapSpeedSlowest;
	private TextView textLapSpeedFastest;
	private TextView textLapTimeMaximum;
	private TextView textLapTimeMinimum;
	
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
		
		textLapTimeMinimum = (TextView) findViewById(R.id.textLapTimeMinimum);
		textLapTimeMinimum.setText(FormatUtility.formatDurationAsMinutesAndSeconds(statistic.getMinimum()));
		
		TextView textLapTimeAverage = (TextView) findViewById(R.id.textLapTimeAverage);
		textLapTimeAverage.setText(FormatUtility.formatDurationAsMinutesAndSeconds(statistic.getAverage()));
		
		textLapTimeMaximum = (TextView) findViewById(R.id.textLapTimeMaximum);
		textLapTimeMaximum.setText(FormatUtility.formatDurationAsMinutesAndSeconds(statistic.getMaximum()));

		textLapSpeedFastest = (TextView) findViewById(R.id.textLapSpeedFastest);
		textLapSpeedFastest.setText(FormatUtility.formatDurationAsMinutesAndSeconds((long) statistic.getFastestSpeed()));
		
		TextView textLapSpeedAverage = (TextView) findViewById(R.id.textLapSpeedAverage);
		textLapSpeedAverage.setText(FormatUtility.formatDurationAsMinutesAndSeconds((long) statistic.getAverageSpeed()));
		
		textLapSpeedSlowest = (TextView) findViewById(R.id.textLapSpeedSlowest);
		textLapSpeedSlowest.setText(FormatUtility.formatDurationAsMinutesAndSeconds((long) statistic.getSlowestSpeed()));
	
		//force widgets removal in PORTRAIT mode
		onConfigurationChanged(getResources().getConfiguration());
	}

	/**
	 * Removes widgets to free more space in the PORTRAIT mode and restores them in LANDSCAPE mode.
 	 */
	@Override
	public void onConfigurationChanged(Configuration configuration) {

		int visibility = (configuration.orientation == Configuration.ORIENTATION_PORTRAIT ? View.GONE : View.VISIBLE);

		textLapTimeMinimum.setVisibility(visibility);
    	textLapTimeMaximum.setVisibility(visibility);
    	textLapSpeedFastest.setVisibility(visibility);
    	textLapSpeedSlowest.setVisibility(visibility);
    	
    	super.onConfigurationChanged(configuration);
	}
}
