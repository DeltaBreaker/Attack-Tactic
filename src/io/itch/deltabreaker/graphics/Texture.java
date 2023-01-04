package io.itch.deltabreaker.graphics;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL40;

import io.itch.deltabreaker.math.Vector3f;

public class Texture {

	private int id;
	private int width, height;

	private byte[] pixelData;

	public boolean built = false;
	
	public Texture(String file) {
		BufferedImage bi;
		try {
			bi = ImageIO.read(new File(file));
			width = bi.getWidth();
			height = bi.getHeight();

			int[] pixels_raw = new int[width * height * 4];
			pixels_raw = bi.getRGB(0, 0, width, height, null, 0, width);
			pixelData = new byte[pixels_raw.length * 4];

			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					int pixel = pixels_raw[x * height + y];

					byte r = (byte) ((pixel >> 16) & 0xff);
					byte g = (byte) ((pixel >> 8) & 0xff);
					byte b = (byte) (pixel & 0xff);
					byte a = (byte) ((pixel >> 24) & 0xff);

					pixelData[(x * height + y) * 4] = r;
					pixelData[(x * height + y) * 4 + 1] = g;
					pixelData[(x * height + y) * 4 + 2] = b;
					pixelData[(x * height + y) * 4 + 3] = a;
				}
			}
		} catch (Exception e) {
			System.err.println("[Texture]: Failed to load texture " + file);
			e.printStackTrace();
		}
	}

	public Texture(int width, int height, byte[] data) {
		this.width = width;
		this.height = height;
		pixelData = data;
	}

	public Texture(int width, int height, int pixelFormat) throws Exception {
		id = GL40.glGenTextures();
		this.width = width;
		this.height = height;
		GL40.glBindTexture(GL40.GL_TEXTURE_2D, this.id);
		GL40.glTexImage2D(GL40.GL_TEXTURE_2D, 0, GL40.GL_DEPTH_COMPONENT, this.width, this.height, 0, pixelFormat, GL40.GL_FLOAT, (ByteBuffer) null);
		GL40.glTexParameteri(GL40.GL_TEXTURE_2D, GL40.GL_TEXTURE_MIN_FILTER, GL40.GL_NEAREST);
		GL40.glTexParameteri(GL40.GL_TEXTURE_2D, GL40.GL_TEXTURE_MAG_FILTER, GL40.GL_NEAREST);
		GL40.glTexParameteri(GL40.GL_TEXTURE_2D, GL40.GL_TEXTURE_WRAP_S, GL40.GL_CLAMP_TO_EDGE);
		GL40.glTexParameteri(GL40.GL_TEXTURE_2D, GL40.GL_TEXTURE_WRAP_T, GL40.GL_CLAMP_TO_EDGE);
		
		built = true;
	}

	public void build() {
		ByteBuffer pixels = BufferUtils.createByteBuffer(pixelData.length * 4);
		for (int i = 0; i < pixelData.length / 4; i++) {
			pixels.put(pixelData[i * 4]);
			pixels.put(pixelData[i * 4 + 1]);
			pixels.put(pixelData[i * 4 + 2]);
			pixels.put(pixelData[i * 4 + 3]);
		}
		pixels.flip();

		id = GL40.glGenTextures();
		GL40.glBindTexture(GL40.GL_TEXTURE_2D, id);
		GL40.glTexParameterf(GL40.GL_TEXTURE_2D, GL40.GL_TEXTURE_MIN_FILTER, GL40.GL_NEAREST);
		GL40.glTexParameterf(GL40.GL_TEXTURE_2D, GL40.GL_TEXTURE_MAG_FILTER, GL40.GL_NEAREST);
		GL40.glTexImage2D(GL40.GL_TEXTURE_2D, 0, GL40.GL_RGBA, width, height, 0, GL40.GL_RGBA, GL40.GL_UNSIGNED_BYTE, pixels);
		GL40.glBindTexture(GL40.GL_TEXTURE_2D, 0);
		
		built = true;
	}

	public void bind(int sampler) {
		if (sampler >= 0 && sampler <= 31) {
			GL40.glActiveTexture(GL40.GL_TEXTURE0 + sampler);
			GL40.glBindTexture(GL40.GL_TEXTURE_2D, id);
		}
	}

	public int getID() {
		return id;
	}

	public Vector3f getColor(int x, int y) {
		int location = (x * height + y) * 4;
		return new Vector3f((pixelData[location] & 0xff) / 255f, (pixelData[location + 1] & 0xff) / 255f, (pixelData[location + 2] & 0xff) / 255f);
	}

}
