package com.pvlf.android.timer.model.json;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.pvlf.android.timer.model.RunContext;

/**
 * Lap JSON wrapper. 
 */
public class RunContextWrapper implements JSONSerializable<RunContext> {

	private static final String LAPS_PER_UNIT_OF_DISTANCE = "lapsPerUnitOfDistance";

	@Override
	public JSONObject toJSON(RunContext runContext) throws JSONException {

		JSONObject json = new JSONObject();

		json.put(LAPS_PER_UNIT_OF_DISTANCE, runContext.getLapsPerUnitOfDistance());
		
		return json;
	}

	@Override
	public RunContext fromJSON(JSONObject json, Serializable context) throws JSONException {

		RunContext runContext = new RunContext();
		runContext.setLapsPerUnitOfDistance((float) json.getDouble(LAPS_PER_UNIT_OF_DISTANCE));

		return runContext;
	}
}
