package com.pvlf.android.timer.model.json;

import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.pvlf.android.timer.model.Lap;
import com.pvlf.android.timer.model.Run;
import com.pvlf.android.timer.model.RunContext;

/**
 * Run JSON wrapper. 
 */
public class RunWrapper implements JSONSerializable<Run> {

	private static final String LAPS = "laps";
	private static final String RUN_CONTEXT = "runContext";

	@Override
	public JSONObject toJSON(Run run) throws JSONException {

		JSONObject json = new JSONObject();

		RunContextWrapper runContextWrapper = new RunContextWrapper();
		json.put(RUN_CONTEXT, runContextWrapper.toJSON(run.getRunContext()));
		
		LapWrapper lapWrapper = new LapWrapper();
		JSONArray array = new JSONArray();
		for (Lap lap : run.getLaps()) {
			array.put(lapWrapper.toJSON(lap));
		}
		
		json.put(LAPS, array );

		return json;
	}

	@Override
	public Run fromJSON(JSONObject json, Serializable context) throws JSONException {

		//deserialize run context
		JSONObject runContextJson = json.getJSONObject(RUN_CONTEXT);
		RunContextWrapper runContextWrapper = new RunContextWrapper();
		RunContext runContext = runContextWrapper.fromJSON(runContextJson, context);

		//create run using the run context
		Run run = new Run(runContext);

		//deserialize laps using the run's context
		LapWrapper lapWrapper = new LapWrapper();
		JSONArray laps = json.getJSONArray(LAPS);
		for (int i = 0; i < laps.length(); i++) {
			Lap lap = lapWrapper.fromJSON(laps.getJSONObject(i), runContext);
			//laps are in reversed order already
			//add to the end to retain the order
			run.getLaps().add(lap);
		}
		
		run.setSaved(true);
		
		return run;
	}

}
