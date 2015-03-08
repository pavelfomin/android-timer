package com.pvlf.android.timer;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.media.AudioManager;
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
import com.pvlf.android.timer.util.AlertDialogUtility;
import com.pvlf.android.timer.util.FormatUtility;
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

	private AudioManager audioManager;

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

	/**
	 * Called when activity is started with intent using Intent.FLAG_ACTIVITY_CLEAR_TOP.
	 * See {@link MainActivity#onListItemClick} for more details.
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		
		super.onNewIntent(intent);
		Log.d(TAG, "onNewIntent intent="+ intent);

		//initialize timer with an instance of the Run passed in
		Run run = (Run) intent.getSerializableExtra(RUN_PARAMETER);
		if (run != null) {
			this.run = run;

			adapter.clear();
			for (Lap lap : run.getLaps()) {
				//laps are in reversed order already
				//add to the end to retain the correct order
				adapter.add(lap);
			}
			
			updateTimerView();

			//get default shared preferences 
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

			//reset run context with the current settings
			run.getRunContext().reset(sharedPreferences);
		}
	}

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

        audioManager = (AudioManager)getSystemService(AUDIO_SERVICE);
    }

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		
		int action = event.getAction();
		int keyCode = event.getKeyCode();

		long currentTimeMillis = System.currentTimeMillis();
		
		switch (keyCode) {
		
		case KeyEvent.KEYCODE_VOLUME_UP:
			if (action == KeyEvent.ACTION_DOWN) {
                endRun(currentTimeMillis);
			}
			
			playSound();
			return true;
		
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			if (action == KeyEvent.ACTION_DOWN) {
				startLap(currentTimeMillis);
			}

			playSound();
			return true;

		default:
			return super.dispatchKeyEvent(event);
		}
	}

	/**
	 * Plays the sound.
	 */
	private void playSound() {
		
		audioManager.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD, 2);
	}

	/**
	 * Ends run.
	 * @param currentTimeMillis
	 */
	private void endRun(long currentTimeMillis) {
		
		if (run != null && (! run.isCompleted())) {
			//end run by ending the current lap
			endLap(currentTimeMillis);
			
			//remove any pending posts from the message queue
			timerHandler.removeCallbacks(timerRunnable);
			
			//update timer view one more time
			updateTimerView();
		} else {
			Toast.makeText(this, getString(R.string.msg_timerNotActive), Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Starts new lap. Creates new or resumes an existing run if needed.
	 */
	private void startLap(long currentTimeMillis) {
		
		boolean startOrResume = (run == null || run.isCompleted());  
		if (run == null) {
			//start new run
			run = new Run(runContext);
		} else if(! run.isCompleted()) {
			//end current lap
			endLap(currentTimeMillis);
		} else {
			//resume run
			run.setSaved(false);
		}

		//start new lap
		run.addLap(new Lap(currentTimeMillis, runContext, run.getLapsNumber() + 1));
		
		if (startOrResume) {
			//post the initial timer update
			timerHandler.postDelayed(timerRunnable, 0);
		}
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

	private void endLap(long currentTimeMillis) {

		//end the current lap
		Lap lap = run.endLap(currentTimeMillis);

		//insert new lap at the beginning of the list
		adapter.insert(lap, 0);
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
			timerHandler.postDelayed(timerRunnable, 1000 / runContext.getRefreshRate());
		}
		
		updateTimerViewValues(lapDuration, runDuration, numberOfLaps);
    }

	private void updateTimerViewValues(long lapDuration, long runDuration, int numberOfLaps) {

		textRunDuration.setText(FormatUtility.formatDuration(runDuration));
		textLapDuration.setText(FormatUtility.formatDuration(lapDuration));
		textLaps.setText(String.valueOf(numberOfLaps));
		
		textDistance.setText(FormatUtility.formatDistance(numberOfLaps / runContext.getLapsPerUnitOfDistance()));
	}

	/**
	 * Resets the timer.
	 * @param button
	 */
	public void reset(View button) {
		
		if (run != null && run.isCompleted()) {
			
			AlertDialogUtility.showAlertDialog(this, R.string.msg_resetTimer, new AlertDialog.OnClickListener() {

				public void onClick(DialogInterface dialog, int choice) {
					// remove all items from adapter
					adapter.clear();
					// reset run
					run = null;
					// set initial values
					updateTimerViewValues(0, 0, 0);
				}
			});
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
				AlertDialogUtility.showAlertDialog(this, R.string.statistic, null); 
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
				run.setSaved(true);
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
		sb.append("Min: ").append(FormatUtility.formatDuration(statistic.getMinimum()));
		sb.append(" Max: ").append(FormatUtility.formatDuration(statistic.getMaximum()));
		sb.append(" Aver: ").append(FormatUtility.formatDuration(statistic.getAverage()));
		
		float lapsPerUnitOfDistance = runContext.getLapsPerUnitOfDistance();

		sb.append("\nTime per mile\n");
		sb.append("Min: ").append(FormatUtility.formatDurationAsMinutesAndSeconds((long) (statistic.getMinimum() * lapsPerUnitOfDistance)));
		sb.append(" Max: ").append(FormatUtility.formatDurationAsMinutesAndSeconds((long) (statistic.getMaximum() * lapsPerUnitOfDistance)));
		sb.append(" Aver: ").append(FormatUtility.formatDurationAsMinutesAndSeconds((long) (statistic.getAverage() * lapsPerUnitOfDistance)));
		
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
    	
		//re-initialize run context with configured preferences
		runContext.reset(sharedPreferences);
    }

    /**
     * Confirm exit if the current run is not saved.
     */
    @Override
	public void onBackPressed() {

		//check if run is saved.
    	if (run == null || run.isSaved()) {
    		super.onBackPressed();
    		return;
		}

    	//if not saved then ask for a confirmation
		Builder adb = new AlertDialog.Builder(this);
		adb.setTitle(getString(R.string.msg_confirmExit));
		adb.setCancelable(false);
		adb.setNegativeButton(getString(R.string.cancel), null);
		adb.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int id) {

				TimerActivity.super.onBackPressed();
			}
		});
		adb.show();
	}
    
	/**
	 * Laps Long Click Listener that processes the long click event. 
	 *
	 */
	private class LapsLongClickListener implements AdapterView.OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
			
			AlertDialogUtility.showAlertDialog(TimerActivity.this, R.string.msg_removeLapsEntry,
					new AlertDialog.OnClickListener() {

						public void onClick(DialogInterface dialog, int choice) {

							// obtain selected lap from adapter
							Lap lap = adapter.getItem(position);

							// remove selected lap from the run
							if (run.removeLap(lap)) {
								// remove selected lap from adapter
								adapter.remove(lap);

								// update Timer View if run is complete
								if (run.isCompleted()) {
									updateTimerView();
								}
							} else {
								Log.e(TAG, "Lap wasn't removed at position=" + position);

							}
						}
					});
			return true;
		}
	}

}

