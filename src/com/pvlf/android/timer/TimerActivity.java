package com.pvlf.android.timer;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.pvlf.android.timer.model.Lap;
import com.pvlf.android.timer.model.Run;
import com.pvlf.android.timer.model.Statistic;

/**
 * Main timer activity.
 *
 */
public class TimerActivity extends ListActivity {
	private static final String TAG = TimerActivity.class.getName();

	private static final String LAPS_PER_MILE_KEY = "lapsPerMile";

    private TextView timerTextView;
    
    private Run run;

	/**
	 * List data adapter.
	 * An Adapter object acts as a bridge between an {@link AdapterView} and the
	 * underlying data for that view. The Adapter provides access to the data items.
	 * The Adapter is also responsible for making a {@link android.view.View} for
	 * each item in the data set.
	 */
	private ArrayAdapter<String> adapter;
    
	/**
	 * Handler to use instead of a timer. 
	 */
    private Handler timerHandler = new Handler();

    private Runnable timerRunnable = new Runnable() {

        @Override
        public void run() {
        	
        	updateTimerView();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {

    	super.onCreate(savedInstanceState);
        setContentView(R.layout.timer);

		//until preferences activity is invoked for the very first time after the application's install, 
		//the call to sharedPreferences.getString(key, null) will return null even if the android:defaultValue attribute is set in xml.
		//reset default values only on the first ever read
		PreferenceManager.setDefaultValues(getBaseContext(), R.xml.preferences, false);

		//ListView listView = (ListView) findViewById(android.R.id.list);
		//enable long click event for list items
		//listView.setLongClickable(true);
		//attach the LongClickListener to the list
		//listView.setOnItemLongClickListener(new FavoritesLongClickListener());

		//initialize list from saved json file
		setListAdapter(createArrayAdapter());
        
        timerTextView = (TextView) findViewById(R.id.textDuration);
    }

    @Override
    public void onPause() {

    	super.onPause();
        //timerHandler.removeCallbacks(timerRunnable);
    }

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		
		int action = event.getAction();
		int keyCode = event.getKeyCode();

		long currentTimeMillis = System.currentTimeMillis();
		
		switch (keyCode) {
		
		case KeyEvent.KEYCODE_VOLUME_UP:
			if (action == KeyEvent.ACTION_DOWN) {
                if (run != null && (! run.isCompleted())) {
                	//end run
					endRun(currentTimeMillis);
					
					//remove any pending posts from the message queue
					timerHandler.removeCallbacks(timerRunnable);
					
					//update timer view one more time
					updateTimerView();
					
					//show statistic
					statistic(null);
				} else {
					Toast.makeText(this, getString(R.string.msg_timerNotActive), Toast.LENGTH_SHORT).show();
				}
			}
			return true;
		
		case KeyEvent.KEYCODE_VOLUME_DOWN:

			if (action == KeyEvent.ACTION_DOWN) {
				if (run == null) {
					//start new run
					run = new Run();
					//post the timer update
	                timerHandler.postDelayed(timerRunnable, 0);
				} else if(run.isCompleted()) {
					//resume run
					run.resume();
					//post the timer update
	                timerHandler.postDelayed(timerRunnable, 0);
				} else {
					endLap(currentTimeMillis);
				}
				//start new lap
				run.addLap(new Lap(currentTimeMillis));
				//Log.d(TAG, "dispatchKeyEvent run="+ run);
			}
			return true;
		default:
			return super.dispatchKeyEvent(event);
		}
	}

    /**
     * Creates new adapter using favorites data.
     * @return new instance of ArrayAdapter
     */
    private ArrayAdapter<String> createArrayAdapter() {
    	
    	List<String> items = new ArrayList<String>();
    	adapter = new ArrayAdapter<String>(this, R.layout.lap, items);
    	Log.d(TAG, "Created new adapter="+ adapter );
		return adapter;
    }

	private void endRun(long currentTimeMillis) {
		
		//end the current lap
		endLap(currentTimeMillis);

		//finalize the run
		run.end();
	}
	
	private void endLap(long currentTimeMillis) {

		//end the current lap
		Lap lap = run.endLap(currentTimeMillis);

		//insert new lap at the beginning of the lap
		adapter.insert(String.format("%d: %s", run.getLaps().size(), lap.toFormattedString()), 0);
		//notify adapter of the changes made to force the data refresh
		adapter.notifyDataSetChanged();
	}

    /**
     * Updates the timer view.
     */
    private void updateTimerView() {

    	long lapDuration;
    	long runDuration = run.getDuration();
    	Lap lap = run.getCurrentLap();
    	int numberOfLaps = run.getLaps().size();
    	
    	if (run.isCompleted()) {
			lapDuration = lap.getDuration();
		} else {
			long currentTimeMillis = System.currentTimeMillis();
			lapDuration = currentTimeMillis - lap.getStart();
			runDuration += lapDuration;
			numberOfLaps--;
			//post another update
			timerHandler.postDelayed(timerRunnable, 100);
		}
		
		timerTextView.setText(String.format("%s laps:%d %s", 
        		Lap.formatDuration(runDuration), numberOfLaps, Lap.formatDuration(lapDuration)));
    }

	/**
	 * Resets the timer.
	 * @param button
	 */
	public void reset(View button) {
		
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(R.string.resetTimer);
        adb.setMessage(getString(R.string.msg_resetTimer));
        adb.setNegativeButton(getString(R.string.cancel), null);
        adb.setPositiveButton(getString(R.string.ok), new AlertDialog.OnClickListener() {
                public void onClick(DialogInterface dialog, int choice) {
                	//remove all items from adapter
                	adapter.clear();
                	//notify adapter of the changes made to force the data refresh
                    adapter.notifyDataSetChanged();
                    //reset run
                    run = null;
                    timerTextView.setText("");
                }
        });
        adb.show();
	}

	/**
	 * Shows run's statistic.
	 * @param button
	 */
	public void statistic(View button) {

		if (run != null) {
			Statistic statistic = run.getRunStatistic();
			
			if (statistic != null) {
				String message = formatStatistic(statistic);
				
				AlertDialog.Builder adb = new AlertDialog.Builder(this);
				adb.setTitle(R.string.statistic);
				adb.setMessage(message);
				adb.setPositiveButton(getString(R.string.ok), null);
				adb.show();
				
				Log.d(TAG, "Run statistic: "+ message);
			}
			
		}
	}


	private String formatStatistic(Statistic statistic) {

		//get default shared preferences 
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		//get currently configured value for laps per mile
    	float lapsPerMile = 1;
		
		try {
			lapsPerMile = Float.parseFloat(sharedPreferences.getString(LAPS_PER_MILE_KEY, "1"));
		} catch (NumberFormatException e) {
			Log.w(TAG, "Run statistic: laps per mile value can't be parsed", e);
		}
    	
		StringBuilder sb = new StringBuilder();
		sb.append("Time per lap\n");
		sb.append("Min: ").append(Lap.formatDuration(statistic.getMinimum()));
		sb.append(" Max: ").append(Lap.formatDuration(statistic.getMaximum()));
		sb.append(" Aver: ").append(Lap.formatDuration(statistic.getAverage()));
		sb.append("\nTime per mile\n");
		sb.append("Min: ").append(Lap.formatDuration((long) (statistic.getMinimum() * lapsPerMile)));
		sb.append(" Max: ").append(Lap.formatDuration((long) (statistic.getMaximum() * lapsPerMile)));
		sb.append(" Aver: ").append(Lap.formatDuration((long) (statistic.getAverage() * lapsPerMile)));
		
		return sb.toString();
	}
	
	/**
	 * Initialize the contents of the Activity's standard options menu.
	 * The content of the menu is defined in /res/menu/main.xml.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Process menu options selection.
	 * This method is called whenever an item in your options menu is selected.
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.settings:
			startActivity(new Intent(this, SettingsActivity.class));
			break;
		}

		//Return false to allow normal menu processing to proceed, true to consume it here.
		return true;
	}

}

