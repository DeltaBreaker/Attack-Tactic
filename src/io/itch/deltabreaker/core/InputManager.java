package io.itch.deltabreaker.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Properties;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWGamepadState;
import org.lwjgl.glfw.GLFWJoystickCallback;
import org.lwjgl.glfw.GLFWKeyCallback;

import io.itch.deltabreaker.state.StateManager;

public class InputManager {

	private static String fileName = "bindings.cfg";
	public static int repeatDelay = 10;
	public static int keyTime = 15;

	public int keyTimer = 0;
	public int repeatTimer = 0;
	public boolean pressed = false;
	public InputMapping key;

	private GLFWKeyCallback keyboard;
	private GLFWJoystickCallback gamepads;
	private GLFWGamepadState state;
	private boolean pollController = false;
	private PerformanceManager performanceManager;

	private boolean[] buttonStates = new boolean[15];
	private byte[] buttonPressed = new byte[15];
	private float[] axisStates = new float[4];
	
	public InputManager(PerformanceManager performanceManager) {
		loadInputMappings(fileName);

		keyboard = new GLFWKeyCallback() {
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
				if (action == GLFW.GLFW_PRESS) {
					onKeyPressed(InputMapping.getActionFromKey(key));
				} else if (action == GLFW.GLFW_RELEASE) {
					onKeyReleased(InputMapping.getActionFromKey(key));
				}
			}
		};

		// This handles when a controller connects/disconnects
		pollController = GLFW.glfwJoystickPresent(GLFW.GLFW_JOYSTICK_1);
		gamepads = new GLFWJoystickCallback() {
			@Override
			public void invoke(int id, int event) {
				if (id == GLFW.GLFW_JOYSTICK_1) {
					if (event == GLFW.GLFW_CONNECTED) {
						pollController = true;
					} else if (event == GLFW.GLFW_DISCONNECTED) {
						pollController = false;
					}
				}
			}

		};
		state = new GLFWGamepadState(ByteBuffer.allocateDirect(40));
		this.performanceManager = performanceManager;
	}

	private void loadInputMappings(String fileName) {
		File f = new File(fileName);
		if (f.exists()) {
			Properties config = new Properties();

			try (FileInputStream fis = new FileInputStream(f)) {
				config.load(fis);

				for (InputMapping i : InputMapping.values()) {
					String[] values = ((String) config.get(i.toString())).split(",");
					i.setKeyCode(Integer.parseInt(values[0]));
					i.setKeyCode(Integer.parseInt(values[1]));
				}

				System.out.println("[InputManager]: Input bindings file loaded");
			} catch (Exception e) {
				System.out.println("[InputManager]: Error loading input bindings. Recreating...");
				e.printStackTrace();
				createMappingFile(fileName);
			}
		} else {
			createMappingFile(fileName);
		}
	}

	private void createMappingFile(String file) {
		File dir = new File("save");
		if (!dir.exists()) {
			dir.mkdirs();
		}

		File f = new File(dir + "/" + file);
		Properties config = new Properties();

		for (InputMapping i : InputMapping.values()) {
			i.reset();
			config.setProperty(i.toString(), i.keyCode + "," + i.buttonCode);
		}

		try {
			config.store(new FileOutputStream(f), "Input Bindings");
			System.out.println("[InputManager]: Input bindings file created");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void onKeyPressed(InputMapping key) {
		repeatTimer = 0;
		this.key = key;
		keyTimer = 0;
		pressed = true;
		StateManager.currentState.onKeyPress(key);
		repeatActions();

		if (key == InputMapping.SHOW_INFO) {
			performanceManager.toggleWindow();
			PerformanceManager.developerMode = !PerformanceManager.developerMode;
		}
		if (key == InputMapping.SHOW_SETTINGS) {
			SettingsManager.showLiveSettingsEditor();
		}
	}

	public void onKeyReleased(InputMapping key) {
		if (this.key == key) {
			pressed = false;
		}
		StateManager.currentState.onKeyRelease(key);
	}

	// This method turns the constant gamepad button polls into a single call like
	// keyboard events
	public void checkGamepadState() {
		if (pollController && GLFW.glfwJoystickIsGamepad(GLFW.GLFW_JOYSTICK_1)) {
			GLFW.glfwGetGamepadState(GLFW.GLFW_JOYSTICK_1, state);

			state.buttons().get(buttonPressed);

			for (int i = 0; i < buttonStates.length; i++) {
				if (buttonPressed[i] == 1 && !this.buttonStates[i]) {
					this.buttonStates[i] = true;
					onButtonPressed(i);
				}
				if (buttonPressed[i] == 0 && this.buttonStates[i]) {
					this.buttonStates[i] = false;
					onButtonReleased(i);
				}
			}

			state.axes().get(axisStates);

			if ((axisStates[0] > SettingsManager.deadzone || axisStates[0] < -SettingsManager.deadzone) || (axisStates[1] > SettingsManager.deadzone || axisStates[1] < -SettingsManager.deadzone)) {
				InputMapping.JOYSTICK.setAxes(axisStates[0], axisStates[1]);
				onKeyPressed(InputMapping.JOYSTICK);
			}
		}
	}

	public void onButtonPressed(int button) {
		onKeyPressed(InputMapping.getActionFromButton(button));
	}

	public void onButtonReleased(int button) {
		onKeyReleased(InputMapping.getActionFromButton(button));
	}

	public void repeatActions() {
		StateManager.currentState.onKeyRepeat(key);
	}

	public GLFWKeyCallback getKeyboard() {
		return keyboard;
	}

	public GLFWJoystickCallback getGamepads() {
		return gamepads;
	}

}
