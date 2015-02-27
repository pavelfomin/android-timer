package com.pvlf.android.timer.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.pvlf.android.timer.model.json.JSONSerializable;

/**
 * JSON storage history utility that encapsulates entities read/write functionality.
 * Marked as abstract to emphasize the static usage only i.e. can't be instantiated.
 */
public abstract class JSONStorageUtility {
	private static final String TAG = JSONStorageUtility.class.toString();

    /**
     * Reads JSON entities.
     * @param context context
     * @param jsonSerializable instance of JSONSerializable
     * @param fileName file name
     * @param jsonElement top json element
     * @return list of entities
     * @throws Exception 
     */
	public static <T> List<T> getList(Context context, JSONSerializable<T> jsonSerializable, String fileName, String jsonElement) throws Exception {

		List<T> entities = new ArrayList<T>();
		File file = new File(context.getFilesDir(), fileName);
		try {
			String data = StorageUtility.read(file);
			JSONObject json = new JSONObject(data);
			JSONArray array = json.getJSONArray(jsonElement);
			for (int i = 0; i < array.length(); i++) {
				T enity = jsonSerializable.fromJSON(array.getJSONObject(i));
				entities.add(enity);
			}
		} catch (FileNotFoundException e) {
			//ignore this one - no entities stored
		} catch (Exception e) {
			throw new  Exception("Failed to obtain entities from file="+ file, e);
		}

		return entities;
	}

	/**
	 * Adds new entity to the list and stores the file.
     * @param context context
     * @param entity new entity
     * @param jsonSerializable instance of JSONSerializable
     * @param fileName file name
     * @param jsonElement top json element
	 * @throws Exception 
	 */
	public static <T> void add(Context context, T entity, JSONSerializable<T> jsonSerializable, String fileName, String jsonElement) throws Exception {

		File file = new File(context.getFilesDir(), fileName);
		Log.d(TAG, "Writing the entity="+ entity +" into file="+ file.getAbsolutePath());
		
		String data = null;
		//read existing entities
		try {
			data = StorageUtility.read(file);
		} catch (FileNotFoundException e) {
			//ignore this one - no entities stored
		} catch (Exception e) {
			throw new Exception("Failed to obtain entities from file="+ file, e);
		}
		
		try {
			JSONObject json = null;
			if (data == null) {
				//no history yet, create new json array
				json = new JSONObject();
				JSONArray history = new JSONArray();
				json.put(jsonElement, history);
			} else {
				json = new JSONObject(data);
			}
			//get the list of entities
			JSONArray history = json.getJSONArray(jsonElement);
			//add new entity to the end of the list
			history.put(jsonSerializable.toJSON(entity));
			//save changes
			StorageUtility.save(file, json.toString());
		} catch (Exception e) {
			Log.e(TAG, "Failed to save new entity="+ entity, e);
		}
	}

	/**
	 * Removes entities file.
	 * @param context
	 * @param fileName
	 * @throws Exception 
	 */
	public static void removeAllEntities(Context context, String fileName) throws Exception {
		File file = new File(context.getFilesDir(), fileName);
		boolean deleted = file.delete();
		if (! deleted) {
			throw new Exception("Failed to remove file="+ fileName);
		}
	}
	
	/**
	 * Removes an entity from the list and stores the file.
	 * @param context
     * @param position position of an entity to remove
     * @param fileName file name
     * @param jsonElement top json element
	 * @throws Exception 
     */
	public static <T> void remove(Context context, int position, String fileName, String jsonElement) throws Exception {
		File file = new File(context.getFilesDir(), fileName);
		try {
			String data = StorageUtility.read(file);
			JSONObject json = new JSONObject(data);
			//current list of entities
			JSONArray entities = json.getJSONArray(jsonElement);
			//new list of entities, w/out entityToRemove
			JSONArray newEntities = new JSONArray();
			for (int i = 0; i < entities.length(); i++) {
				if (i != position) {
					JSONObject entity = entities.getJSONObject(i);
					newEntities.put(entity);
				}
			}
			//set new entity in JSON
			json.put(jsonElement, newEntities);
			//save changes
			StorageUtility.save(file, json.toString());
		} catch (FileNotFoundException e) {
			//ignore this one - no entities stored
		} catch (Exception e) {
			throw new Exception("Failed to remove entity from file="+ file, e);
		}
		
	}
}
