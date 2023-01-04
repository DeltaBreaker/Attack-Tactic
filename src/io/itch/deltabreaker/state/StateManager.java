package io.itch.deltabreaker.state;

import java.util.TreeMap;

public class StateManager {

	public static State currentState = new State("");
	private static TreeMap<String, State> states = new TreeMap<String, State>();

	public static boolean paused = false;
	
	public static void tick() {
		if (!paused) {
			currentState.tick();
		}
	}

	public static void render() {
		currentState.render();
	}

	public static void swapState(String name) {
		currentState.onExit();
		currentState = states.get(name);
		currentState.onEnter();
		System.out.println("[StateManager]: State changed to: " + name);
	}

	public static void initState(State state) {
		if(states.containsKey(state.STATE_ID)) {
			states.get(state.STATE_ID).onDestroy();
		}
		states.put(state.STATE_ID, state);
		System.out.println("[StateManager]: State initialized: " + state.STATE_ID);
	}

	public static State getState(String stateID) {
		return states.get(stateID);
	}

}
