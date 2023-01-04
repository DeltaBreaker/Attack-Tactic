package io.itch.deltabreaker.event;

import java.io.File;
import java.io.FileReader;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class EventScript {

	public static final String ACTIVATOR_ACTION = "event.activator.action";
	public static final String ACTIVATOR_MAP_LOAD = "event.activator.map.load";
	public static final String ACTIVATOR_MAP_ROUTE = "event.activator.map.route";
	public static final String ACTIVATOR_DEATH = "event.activator.death";
	public static final String ACTIVATOR_WAIT = "event.activator.wait";

	public String activator;
	public String[] lines;

	public EventScript(String activator, String[] lines) {
		this.activator = activator;
		this.lines = lines;
	}

	public static EventScript loadScript(File f) {
		if (f.getName().endsWith(".json")) {
			try {

				JSONObject jo = (JSONObject) new JSONParser().parse(new FileReader(f));

				String activator = (String) jo.get("activator");
				JSONArray ja = (JSONArray) jo.get("script");
				String[] lines = new String[ja.size()];
				for (int i = 0; i < ja.size(); i++) {
					lines[i] = (String) ja.get(i);
				}

				return new EventScript(activator, lines);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			System.out.println("[EventScript]: File " + f.getName() + " was not found");
		}
		return null;
	}

}
