package io.itch.deltabreaker.event;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import io.itch.deltabreaker.core.FileManager;

public class DialogueScript {

	public static HashMap<String, DialogueScript> scripts = new HashMap<>();

	public String[][] text;
	public int[][] charTime;
	public String[][] units;
	public String[][] expressions;
	public String[][] mouth;
	public int[] highlights;

	public DialogueScript(String[][] text, int[][] charTime, String[][] units, String[][] expressions, String[][] mouth, int[] highlights) {
		this.text = text;
		this.charTime = charTime;
		this.units = units;
		this.expressions = expressions;
		this.mouth = mouth;
		this.highlights = highlights;
	}

	public static void loadScripts(String dir) {
		List<File> list = FileManager.getFiles(dir);

		for (File f : list) {
			if (f.getName().endsWith(".json")) {
				try {
					
					JSONArray ja = (JSONArray) new JSONParser().parse(new FileReader(f));
					
					String[][] text = new String[ja.size()][];
					int[][] charTime = new int[ja.size()][];
					String[][] units = new String[ja.size()][];
					String[][] expressions = new String[ja.size()][];
					String[][] mouth = new String[ja.size()][];
					int[] highlights = new int[ja.size()];
					
					for(int i = 0; i < ja.size(); i++) {
						JSONObject block = (JSONObject) ja.get(i);
						
						JSONArray jText = (JSONArray) block.get("text");
						text[i] = new String[jText.size()];
						for(int j = 0; j < jText.size(); j++) {
							text[i][j] = (String) jText.get(j);
						}
						
						JSONArray jTime = (JSONArray) block.get("charTime");
						charTime[i] = new int[jTime.size()];
						for(int j = 0; j < jTime.size(); j++) {
							charTime[i][j] = Math.toIntExact((long) jTime.get(j));
						}
						
						JSONArray jUnits = (JSONArray) block.get("units");
						units[i] = new String[jUnits.size()];
						expressions[i] = new String[jUnits.size()];
						mouth[i] = new String[jUnits.size()];
						for(int j = 0; j < jUnits.size(); j++) {
							JSONObject portrait = (JSONObject) jUnits.get(j);
							units[i][j] = (String) portrait.get("portrait");
							expressions[i][j] = (String) portrait.get("expression");
							mouth[i][j] = (String) portrait.get("mouth");
						}
						
						highlights[i] = Math.toIntExact((long) block.get("highlight"));
					}
					
					scripts.put(f.getName(), new DialogueScript(text, charTime, units, expressions, mouth, highlights));
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		System.out.println("[Script]: " + scripts.size() + " scripts loaded");
	}

}

//package io.itch.deltabreaker.event;
//
//import java.io.File;
//import java.io.FileReader;
//import java.util.HashMap;
//import java.util.List;
//
//import org.json.simple.JSONArray;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;
//
//import io.itch.deltabreaker.core.FileManager;
//
//public class DialogueScript {
//
//	public static HashMap<String, DialogueScript> scripts = new HashMap<>();
//
//	public String[][] text;
//	public int[][] charTime;
//	public String[][] units;
//	public int[] highlights;
//
//	public DialogueScript(String[][] text, int[][] charTime, String[][] units, int[] highlights) {
//		this.text = text;
//		this.charTime = charTime;
//		this.units = units;
//		this.highlights = highlights;
//	}
//
//	public static void loadScripts(String dir) {
//		List<File> list = FileManager.getFiles(dir);
//
//		for (File f : list) {
//			if (f.getName().endsWith(".json")) {
//				try {
//					
//					JSONArray ja = (JSONArray) new JSONParser().parse(new FileReader(f));
//					
//					String[][] text = new String[ja.size()][];
//					int[][] charTime = new int[ja.size()][];
//					String[][] units = new String[ja.size()][];
//					String[][] expressions = new String[ja.size()][];
//					String[][] mouth = new String[ja.size()][];
//					int[] highlights = new int[ja.size()];
//					
//					for(int i = 0; i < ja.size(); i++) {
//						JSONObject block = (JSONObject) ja.get(i);
//						
//						JSONArray jText = (JSONArray) block.get("text");
//						text[i] = new String[jText.size()];
//						for(int j = 0; j < jText.size(); j++) {
//							text[i][j] = (String) jText.get(j);
//						}
//						
//						JSONArray jTime = (JSONArray) block.get("charTime");
//						charTime[i] = new int[jTime.size()];
//						for(int j = 0; j < jTime.size(); j++) {
//							charTime[i][j] = Math.toIntExact((long) jTime.get(j));
//						}
//						
//						JSONArray jUnits = (JSONArray) block.get("units");
//						units[i] = new String[jUnits.size()];
//						expressions[i] = new String[jUnits.size()];
//						mouth[i] = new String[jUnits.size()];
//						for(int j = 0; j < jUnits.size(); j++) {
//							units[i][j] = (String) jUnits.get(j);
//						}
//						
//						highlights[i] = Math.toIntExact((long) block.get("highlight"));
//					}
//					
//					scripts.put(f.getName(), new DialogueScript(text, charTime, units, highlights));
//					
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}
//		
//		System.out.println("[Script]: " + scripts.size() + " scripts loaded");
//	}
//
//}