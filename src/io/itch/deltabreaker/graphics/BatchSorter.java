package io.itch.deltabreaker.graphics;

import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import org.lwjgl.opengl.GL40;

import io.itch.deltabreaker.core.ResourceManager;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.math.Matrix4f;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;

public class BatchSorter {

	public static TreeMap<String, RenderSpec> batches = new TreeMap<>();
	public static TreeMap<String, RenderSpec> transparentBatches = new TreeMap<>();
	public static TreeMap<String, RenderSpec> staticBatches = new TreeMap<>();
	public static TreeMap<String, RenderSpec> shadowBatches = new TreeMap<>();
	private static StringBuilder br = new StringBuilder();
	
	public static void render() {
		long time = System.nanoTime();
		batches.values().forEach((r) -> {
			if (r.size > 0) {
				r.render();
			}
		});
		
		transparentBatches.values().forEach((r) -> {
			if (r.size > 0) {
				GL40.glDepthMask(r.ignoreDepth);
				r.render();
			}
		});
		GL40.glDepthMask(true);
		Startup.thread.performanceManager.rawRenderTime += (System.nanoTime() - time) / 1000000.0;
	}

	public static void renderStatic() {
		long time = System.nanoTime();
		staticBatches.values().forEach((r) -> {
			if (r.size > 0) {
				r.render();
			}
		});
		Startup.thread.performanceManager.rawRenderTime += (System.nanoTime() - time) / 1000000.0;
	}

	public static void renderShadow() {
		long time = System.nanoTime();
		shadowBatches.values().forEach((r) -> {
			if (r.size > 0) {
				r.render();
			}
		});
		Startup.thread.performanceManager.rawRenderTime += (System.nanoTime() - time) / 1000000.0;
	}

	public static void add(String model, String texture, String shader, String material, Vector3f position, Vector3f rotation, Vector3f scale, Vector4f shade, boolean shadow, boolean staticView, boolean... ignoreDepth) {
		br.setLength(0);
		boolean setIgnore = (ignoreDepth.length >= 1) ? ignoreDepth[0] : true;
		br.append(model);
		br.append(texture);
		br.append(shader);
		br.append(material);
		br.append(shadow);
		br.append(setIgnore);
		String key = br.toString();

		TreeMap<String, RenderSpec> batch = staticView ? staticBatches : (shade.getW() < 1) ? transparentBatches : batches;

		if (!batch.containsKey(key)) {
			batch.put(key, new RenderSpec(model, texture, shader, material, shadow, setIgnore));
		}
		batch.get(key).add(position, rotation, scale, shade);

		if (shadow) {
			if (!shadowBatches.containsKey(key)) {
				shadowBatches.put(key, new RenderSpec(model, texture, shader, material, shadow, setIgnore));
			}
			shadowBatches.get(key).add(position, rotation, scale, shade);
		}
	}

	public static void add(String priority, String model, String texture, String shader, String material, Vector3f position, Vector3f rotation, Vector3f scale, Vector4f shade, boolean shadow, boolean staticView, boolean... ignoreDepth) {
		br.setLength(0);
		boolean setIgnore = (ignoreDepth.length >= 1) ? ignoreDepth[0] : true;
		br.append(priority);
		br.append(model);
		br.append(texture);
		br.append(shader);
		br.append(material);
		br.append(shadow);
		br.append(setIgnore);
		String key = br.toString();

		TreeMap<String, RenderSpec> batch = staticView ? staticBatches : (shade.getW() < 1) ? transparentBatches : batches;

		if (!batch.containsKey(key)) {
			batch.put(key, new RenderSpec(model, texture, shader, material, shadow, setIgnore));
		}
		batch.get(key).add(position, rotation, scale, shade);

		if (shadow) {
			if (!shadowBatches.containsKey(key)) {
				shadowBatches.put(key, new RenderSpec(model, texture, shader, material, shadow, setIgnore));
			}
			shadowBatches.get(key).add(position, rotation, scale, shade);
		}
	}

	public static void addBatch(String model, String texture, String shader, String material, ArrayList<Vector3f> position, ArrayList<Vector3f> rotation, ArrayList<Vector3f> scale, ArrayList<Vector4f> shade, boolean shadow,
			boolean staticView, boolean... ignoreDepth) {
		br.setLength(0);
		boolean setIgnore = (ignoreDepth.length >= 1) ? ignoreDepth[0] : true;
		br.append(model);
		br.append(texture);
		br.append(shader);
		br.append(material);
		br.append(shadow);
		br.append(setIgnore);
		String key = br.toString();

		TreeMap<String, RenderSpec> batch = staticView ? staticBatches : batches;

		for (int i = 0; i < position.size(); i++) {
			if (!staticView) {
				batch = (shade.get(i).getW() < 1) ? transparentBatches : batches;
			}

			if (!batch.containsKey(key.toString())) {
				batch.put(key, new RenderSpec(model, texture, shader, material, shadow, setIgnore));
			}

			batch.get(key).add(position.get(i), rotation.get(i), scale.get(i), shade.get(i));
		}

		if (shadow) {
			if (!shadowBatches.containsKey(key)) {
				shadowBatches.put(key, new RenderSpec(model, texture, shader, material, shadow, setIgnore));
			}

			for (int i = 0; i < position.size(); i++) {
				shadowBatches.get(key).add(position.get(i), rotation.get(i), scale.get(i), shade.get(i));
			}
		}
	}

