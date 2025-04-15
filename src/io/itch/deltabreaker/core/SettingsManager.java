package io.itch.deltabreaker.core;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

import io.itch.deltabreaker.core.audio.AudioManager;

public class SettingsManager {

	public static JFrame frame;

	// Graphics options
	public static boolean enableFXAA = true;
	public static boolean enableBloom = true;
	public static boolean enableFancyWater = true;
	public static boolean enableFancyRain = true;
	public static boolean fullscreen = false;
	public static boolean enableShake = true;

	public static float gamma = 0.81f;
	public static float vignetteRadius = 1.05f;
	public static float vignetteSoftness = 0.45f;
	public static float shadowIntensity = 0.05f;
	public static float resScaling = 1f;
	
	public static int shadowMapSize = 2048;
	public static int renderRate = 144;
	public static int bloomFidelity = 10;
	
	// These aren't saved
	public static Dimension storedRes = new Dimension(1280, 720);
	public static Point storedPos = new Point(0, 0);
	
	// Misc
	public static float deadzone = 0.35f;
	
	// Developer options
	public static boolean consolePerformacneOutput = false;
	public static boolean enableGLErrorChecking = true;

	public static void saveSettingsFile(String path, String file) {
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}

		File f = new File(dir + "/" + file);

		Properties config = new Properties();

		config.setProperty("enable_fxaa", "" + enableFXAA);
		config.setProperty("enable_bloom", "" + enableBloom);
		config.setProperty("bloom_fidelity", "" + bloomFidelity);
		config.setProperty("enable_fancy_water", "" + enableFancyWater);
		config.setProperty("enable_fancy_rain", "" + enableFancyRain);
		config.setProperty("render_scale", "" + resScaling);
		config.setProperty("shadow_resolution", "" + shadowMapSize);

		config.setProperty("start_fullscreen", "" + fullscreen);
		config.setProperty("gamma", "" + gamma);
		config.setProperty("vignette_radius", "" + vignetteRadius);
		config.setProperty("vignette_softness", "" + vignetteSoftness);
		config.setProperty("shadow_intensity", "" + shadowIntensity);

		config.setProperty("fps_cap", "" + renderRate);

		config.setProperty("music_gain", "" + AudioManager.defaultMusicGain);
		config.setProperty("sfx_main_gain", "" + AudioManager.defaultMainSFXGain);
		config.setProperty("sfx_sub_gain", "" + AudioManager.defaultSubSFXGain);
		config.setProperty("sfx_battle_gain", "" + AudioManager.defaultBattleSFXGain);

		config.setProperty("enable_camera_shake", "" + enableShake);
		
