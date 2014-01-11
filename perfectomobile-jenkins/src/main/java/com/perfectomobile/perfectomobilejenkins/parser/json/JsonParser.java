package com.perfectomobile.perfectomobilejenkins.parser.json;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonParser {

	private static JSONParser parser = new JSONParser();
	private static JsonParser instance = null;

	protected JsonParser() {
		// Exists only to defeat instantiation.
	}

	public static JsonParser getInstance() {
		if (instance == null) {
			instance = new JsonParser();
		}
		return instance;
	}
	
	public String getElement(String jsonStringObject, String element) throws ParseException{
		
		String resultElement = null;
		
		Object obj = parser.parse(jsonStringObject);
		
		JSONObject jsonObject = (JSONObject) obj;
		 
		resultElement = (String) jsonObject.get(element);
		
		return resultElement;
	}
	
	public JSONArray getElements(String jsonStringObject, String element) throws ParseException{
		
		Object obj = parser.parse(jsonStringObject);
		
		JSONObject jsonObject = (JSONObject) obj;
		
		// loop array
		JSONArray resultElements = (JSONArray) jsonObject.get(element);
		
		return resultElements;
	}

}
