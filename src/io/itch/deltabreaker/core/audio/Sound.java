package io.itch.deltabreaker.core.audio;

import java.io.File;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import org.lwjgl.openal.AL10;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.libc.LibCStdlib;

public class Sound {

	private int bufferId;
	private int sourceId;

	private boolean isPlaying = false;
	private boolean stopAfterFade = true;
	
	private float gain = AudioManager.defaultGain;
	private float targetGain = gain;
	private float gainSpeed = 0;

	public Sound(File file) {
		MemoryStack.stackPush();
		IntBuffer channelsBuffer = MemoryStack.stackMallocInt(1);

		MemoryStack.stackPush();
		IntBuffer sampleRateBuffer = MemoryStack.stackMallocInt(1);

		ShortBuffer rawAudioBuffer = STBVorbis.stb_vorbis_decode_filename(file.getPath(), channelsBuffer, sampleRateBuffer);
		if (rawAudioBuffer == null) {
			System.out.println("[Sound]: Failed to load sound " + file.getName());
			MemoryStack.stackPop();
			MemoryStack.stackPop();
			return;
		}

		int channels = channelsBuffer.get();
		int sampleRate = sampleRateBuffer.get();
		MemoryStack.stackPop();
		MemoryStack.stackPop();

		int format = -1;
		if (channels == 1) {
			format = AL10.AL_FORMAT_MONO16;
		} else if (channels == 2) {
			format = AL10.AL_FORMAT_STEREO16;
		}

		bufferId = AL10.alGenBuffers();
		AL10.alBufferData(bufferId, format, rawAudioBuffer, sampleRate);

		sourceId = AL10.alGenSources();
		AL10.alSourcei(sourceId, AL10.AL_BUFFER, bufferId);
		AL10.alSourcei(sourceId, AL10.AL_LOOPING, 0);
		AL10.alSourcei(sourceId, AL10.AL_POSITION, 0);
		AL10.alSourcef(sourceId, AL10.AL_GAIN, gain);
		LibCStdlib.free(rawAudioBuffer);
		
		System.out.println("[Sound]: Loaded audio " + file.getName());
	}

	public void play(float gain, boolean loop) {
		int state = AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE);
		if (state == AL10.AL_STOPPED) {
			isPlaying = false;
			AL10.alSourcei(sourceId, AL10.AL_POSITION, 0);
		}
		if (!isPlaying) {
			isPlaying = true;
			setVolume(gain);
			AL10.alSourcei(sourceId, AL10.AL_LOOPING, (loop) ? 1 : 0);
			AL10.alSourcePlay(sourceId);
		} else {
			stop();
			play(gain, loop);
		}
	}

	public void playWithoutReset(float gain, boolean loop) {
		int state = AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE);
		if (state == AL10.AL_STOPPED) {
			isPlaying = false;
			AL10.alSourcei(sourceId, AL10.AL_POSITION, 0);
		}
		if (!isPlaying) {
			isPlaying = true;
			setVolume(gain);
			AL10.alSourcei(sourceId, AL10.AL_LOOPING, (loop) ? 1 : 0);
			AL10.alSourcePlay(sourceId);
		}
	}
	
	public void stop() {
		if (isPlaying) {
			AL10.alSourceStop(sourceId);
			isPlaying = false;
		}
	}

	public void fade(float target, int time, boolean stopAfterFade) {
		targetGain = target;
		gainSpeed = (target - gain) / time;
		this.stopAfterFade = stopAfterFade;
	}

	public void setVolume(float gain) {
		this.gain = gain;
		targetGain = gain;
		AL10.alSourcef(sourceId, AL10.AL_GAIN, gain);
	}

	public void updateGain() {
		if (gain != targetGain) {
			if (gain < targetGain) {
				gain = Math.min(targetGain, gain + gainSpeed);
			} else if (gain > targetGain) {
				gain = Math.max(targetGain, gain + gainSpeed);
			}
			AL10.alSourcef(sourceId, AL10.AL_GAIN, gain);
			
			if(gain == 0 && stopAfterFade) {
				stop();
			}
		}
	}

	public boolean isPlaying() {
		int state = AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE);
		if (state == AL10.AL_STOPPED) {
			isPlaying = false;
		}
		return isPlaying;
	}

	public void delete() {
		AL10.alDeleteBuffers(bufferId);
		AL10.alDeleteSources(sourceId);
	}

}