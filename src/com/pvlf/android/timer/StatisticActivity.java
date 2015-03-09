package com.pvlf.android.timer;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.pvlf.android.timer.model.Run;
import com.pvlf.android.timer.model.Statistic;
import com.pvlf.android.timer.util.FormatUtility;

/**
 * Activity that shows runs statistic.
 */
public class StatisticActivity extends Activity {
	private static final String TAG = StatisticActivity.class.toString();
	
	private Run run;

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


	private void updateStatistic(Run run) {

		Statistic statistic = run.getRunStatistic();
		
		StringBuilder sb = new StringBuilder();
		
		sb.append("Time per lap\n");
		sb.append("Min: ").append(FormatUtility.formatDuration(statistic.getMinimum()));
		sb.append(" Max: ").append(FormatUtility.formatDuration(statistic.getMaximum()));
		sb.append(" Aver: ").append(FormatUtility.formatDuration(statistic.getAverage()));
		
		float lapsPerUnitOfDistance = run.getRunContext().getLapsPerUnitOfDistance();

		sb.append("\nTime per mile\n");
		sb.append("Min: ").append(FormatUtility.formatDurationAsMinutesAndSeconds((long) (statistic.getMinimum() * lapsPerUnitOfDistance)));
		sb.append(" Max: ").append(FormatUtility.formatDurationAsMinutesAndSeconds((long) (statistic.getMaximum() * lapsPerUnitOfDistance)));
		sb.append(" Aver: ").append(FormatUtility.formatDurationAsMinutesAndSeconds((long) (statistic.getAverage() * lapsPerUnitOfDistance)));
	}
}
