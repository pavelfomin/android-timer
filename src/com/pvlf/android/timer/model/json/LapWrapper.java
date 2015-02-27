package com.pvlf.android.timer.model.json;

import org.json.JSONException;
import org.json.JSONObject;

import com.pvlf.android.timer.model.Lap;

/**
 * Lap JSON wrapper. 
 */
public class LapWrapper implements JSONSerializable<Lap> {

	private static final String START = "start";
	private static final String DURATION = "duration";

	@Override
	public JSONObject toJSON(Lap lap) throws JSONException {

		JSONObject json = new JSONObject();

		json.put(START, lap.getStart());
		json.put(DURATION, lap.getDuration());
		
		return json;
	}

	@Override
	public Lap fromJSON(JSONObject json) throws JSONException {

		Lap lap = new Lap(json.getLong(START));
		lap.setDuration(json.getLong(DURATION));

		return lap;
	}
}
