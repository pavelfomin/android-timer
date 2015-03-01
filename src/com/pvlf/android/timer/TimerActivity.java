package com.pvlf.android.timer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.pvlf.android.timer.model.Lap;
import com.pvlf.android.timer.model.Run;
import com.pvlf.android.timer.model.RunContext;
import com.pvlf.android.timer.model.Statistic;
import com.pvlf.android.timer.model.json.RunWrapper;
import com.pvlf.android.timer.util.JSONStorageUtility;

/**
 * Main timer activity.
 *
 */
public class TimerActivity extends ListActivity implements OnSharedPreferenceChangeListener {
	private static final String TAG = TimerActivity.class.getName();

	static final String RUN_CONTEXT_PARAMETER = TimerActivity.class.getName() + ".RUN_CONTEXT";
	static final String RUN_PARAMETER = TimerActivity.class.getName() + ".RUN";
	
    private TextView textRunDuration;
	private TextView textLapDuration;
	private TextView textLaps;
	private TextView textDistance;
    
	private RunContext runContext;

    private Run run;

	/**
	 * List data adapter.
	 * An Adapter object acts as a bridge between an {@link AdapterView} and the
	 * underlying data for that view. The Adapter provides access to the data items.
	 * The Adapter is also responsible for making a {@link android.view.View} for
	 * each item in the data set.
	 */
	private ArrayAdapter<Lap> adapter;
    
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

        //keep screen on
        //TODO: parameterize keep screen on? 
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        
		//until preferences activity is invoked for the very first time after the application's install, 
		//the call to sharedPreferences.getString(key, null) will return null even if the android:defaultValue attribute is set in xml.
		//reset default values only on the first ever read
		PreferenceManager.setDefaultValues(getBaseContext(), R.xml.preferences, false);

