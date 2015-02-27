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
	private ArrayAdapter<String> adapter;

	/**
	 * Called when the activity is starting.
	 */
    @Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.history);
		
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
    private ArrayAdapter<String> createArrayAdapter() {

    	List<String> items = new ArrayList<String>();
    	List<Run> list;
		try {
			list = JSONStorageUtility.getList(this, new RunWrapper(), HISTORY_FILE_NAME, HISTORY);
	    	for (Run run : list) {
	    		items.add(run.getDescription());
			}
		} catch (Exception e) {
			Log.e(TAG, "Failed to get the history", e);
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}
    	adapter = new ArrayAdapter<String>(this, R.layout.run, items);
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
		//TODO: String location = adapter.getItem(position);
	    //TODO: intent.putExtra(TimerActivity.FAVORITE_LOCATION_PARAMETER, location);
	    startActivity(intent);
	}

	/**
	 * Removes all history entries. This method is referenced in the button layout definition.
	 * @param button
	 */
	public void removeAll(View button) {
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle(R.string.removeAllHistoryEntries);
        adb.setMessage(getString(R.string.removeAllHistoryEntries));
        adb.setNegativeButton(getString(R.string.cancel), null);
        adb.setPositiveButton(getString(R.string.ok), new AlertDialog.OnClickListener() {
                public void onClick(DialogInterface dialog, int choice) {
                	//remove all items from adapter
                	adapter.clear();
                	//remove all items from from storage
                	JSONStorageUtility.removeAllEntities(HistoryActivity.this, HISTORY_FILE_NAME);
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
	        adb.setTitle(R.string.removeHistoryEntry);
	        adb.setMessage(getString(R.string.removeHistoryEntry));
	        adb.setNegativeButton(getString(R.string.cancel), null);
	        adb.setPositiveButton(getString(R.string.ok), new AlertDialog.OnClickListener() {
	                public void onClick(DialogInterface dialog, int choice) {
	                	//obtain selected run from adapter
	                	String item = adapter.getItem(position);
	                	//remove selected run from adapter
	                	adapter.remove(item);
	                	//Run run = new Run(); //TODO: how can this one be created?
						//remove selected run from storage
						//TODO: JSONStorageUtility.remove(HistoryActivity.this, run , new RunWrapper(), HISTORY_FILE_NAME, HISTORY);
						//notify adapter of the changes made to force the data refresh
	                    adapter.notifyDataSetChanged();
	                }
	        });
	        adb.show();
			return true;
		}
	}
}
