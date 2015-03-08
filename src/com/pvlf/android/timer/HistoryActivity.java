package com.pvlf.android.timer;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.pvlf.android.timer.model.Run;
import com.pvlf.android.timer.model.RunContext;
import com.pvlf.android.timer.model.json.RunWrapper;
import com.pvlf.android.timer.util.JSONStorageUtility;

/**
 * Activity that shows runs history.
 */
public class HistoryActivity extends ListActivity {
	private static final String TAG = HistoryActivity.class.toString();

	/**
	 * JSON KEY for run history array.
	 */
	static final String HISTORY = "history";
	
	/**
	 * Name of the run history file.
	 */
    static final String HISTORY_FILE_NAME = "history.json";

	/**
	 * List data adapter.
	 * An Adapter object acts as a bridge between an {@link AdapterView} and the
	 * underlying data for that view. The Adapter provides access to the data items.
	 * The Adapter is also responsible for making a {@link android.view.View} for
	 * each item in the data set.
	 */
	private ArrayAdapter<Run> adapter;

	private RunContext runContext;

	/**
	 * Called when the activity is starting.
	 */
    @Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.history);
		
		Intent intent = getIntent();
		runContext = (RunContext) intent.getSerializableExtra(TimerActivity.RUN_CONTEXT_PARAMETER);
		
		ListView listView = (ListView) findViewById(android.R.id.list);
		//enable long click event for list items
		listView.setLongClickable(true);
		//attach the LongClickListener to the list
		listView.setOnItemLongClickListener(new HistoryLongClickListener());

		//initialize list from saved json file
		setListAdapter(createArrayAdapter());
	}

    /**
     * Creates new adapter using history data.
     * @return new instance of ArrayAdapter
     */
    private ArrayAdapter<Run> createArrayAdapter() {

    	List<Run> items = new ArrayList<Run>();
    	List<Run> list;
		try {
			list = JSONStorageUtility.getList(this, new RunWrapper(), HISTORY_FILE_NAME, HISTORY, runContext);
	    	for (Run run : list) {
	    		items.add(run);
			}
		} catch (Exception e) {
			Log.e(TAG, "Failed to get the history", e);
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
    	adapter = new ArrayAdapter<Run>(this, R.layout.run, items);
    	Log.d(TAG, "Created new adapter="+ adapter );
		return adapter;
    }
    
    /**
     * Handle run selection.
     */
	@Override
	protected void onListItemClick(ListView l, View v, final int position, long id) {

		Intent intent = new Intent(this, TimerActivity.class);
		//when MainActivity is launched, all tasks on top of it are cleared so that MainActivity is top.
		//A new back stack is created with MainActivity at the top, and using singleTop ensures that MainActivity is created only once
		//since MainActivity is now on top due to FLAG_ACTIVITY_CLEAR_TOP
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    intent.putExtra(TimerActivity.RUN_PARAMETER, adapter.getItem(position));
	    startActivity(intent);
	}

	/**
	 * Removes all history entries. This method is referenced in the button layout definition.
	 * @param button
	 */
	public void removeAll(View button) {
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(R.string.msg_removeAllHistoryEntries);
        adb.setNegativeButton(getString(R.string.cancel), null);
        adb.setPositiveButton(getString(R.string.ok), new AlertDialog.OnClickListener() {
                public void onClick(DialogInterface dialog, int choice) {
                	//remove all items from adapter
                	adapter.clear();
                	//remove all items from from storage
                	try {
						JSONStorageUtility.removeAllEntities(HistoryActivity.this, HISTORY_FILE_NAME);
					} catch (Exception e) {
						Log.e(TAG, e.getMessage(), e);
						Toast.makeText(HistoryActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
					}
                	//notify adapter of the changes made to force the data refresh
                    adapter.notifyDataSetChanged();
                }
        });
        adb.show();
	}

	/**
	 * History Long Click Listener that processes the long click event. 
	 *
	 */
	private class HistoryLongClickListener implements AdapterView.OnItemLongClickListener {

		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
			AlertDialog.Builder adb = new AlertDialog.Builder(HistoryActivity.this);
	        adb.setTitle(R.string.msg_removeHistoryEntry);
	        adb.setNegativeButton(getString(R.string.cancel), null);
	        adb.setPositiveButton(getString(R.string.ok), new AlertDialog.OnClickListener() {
	                public void onClick(DialogInterface dialog, int choice) {
	                	//obtain selected run from adapter
	                	Run run = adapter.getItem(position);
	                	//remove selected run from adapter
	                	adapter.remove(run);
						//remove selected run from storage
						try {
							JSONStorageUtility.remove(HistoryActivity.this, position, HISTORY_FILE_NAME, HISTORY);
						} catch (Exception e) {
							Log.e(TAG, e.getMessage(), e);
							Toast.makeText(HistoryActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
						}
						//notify adapter of the changes made to force the data refresh
	                    adapter.notifyDataSetChanged();
	                }
	        });
	        adb.show();
			return true;
		}
	}
}