		//get default shared preferences 
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		//register listener for settings changes
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);

		//initialize the run context
		runContext = new RunContext(sharedPreferences);

		//set the list data adapter
		setListAdapter(createArrayAdapter());
        
		ListView listView = (ListView) findViewById(android.R.id.list);
		//enable long click event for list items
		listView.setLongClickable(true);
		//attach the LongClickListener to the list
		listView.setOnItemLongClickListener(new LapsLongClickListener());
		
        textRunDuration = (TextView) findViewById(R.id.textRunDuration);
        textLapDuration = (TextView) findViewById(R.id.textLapDuration);
        textLaps = (TextView) findViewById(R.id.textLaps);
        textDistance = (TextView) findViewById(R.id.textDistance);

        //set initial values
        updateTimerViewValues(0, 0, 0);
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
				if (run == null || run.isCompleted()) {
					startOrResumeRun();
				} else {
					endLap(currentTimeMillis);
				}
				//start new lap
				run.addLap(new Lap(currentTimeMillis, runContext));
			}
			return true;
		default:
			return super.dispatchKeyEvent(event);
		}
	}

	/**
	 * Starts new or resumes completed run.
	 */
	private void startOrResumeRun() {
		
		if (run == null) {
			//start new run
			run = new Run(runContext);
		} else {
			//resume run
			run.resume();
		}
		//post the timer update
		timerHandler.postDelayed(timerRunnable, 0);
	}

    /**
     * Creates new adapter using favorites data.
     * @return new instance of ArrayAdapter
     */
    private ArrayAdapter<Lap> createArrayAdapter() {
    	
    	List<Lap> items = new ArrayList<Lap>();
    	adapter = new ArrayAdapter<Lap>(this, R.layout.lap, items);
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
		Lap lap = run.endLap(currentTimeMillis, run.getLapsNumber());

		//insert new lap at the beginning of the list
		adapter.insert(lap, 0);

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
    	int numberOfLaps = run.getLapsNumber();
    	
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
		
		updateTimerViewValues(lapDuration, runDuration, numberOfLaps);
    }

	private void updateTimerViewValues(long lapDuration, long runDuration, int numberOfLaps) {

		textRunDuration.setText(Lap.formatDuration(runDuration));
		textLapDuration.setText(Lap.formatDuration(lapDuration));
		textLaps.setText(String.valueOf(numberOfLaps));
		
		DecimalFormat decimalFormat = new DecimalFormat("0.00"); 
		textDistance.setText(decimalFormat.format(numberOfLaps / runContext.getLapsPerUnitOfDistance()));
	}

	/**
	 * Resets the timer.
	 * @param button
	 */
	public void reset(View button) {
		
		if (run != null && run.isCompleted()) {
			
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
					//set initial values
					updateTimerViewValues(0, 0, 0);
				}
			});
			adb.show();
		} else {
			Toast.makeText(this, getString(R.string.msg_runNotComplete), Toast.LENGTH_SHORT).show();
		}
		
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

	/**
	 * Saves current run.
	 * @param button
	 */
	public void save(View button) {

		if (run != null && run.isCompleted()) {
			try {
				JSONStorageUtility.add(this, run, new RunWrapper(), HistoryActivity.HISTORY_FILE_NAME, HistoryActivity.HISTORY);
				Toast.makeText(this, getString(R.string.msg_runSaved), Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
			}
		} else {
			Toast.makeText(this, getString(R.string.msg_runNotComplete), Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Formats the statistic.
	 * @param statistic
	 * @return formatted statistic
	 */
	private String formatStatistic(Statistic statistic) {

		StringBuilder sb = new StringBuilder();
		
		sb.append("Time per lap\n");
		sb.append("Min: ").append(Lap.formatDuration(statistic.getMinimum()));
		sb.append(" Max: ").append(Lap.formatDuration(statistic.getMaximum()));
		sb.append(" Aver: ").append(Lap.formatDuration(statistic.getAverage()));
		
		float lapsPerUnitOfDistance = runContext.getLapsPerUnitOfDistance();

		sb.append("\nTime per mile\n");
		sb.append("Min: ").append(Lap.formatDurationAsMinutesAndSeconds((long) (statistic.getMinimum() * lapsPerUnitOfDistance)));
		sb.append(" Max: ").append(Lap.formatDurationAsMinutesAndSeconds((long) (statistic.getMaximum() * lapsPerUnitOfDistance)));
		sb.append(" Aver: ").append(Lap.formatDurationAsMinutesAndSeconds((long) (statistic.getAverage() * lapsPerUnitOfDistance)));
		
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

		case R.id.history:
			Intent intent = new Intent(this, HistoryActivity.class);
			intent.putExtra(RUN_CONTEXT_PARAMETER, runContext);
			startActivity(intent);
			break;
		}

		//Return false to allow normal menu processing to proceed, true to consume it here.
		return true;
	}

	/**
	 * Called when a shared preference is changed, added, or removed.
	 */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    	Log.v(TAG, "changed setting key="+ key +" values="+ sharedPreferences.getAll());
    	
        if (RunContext.LAPS_PER_UNIT_OF_DISTANCE_KEY.equals(key)) {
    		//re-initialize run context with configured preferences
    		runContext.reset(sharedPreferences);
        }

    }

	/**
	 * Laps Long Click Listener that processes the long click event. 
	 *
	 */
	private class LapsLongClickListener implements AdapterView.OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
			
			AlertDialog.Builder adb = new AlertDialog.Builder(TimerActivity.this);
	        adb.setTitle(R.string.removeLapsEntry);
	        adb.setMessage(getString(R.string.removeLapsEntry));
	        adb.setNegativeButton(getString(R.string.cancel), null);
	        adb.setPositiveButton(getString(R.string.ok), new AlertDialog.OnClickListener() {
	                
				public void onClick(DialogInterface dialog, int choice) {

					// obtain selected lap from adapter
					Lap lap = adapter.getItem(position);

					// remove selected lap from the run
					if (run.removeLap(lap)) {
						// remove selected lap from adapter
						adapter.remove(lap);
						
						// notify adapter of the changes made to force the data refresh
						adapter.notifyDataSetChanged();
						
						//update Timer View if run is complete
						if (run.isCompleted()) {
							updateTimerView();
						}
					} else {
						Log.e(TAG, "Lap wasn't removed at position=" + position);
						
					}
				}
	        });
	        adb.show();
			return true;
		}
	}

}