	public static void addBatch(String priority, String model, String texture, String shader, String material, ArrayList<Vector3f> position, ArrayList<Vector3f> rotation, ArrayList<Vector3f> scale, ArrayList<Vector4f> shade, boolean shadow,
			boolean staticView, boolean... ignoreDepth) {
		br.setLength(0);
		boolean setIgnore = (ignoreDepth.length >= 1) ? ignoreDepth[0] : true;
		br.append(priority);
		br.append(model);
		br.append(texture);
		br.append(shader);
		br.append(material);
		br.append(shadow);
		br.append(setIgnore);
		String key = br.toString();

		TreeMap<String, RenderSpec> batch = staticView ? staticBatches : batches;

		for (int i = 0; i < position.size(); i++) {
			if (!staticView) {
				batch = (shade.get(i).getW() < 1) ? transparentBatches : batches;
			}

			if (!batch.containsKey(key.toString())) {
				batch.put(key, new RenderSpec(model, texture, shader, material, shadow, setIgnore));
			}
			batch.get(key).add(position.get(i), rotation.get(i), scale.get(i), shade.get(i));
		}

		if (shadow) {
			if (!shadowBatches.containsKey(key)) {
				shadowBatches.put(key, new RenderSpec(model, texture, shader, material, shadow, setIgnore));
			}

			for (int i = 0; i < position.size(); i++) {
				shadowBatches.get(key).add(position.get(i), rotation.get(i), scale.get(i), shade.get(i));
			}
		}
	}

	public static void add(String model, String texture, String shader, String material, Matrix4f precalc, Vector4f shade, boolean shadow, boolean staticView, boolean... ignoreDepth) {
		br.setLength(0);
		boolean setIgnore = (ignoreDepth.length >= 1) ? ignoreDepth[0] : true;
		br.append(model);
		br.append(texture);
		br.append(shader);
		br.append(material);
		br.append(shadow);
		br.append(setIgnore);
		String key = br.toString();

		TreeMap<String, RenderSpec> batch = staticView ? staticBatches : (shade.getW() < 1) ? transparentBatches : batches;

		if (!batch.containsKey(key)) {
			batch.put(key, new RenderSpec(model, texture, shader, material, shadow, setIgnore));
		}
		batch.get(key).add(precalc, shade);

		if (shadow) {
			if (!shadowBatches.containsKey(key)) {
				shadowBatches.put(key, new RenderSpec(model, texture, shader, material, shadow, setIgnore));
			}
			shadowBatches.get(key).add(precalc, shade);
		}
	}

	public static void add(String priority, String model, String texture, String shader, String material, Matrix4f precalc, Vector4f shade, boolean shadow, boolean staticView, boolean... ignoreDepth) {
		br.setLength(0);
		boolean setIgnore = (ignoreDepth.length >= 1) ? ignoreDepth[0] : true;
		br.append(priority);
		br.append(model);
		br.append(texture);
		br.append(shader);
		br.append(material);
		br.append(shadow);
		br.append(setIgnore);
		String key = br.toString();

		TreeMap<String, RenderSpec> batch = staticView ? staticBatches : (shade.getW() < 1) ? transparentBatches : batches;

		if (!batch.containsKey(key)) {
			batch.put(key, new RenderSpec(model, texture, shader, material, shadow, setIgnore));
		}
		batch.get(key).add(precalc, shade);

		if (shadow) {
			if (!shadowBatches.containsKey(key)) {
				shadowBatches.put(key, new RenderSpec(model, texture, shader, material, shadow, setIgnore));
			}
			shadowBatches.get(key).add(precalc, shade);
		}
	}

	public static void addBatch(String model, String texture, String shader, String material, ArrayList<Matrix4f> precalc, ArrayList<Vector4f> shade, boolean shadow, boolean staticView, boolean... ignoreDepth) {
		br.setLength(0);
		boolean setIgnore = (ignoreDepth.length >= 1) ? ignoreDepth[0] : true;
		br.append(model);
		br.append(texture);
		br.append(shader);
		br.append(material);
		br.append(shadow);
		br.append(setIgnore);
		String key = br.toString();

		TreeMap<String, RenderSpec> batch = staticView ? staticBatches : batches;

		for (int i = 0; i < precalc.size(); i++) {
			if (!staticView) {
				batch = (shade.get(i).getW() < 1) ? transparentBatches : batches;
			}

			if (!batch.containsKey(key.toString())) {
				batch.put(key, new RenderSpec(model, texture, shader, material, shadow, setIgnore));
			}
			batch.get(key).add(precalc.get(i), shade.get(i));
		}

		if (shadow) {
			if (!shadowBatches.containsKey(key)) {
				shadowBatches.put(key, new RenderSpec(model, texture, shader, material, shadow, setIgnore));
			}

			for (int i = 0; i < precalc.size(); i++) {
				shadowBatches.get(key).add(precalc.get(i), shade.get(i));
			}
		}
	}

