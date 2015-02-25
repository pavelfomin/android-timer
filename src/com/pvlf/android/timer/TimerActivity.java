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

import com.pvlf.android.timer.model.Lap;
import com.pvlf.android.timer.model.Run;

/**
 * Main timer activity.
 *
 */
public class TimerActivity extends ListActivity {
	private static final String TAG = TimerActivity.class.getName();

    private TextView timerTextView;
    
    private Run run;
    private Lap lap;

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

    	long runDuration;
    	long lapDuration;

    	if (run.isCompleted()) {
			runDuration = run.getDuration();
			lapDuration = lap.getDuration();
			//Log.d(TAG, String.format("run duration: %d laps: %d", runDuration, run.getLapsDuration()));
		} else {
			long currentTimeMillis = System.currentTimeMillis();
			runDuration = currentTimeMillis - run.getStart();
			lapDuration = currentTimeMillis - lap.getStart();
			//post another update
			timerHandler.postDelayed(timerRunnable, 100);
		}
		
		timerTextView.setText(String.format("%s laps:%d %s", 
        		Lap.formatDuration(runDuration), run.getLaps().size(), Lap.formatDuration(lapDuration)));
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
                if (run != null) {
                	//end run
					endRun(currentTimeMillis);
					
					//remove any pending posts from the message queue
					timerHandler.removeCallbacks(timerRunnable);
					
					//update timer view one more time
					updateTimerView();
				}
			}
			return true;
		
		case KeyEvent.KEYCODE_VOLUME_DOWN:

			if (action == KeyEvent.ACTION_DOWN) {
				if (run == null) {
					//start new run
					run = new Run(currentTimeMillis);
					//post to handler
	                timerHandler.postDelayed(timerRunnable, 0);
				} else {
					endLap(currentTimeMillis);
				}
				//start new lap
				lap = new Lap(currentTimeMillis);
				Log.d(TAG, "dispatchKeyEvent run="+ run);
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
		run.end(currentTimeMillis);
	}
	
	private void endLap(long currentTimeMillis) {

		//end the current lap
		lap.end(currentTimeMillis);
		//add it to the list of laps
		run.addLap(lap);
		//insert new lap at the beginning of the lap
		adapter.insert(String.format("%d: %s", run.getLaps().size(), lap.toFormattedString()), 0);
		//notify adapter of the changes made to force the data refresh
		adapter.notifyDataSetChanged();
	}

	/**
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

}

