package io.itch.deltabreaker.core.audio;

import java.io.File;
import java.util.HashMap;

import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;

public class AudioManager {

	private static HashMap<String, Sound> sounds = new HashMap<>();

	public static float defaultGain = 0.5f;
	public static float defaultMusicGain = 0.5f;
	public static float defaultMainSFXGain = 0.5f;
	public static float defaultSubSFXGain = 0.5f;
	public static float defaultBattleSFXGain = 0.5f;

	private static long audioContext;
	private static long audioDevice;

	public static void loadAudio(File file) {
		sounds.put(file.getName(), new Sound(file));
	}

	public static void setup() {
		String defaultOutput = ALC10.alcGetString(0, ALC10.ALC_DEFAULT_DEVICE_SPECIFIER);
		audioDevice = ALC10.alcOpenDevice(defaultOutput);

		int[] args = { 0 };
		audioContext = ALC10.alcCreateContext(audioDevice, args);

		ALC10.alcMakeContextCurrent(audioContext);

		ALCCapabilities alcCap = ALC.createCapabilities(audioDevice);
		ALCapabilities alCap = AL.createCapabilities(alcCap);

		if (!alCap.OpenAL10) {
			System.out.println("[AudioManager]: OpenAL not supported");
		} else {
			System.out.println("[AudioManager]: OpenAL started");
		}
	}

	public static void tick() {
		for (Sound s : sounds.values()) {
			if (s.isPlaying()) {
				s.updateGain();
			}
		}
	}

	public static Sound getSound(String name) {
		return sounds.get(name);
	}

	public static void cleanUp() {
		ALC10.alcDestroyContext(audioContext);
		ALC10.alcCloseDevice(audioDevice);
	}

}