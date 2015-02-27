package com.pvlf.android.timer.model.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.pvlf.android.timer.model.Lap;
import com.pvlf.android.timer.model.Run;

/**
 * Run JSON wrapper. 
 */
public class RunWrapper implements JSONSerializable<Run> {

	private static final String DESCRIPTION = "description";
	private static final String LAPS = "laps";

	@Override
	public JSONObject toJSON(Run run) throws JSONException {

		LapWrapper lapWrapper = new LapWrapper();
		
		JSONObject json = new JSONObject();
		json.put(DESCRIPTION, run.getDescription());
		
		JSONArray array = new JSONArray();
		for (Lap lap : run.getLaps()) {
			array.put(lapWrapper.toJSON(lap));
		}
		
		json.put(LAPS, array );

		return json;
	}

	@Override
	public Run fromJSON(JSONObject json) throws JSONException {

		Run run = new Run();

		LapWrapper lapWrapper = new LapWrapper();
		
		JSONArray laps = json.getJSONArray(LAPS);
		for (int i = 0; i < laps.length(); i++) {
			Lap lap = lapWrapper.fromJSON(laps.getJSONObject(i));
			run.addLap(lap);
		}
		
		return run;
	}

}