		try {
			config.store(new FileOutputStream(f), "Game Settings");
			System.out.println("[SettingsManager]: Settings file created");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void loadSettingsFile(String path, String file) {
		File f = new File(path + "/" + file);
		if (f.exists()) {
			Properties config = new Properties();

			try (FileInputStream fis = new FileInputStream(f)) {
				config.load(fis);

				enableFXAA = Boolean.parseBoolean((String) config.get("enable_fxaa"));
				enableBloom = Boolean.parseBoolean((String) config.get("enable_bloom"));
				bloomFidelity = Integer.parseInt((String) config.get("bloom_fidelity"));
				enableFancyWater = Boolean.parseBoolean((String) config.get("enable_fancy_water"));
				enableFancyRain = Boolean.parseBoolean((String) config.get("enable_fancy_rain"));
				resScaling = Float.parseFloat((String) config.get("render_scale"));
				shadowMapSize = Integer.parseInt((String) config.get("shadow_resolution"));

				fullscreen = Boolean.parseBoolean((String) config.get("start_fullscreen"));
				gamma = Float.parseFloat((String) config.get("gamma"));
				vignetteRadius = Float.parseFloat((String) config.get("vignette_radius"));
				vignetteSoftness = Float.parseFloat((String) config.get("vignette_softness"));
				shadowIntensity = Float.parseFloat((String) config.get("shadow_intensity"));

				renderRate = Integer.parseInt((String) config.get("fps_cap"));
				Startup.renderCap = 1.0 / renderRate;

				AudioManager.defaultMusicGain = Float.parseFloat((String) config.get("music_gain"));
				AudioManager.defaultMainSFXGain = Float.parseFloat((String) config.get("sfx_main_gain"));
				AudioManager.defaultSubSFXGain = Float.parseFloat((String) config.get("sfx_sub_gain"));
				AudioManager.defaultBattleSFXGain = Float.parseFloat((String) config.get("sfx_battle_gain"));
				
				enableShake = Boolean.parseBoolean((String) config.getProperty("enable_camera_shake"));
				
				System.out.println("[SettingsManager]: Settings file loaded");
			} catch (Exception e) {
				System.out.println("[SettingsManager]: Error loading settings. Recreating...");
				e.printStackTrace();
				saveSettingsFile(path, file);
			}
		} else {
			System.out.println("[SettingsManager]: Settings file not found");
			saveSettingsFile(path, file);
		}
	}

	public static JFrame createLiveSettingsEditor() {
		JFrame frame = new JFrame("Live Graphics Settings");
		frame.setLocationRelativeTo(null);
		frame.setSize(500, 500);
		frame.setLayout(null);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		JLabel gammaLabel = new JLabel("Brightness");
		gammaLabel.setBounds(10, 0, 100, 20);
		frame.add(gammaLabel);

		JSlider gammaSlider = new JSlider(50, 200, (int) (gamma * 100));
		gammaSlider.setBounds(10, 20, 100, 20);
		gammaSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				gamma = gammaSlider.getValue() / 100.0f;
			}
		});
		frame.add(gammaSlider);

		JLabel vignetteRadiusLabel = new JLabel("Vignette Size");
		vignetteRadiusLabel.setBounds(10, 40, 100, 20);
		frame.add(vignetteRadiusLabel);

		JSlider vignetteRadiusSlider = new JSlider(75, 175, (int) (vignetteRadius * 100));
		vignetteRadiusSlider.setBounds(10, 60, 100, 20);
		vignetteRadiusSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				vignetteRadius = vignetteRadiusSlider.getValue() / 100.0f;
			}
		});
		frame.add(vignetteRadiusSlider);

		JLabel vignetteSoftnessLabel = new JLabel("Vignette Softness");
		vignetteSoftnessLabel.setBounds(10, 80, 120, 20);
		frame.add(vignetteSoftnessLabel);

		JSlider vignetteSoftnessSlider = new JSlider(0, 100, (int) (vignetteSoftness * 100));
		vignetteSoftnessSlider.setBounds(10, 100, 100, 20);
		vignetteSoftnessSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				vignetteSoftness = vignetteSoftnessSlider.getValue() / 100.0f;
			}
		});
		frame.add(vignetteSoftnessSlider);

		JLabel shadowIntensitysLabel = new JLabel("Shadow Intensity");
		shadowIntensitysLabel.setBounds(10, 120, 120, 20);
		frame.add(shadowIntensitysLabel);

		JSlider shadowIntensitysSlider = new JSlider(1, 10, (int) (shadowIntensity * 100));
		shadowIntensitysSlider.setBounds(10, 140, 100, 20);
		shadowIntensitysSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				shadowIntensity = shadowIntensitysSlider.getValue() / 100.0f;
			}
		});
		frame.add(shadowIntensitysSlider);

		JLabel exposureLabel = new JLabel("Resolution Scale");
		exposureLabel.setBounds(10, 160, 120, 20);
		frame.add(exposureLabel);

		JSlider exposureSlider = new JSlider(25, 100, (int) (resScaling * 100));
		exposureSlider.setBounds(10, 180, 100, 20);
		exposureSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				resScaling = exposureSlider.getValue() / 100.0f;
			}
		});
		frame.add(exposureSlider);

		JLabel frameRateLabel = new JLabel("Frame Rate " + renderRate);
		frameRateLabel.setBounds(10, 200, 100, 20);
		frame.add(frameRateLabel);

		JSlider frameRateSlider = new JSlider(30, 144, renderRate);
		frameRateSlider.setBounds(10, 220, 100, 20);
		frameRateSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				frameRateLabel.setText("Frame Rate " + renderRate);
				renderRate = frameRateSlider.getValue();
				Startup.renderCap = 1.0 / renderRate;
			}
		});
		frame.add(frameRateSlider);
		
		JLabel fxaaLabel = new JLabel("Anti-aliasing");
		fxaaLabel.setBounds(150, 0, 100, 20);
		frame.add(fxaaLabel);

		JCheckBox fxaaBox = new JCheckBox("Anti-aliasing");
		fxaaBox.setBounds(150, 20, 20, 20);
		fxaaBox.setSelected(enableFXAA);
		fxaaBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				enableFXAA = fxaaBox.isSelected();
			}
		});
		frame.add(fxaaBox);
		
		JLabel bloomLabel = new JLabel("Bloom");
		bloomLabel.setBounds(150, 40, 100, 20);
		frame.add(bloomLabel);
		
		JCheckBox bloomBox = new JCheckBox("Bloom");
		bloomBox.setBounds(150, 60, 20, 20);
		bloomBox.setSelected(enableBloom);
		bloomBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				enableBloom = bloomBox.isSelected();
			}
		});
		frame.add(bloomBox);

		JLabel fancyWaterLabel = new JLabel("Fancy Water");
		fancyWaterLabel.setBounds(150, 80, 100, 20);
		frame.add(fancyWaterLabel);

		JCheckBox fancyWaterBox = new JCheckBox("Fancy Water");
		fancyWaterBox.setBounds(150, 100, 20, 20);
		fancyWaterBox.setSelected(enableFancyWater);
		fancyWaterBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				enableFancyWater = fancyWaterBox.isSelected();
			}
		});
		frame.add(fancyWaterBox);

		JLabel fancyRainLabel = new JLabel("Fancy Rain");
		fancyRainLabel.setBounds(150, 120, 100, 20);
		frame.add(fancyRainLabel);

		JCheckBox fancyRainBox = new JCheckBox("Fancy Rain");
		fancyRainBox.setBounds(150, 140, 20, 20);
		fancyRainBox.setSelected(enableFancyRain);
		fancyRainBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				enableFancyRain = fancyRainBox.isSelected();
			}
		});
		frame.add(fancyRainBox);

		JLabel fullscreenLabel = new JLabel("Fullscreen");
		fullscreenLabel.setBounds(150, 160, 100, 20);
		frame.add(fullscreenLabel);

		JCheckBox fullscreenBox = new JCheckBox("Fullscreen");
		fullscreenBox.setBounds(150, 180, 20, 20);
		fullscreenBox.setSelected(fullscreen);
		fullscreenBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				fullscreen = !fullscreen;
				SettingsManager.setFullscreen(fullscreen);
				fullscreenBox.setSelected(fullscreen);
			}
		});
		frame.add(fullscreenBox);

		JLabel outlineLabel = new JLabel("N/A");
		outlineLabel.setBounds(150, 200, 100, 20);
		frame.add(outlineLabel);

		JCheckBox outlineBox = new JCheckBox("N/A");
		outlineBox.setBounds(150, 220, 20, 20);
		outlineBox.setSelected(false);
		outlineBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
			}
		});
		frame.add(outlineBox);
		
		JLabel shakeLabel = new JLabel("Camera Shake");
		shakeLabel.setBounds(150, 240, 100, 20);
		frame.add(shakeLabel);

		JCheckBox shakeBox = new JCheckBox("shakes");
		shakeBox.setBounds(150, 260, 20, 20);
		shakeBox.setSelected(enableShake);
		shakeBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				enableShake = shakeBox.isSelected();
			}
		});
		frame.add(shakeBox);
		
		JLabel bloomFidelityLabel = new JLabel("Bloom Fidelity");
		bloomFidelityLabel.setBounds(250, 0, 100, 20);
		frame.add(bloomFidelityLabel);

		JSlider bloomFidelitySlider = new JSlider(2, 10, bloomFidelity);
		bloomFidelitySlider.setBounds(250, 20, 100, 20);
		bloomFidelitySlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				bloomFidelity = bloomFidelitySlider.getValue();
			}
		});
		frame.add(bloomFidelitySlider);

		JLabel outlineToleranceLabel = new JLabel("N/A");
		outlineToleranceLabel.setBounds(250, 40, 100, 20);
		frame.add(outlineToleranceLabel);

		JSlider outlineToleranceSlider = new JSlider(20, 80, 20);
		outlineToleranceSlider.setBounds(250, 60, 100, 20);
		outlineToleranceSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				
			}
		});
		frame.add(outlineToleranceSlider);
		
		
		JLabel outlineThicknessLabel = new JLabel("N/A");
		outlineThicknessLabel.setBounds(250, 80, 100, 20);
		frame.add(outlineThicknessLabel);

		JSlider outlineThicknessSlider = new JSlider(100, 400, 100);
		outlineThicknessSlider.setBounds(250, 100, 100, 20);
		outlineThicknessSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent arg0) {
				
			}
		});
		frame.add(outlineThicknessSlider);
		
		JButton save = new JButton("Save");
		save.setBounds(10, 280, 100, 20);
		save.setFocusable(false);
		save.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				saveSettingsFile(Startup.CFG_PATH, Startup.CFG_FILE);
			}
		});
		frame.add(save);

		return frame;
	}

	public static void destroy() {
		if (frame != null) {
			frame.dispose();
		}
	}

	public static void showLiveSettingsEditor() {
		if (frame == null) {
			frame = createLiveSettingsEditor();
		}
		frame.setVisible(!frame.isVisible());
	}

	public static void setFullscreen(boolean isFullscreen) {
		GLFW.glfwSetWindowAttrib(Startup.thread.window, GLFW.GLFW_DECORATED, (isFullscreen) ? GLFW.GLFW_FALSE : GLFW.GLFW_TRUE);
		if (isFullscreen) {
			storedRes.setSize(Startup.width, Startup.height);
			int[] x = new int[1];
			int[] y = new int[1];
			GLFW.glfwGetWindowPos(Startup.thread.window, x, y);
			storedPos.setLocation(x[0], y[0]);

			GLFW.glfwSetWindowPos(Startup.thread.window, 0, 0);
			GLFWVidMode vid = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
			GLFW.glfwSetWindowSize(Startup.thread.window, vid.width(), vid.height());
			Startup.thread.resize = true;
			
			fullscreen = true;
		} else {
			GLFW.glfwSetWindowPos(Startup.thread.window, (int) storedPos.getX(), (int) storedPos.getY());
			GLFW.glfwSetWindowSize(Startup.thread.window, storedRes.width, storedRes.height);
			Startup.thread.resize = true;
			
			fullscreen = false;
		}
	}
	
}