	public static void addBatch(String priority, String model, String texture, String shader, String material, ArrayList<Matrix4f> precalc, ArrayList<Vector4f> shade, boolean shadow, boolean staticView, boolean... ignoreDepth) {
		br.setLength(0);
		boolean setIgnore = (ignoreDepth.length >= 1) ? ignoreDepth[0] : true;
		br.append(priority);
		br.append(model);
		br.append(texture);
		br.append(shader);
		br.append(material);
		br.append(shadow);
		br.append(setIgnore);
		String key = br.toString();

		TreeMap<String, RenderSpec> batch = staticView ? staticBatches : batches;

		for (int i = 0; i < precalc.size(); i++) {
			if (!staticView) {
				batch = (shade.get(i).getW() < 1) ? transparentBatches : batches;
			}

			if (!batch.containsKey(key.toString())) {
				batch.put(key, new RenderSpec(model, texture, shader, material, shadow, setIgnore));
			}
			batch.get(key).add(precalc.get(i), shade.get(i));
		}

		if (shadow) {
			if (!shadowBatches.containsKey(key)) {
				shadowBatches.put(key, new RenderSpec(model, texture, shader, material, shadow, setIgnore));
			}

			for (int i = 0; i < precalc.size(); i++) {
				shadowBatches.get(key).add(precalc.get(i), shade.get(i));
			}
		}
	}

	public static void addLiquidBatch(String priority, String model, String texture, String shader, String material, ArrayList<Vector4f> shade) {
		String key = shader;

		if (!transparentBatches.containsKey(key)) {
			transparentBatches.put(key, new LiquidRenderSpec(model, texture, shader, material));
		}

		for (int i = 0; i < shade.size(); i++) {
			transparentBatches.get(key).add(null, shade.get(i));
		}
	}

	public static void clear() {
		batches.values().forEach((r) -> r.clear());
		staticBatches.values().forEach((r) -> r.clear());
		shadowBatches.values().forEach((r) -> r.clear());
		transparentBatches.values().forEach((r) -> r.clear());
	}

}

class RenderSpec {

	public String model, texture, shader, material;

	public boolean shadow;
	public boolean ignoreDepth;

	public int size = 0;
	public ArrayList<Vector4f> shades = new ArrayList<>();

	public ArrayList<Matrix4f> precalc = new ArrayList<>();
	public ArrayList<Matrix4f> created = new ArrayList<>();
	
	public RenderSpec(String model, String texture, String shader, String material, boolean shadow, boolean ignoreDepth) {
		this.model = model;
		this.texture = texture;
		this.shader = shader;
		this.material = material;
		this.shadow = shadow;
		this.ignoreDepth = ignoreDepth;
	}

	public void add(Vector3f position, Vector3f rotation, Vector3f scale, Vector4f shade) {
		Matrix4f result = Matrix4f.transform(position, rotation, scale);
		precalc.add(result);
		created.add(result);
		shades.add(shade);
		size++;
	}

	public void add(Matrix4f precalc, Vector4f shade) {
		this.precalc.add(precalc);
		shades.add(shade);
		size++;
	}

	public void render() {
		try {
			ResourceManager.models.get(model).renderBatchInstanced(precalc.size(), precalc, ResourceManager.textures.get(texture), ResourceManager.shaders.get(shader), Material.valueOf(material), shades, shadow);
		} catch (NullPointerException e) {
			JOptionPane.showMessageDialog(null, "NullPointerException thrown\n\nCheck resources:\n\nModel: " + model + (ResourceManager.models.get(model) == null ? " returned null" : "") + "\nTexture: " + texture
					+ (ResourceManager.textures.get(texture) == null ? " returned null" : "") + "\nShader: " + shader + (ResourceManager.shaders.get(shader) == null ? " returned null" : "") + "\nMaterial: " + material);
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void clear() {
		size = 0;
		precalc.clear();
		for(Matrix4f m : created) {
			Matrix4f.release(m);
		}
		created.clear();
		shades.clear();
	}

}

class LiquidRenderSpec extends RenderSpec {

	public LiquidRenderSpec(String model, String texture, String shader, String material) {
		super(model, texture, shader, material, false, true);
	}

	public void add(Matrix4f precalc, Vector4f shade) {
		shades.add(shade);
		size++;
	}

	public void render() {
		try {
			ResourceManager.models.get(model).renderWater(shades.size(), shades, ResourceManager.textures.get(texture), ResourceManager.shaders.get(shader), Material.valueOf(material));
		} catch (NullPointerException e) {
			JOptionPane.showMessageDialog(null, "NullPointerException thrown\n\nCheck resources:\n\nModel: " + model + (ResourceManager.models.get(model) == null ? " returned null" : "") + "\nTexture: " + texture
					+ (ResourceManager.textures.get(texture) == null ? " returned null" : "") + "\nShader: " + shader + (ResourceManager.shaders.get(shader) == null ? " returned null" : "") + "\nMaterial: " + material);
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void clear() {
		super.clear();
		size = 0;
		shades.clear();
	}

}