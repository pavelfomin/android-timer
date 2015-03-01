package com.pvlf.android.timer.model.json;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * JSON serializable interface.
 * @param <T>
 */
public interface JSONSerializable<T> {

	/**
	 * Returns an instance of JSONObject created from this instance.
	 * @param entity entity
	 * @return instance of JSONObject
	 * @throws JSONException
	 */
	public JSONObject toJSON(T entity) throws JSONException;
	
	/**
	 * Returns an instance of the entity class created from json.
	 * @param json json
	 * @param context context
	 * @return instance of the entity class
	 * @throws JSONException
	 */
	public T fromJSON(JSONObject json, Serializable context) throws JSONException;
}
