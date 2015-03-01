package com.pvlf.android.timer.model.json;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

import com.pvlf.android.timer.model.Lap;
import com.pvlf.android.timer.model.RunContext;

/**
 * Lap JSON wrapper. 
 */
public class LapWrapper implements JSONSerializable<Lap> {

	private static final String START = "start";
	private static final String DURATION = "duration";
	private static final String POSITION = "position";

	@Override
	public JSONObject toJSON(Lap lap) throws JSONException {

		JSONObject json = new JSONObject();

		json.put(START, lap.getStart());
		json.put(DURATION, lap.getDuration());
		json.put(POSITION, lap.getPosition());
		
		return json;
	}

	@Override
	public Lap fromJSON(JSONObject json, Serializable context) throws JSONException {

		Lap lap = new Lap(json.getLong(START), (RunContext) context, json.getInt(POSITION), json.getLong(DURATION));
		return lap;
	}
}
