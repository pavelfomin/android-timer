package com.pvlf.android.timer;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.pvlf.android.timer.R;
import com.pvlf.android.timer.model.Lap;
import com.pvlf.android.timer.model.Run;
import com.pvlf.android.timer.model.Statistic;

/**
 * Main timer activity.
 *
 */
public class TimerActivity extends ListActivity {
	private static final String TAG = TimerActivity.class.getName();

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

    
    @Override
    public void onCreate(Bundle savedInstanceState) {

    	super.onCreate(savedInstanceState);
        setContentView(R.layout.timer);
        
		//ListView listView = (ListView) findViewById(android.R.id.list);
		//enable long click event for list items
		//listView.setLongClickable(true);
		//attach the LongClickListener to the list
		//listView.setOnItemLongClickListener(new FavoritesLongClickListener());

		//initialize list from saved json file
		setListAdapter(createArrayAdapter());
        
        timerTextView = (TextView) findViewById(R.id.textDuration);
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

    @Override
    public void onPause() {

    	super.onPause();
        //TODO: not sure about removeCallbacks on pause
        timerHandler.removeCallbacks(timerRunnable);
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
				String message = String.format("Min: %s Max: %s Aver: %s", 
						Lap.formatDuration(statistic.getMinimum()), 
						Lap.formatDuration(statistic.getMaximum()), 
						Lap.formatDuration(statistic.getAverage()));
				
				AlertDialog.Builder adb = new AlertDialog.Builder(this);
				adb.setTitle(R.string.statistic);
				adb.setMessage(message);
				adb.setPositiveButton(getString(R.string.ok), null);
				adb.show();
				
				Log.d(TAG, "Run statistic: "+ message);
			}
			
		}
	}

}

