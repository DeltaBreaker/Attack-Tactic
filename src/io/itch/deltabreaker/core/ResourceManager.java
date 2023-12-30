package io.itch.deltabreaker.core;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterOutputStream;

import javax.imageio.ImageIO;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.lwjgl.assimp.AIFace;
import org.lwjgl.assimp.AIMesh;
import org.lwjgl.assimp.AIScene;
import org.lwjgl.assimp.AIVector3D;
import org.lwjgl.assimp.Assimp;

import io.itch.deltabreaker.core.audio.AudioManager;
import io.itch.deltabreaker.graphics.Model;
import io.itch.deltabreaker.graphics.Texture;
import io.itch.deltabreaker.graphics.shader.Shader;
import io.itch.deltabreaker.graphics.shader.ShaderAdditiveBloom;
import io.itch.deltabreaker.graphics.shader.ShaderDeferredLighting;
import io.itch.deltabreaker.graphics.shader.ShaderDrawImage;
import io.itch.deltabreaker.graphics.shader.ShaderGaussianBlur;
import io.itch.deltabreaker.graphics.shader.ShaderMain3D;
import io.itch.deltabreaker.graphics.shader.ShaderMain3DBloom;
import io.itch.deltabreaker.graphics.shader.ShaderMain3DCrystal;
import io.itch.deltabreaker.graphics.shader.ShaderMain3DEnemy;
import io.itch.deltabreaker.graphics.shader.ShaderMain3DHousing;
import io.itch.deltabreaker.graphics.shader.ShaderMain3DLava;
import io.itch.deltabreaker.graphics.shader.ShaderMain3DMedial;
import io.itch.deltabreaker.graphics.shader.ShaderMain3DNoBloom;
import io.itch.deltabreaker.graphics.shader.ShaderMain3DNoBloomTexColor;
import io.itch.deltabreaker.graphics.shader.ShaderMain3DWater;
import io.itch.deltabreaker.graphics.shader.ShaderShadow3D;
import io.itch.deltabreaker.graphics.shader.ShaderStatic3D;
import io.itch.deltabreaker.graphics.shader.ShaderStatic3DCrafting;
import io.itch.deltabreaker.state.StateCreatorHub;
import io.itch.deltabreaker.state.StateDungeon;
import io.itch.deltabreaker.state.StateManager;
import io.itch.deltabreaker.state.StateMatchLobby;
import io.itch.deltabreaker.state.StateSplash;
import io.itch.deltabreaker.state.StateTitle;
import io.itch.deltabreaker.state.StateUnitCreator;

public class ResourceManager {

	public static String currentHash = "";

	public static HashMap<String, Model> models = new HashMap<>();
	public static HashMap<String, Shader> shaders = new HashMap<>();
	public static HashMap<String, Texture> textures = new HashMap<>();

	public static void validateData() {
		long time = System.nanoTime();
		StringBuilder megaHash = new StringBuilder("");

		try {
			List<File> list = FileManager.getFiles("res/data");
			for (File f : list) {
				if (!f.isDirectory()) {
					MessageDigest shaDigest = MessageDigest.getInstance("SHA-256");
					megaHash.append(getFileChecksum(shaDigest, f));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		currentHash = "" + megaHash.toString().hashCode();
		System.out.println("[ResourceManager]: Current hash " + currentHash);

		System.out.println(
				"[ResourceManager]: Data checked in " + (int) ((System.nanoTime() - time) / 100.0) / 10000.0 + "ms");
	}

	private static String getFileChecksum(MessageDigest digest, File file) throws IOException {
		FileInputStream fis = new FileInputStream(file);

		byte[] byteArray = new byte[1024];
		int bytesCount = 0;

		while ((bytesCount = fis.read(byteArray)) != -1) {
			digest.update(byteArray, 0, bytesCount);
		}

		fis.close();

		byte[] bytes = digest.digest();

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
		}

		return sb.toString();
	}

	public static void packModels(String folder) {
		if (new File(folder).exists()) {
			try {
				int packedModels = 0;
				File output = new File(folder + "/models.pck");
				DataOutputStream out = new DataOutputStream(new FileOutputStream(output));

				List<File> list = FileManager.getFiles(folder);
				for (File f : list) {
					if (f.getName().endsWith(".dae")) {
						packedModels++;
					}
				}

				out.writeInt(packedModels);

				for (File f : list) {
					if (f.getName().endsWith(".dae")) {
						AIScene scene = Assimp.aiImportFile(f.getPath(),
								Assimp.aiProcess_JoinIdenticalVertices | Assimp.aiProcess_Triangulate);

						if (scene == null) {
							System.err.println("[FileManager]: There was an error loading the model  " + f);
						}

						AIMesh mesh = AIMesh.create(scene.mMeshes().get(0));
						int vertexCount = mesh.mNumVertices();

						AIVector3D.Buffer vertices = mesh.mVertices();
						AIVector3D.Buffer normals = mesh.mNormals();

						float[] vertexList = new float[vertexCount * 3];
						float[] normalList = new float[vertexCount * 3];
						float[] texCoordList = new float[vertexCount * 2];

						for (int i = 0; i < vertexCount; i++) {
							AIVector3D vertex = vertices.get(i);
							vertexList[i * 3] = vertex.x();
							vertexList[i * 3 + 1] = vertex.y();
							vertexList[i * 3 + 2] = vertex.z();

							AIVector3D normal = normals.get(i);
							normalList[i * 3] = normal.x();
							normalList[i * 3 + 1] = normal.y();
							normalList[i * 3 + 2] = normal.z();

							if (mesh.mNumUVComponents().get(0) != 0) {
								AIVector3D texture = mesh.mTextureCoords(0).get(i);
								texCoordList[i * 2] = texture.x();
								texCoordList[i * 2 + 1] = texture.y();
							}
						}

						int faceCount = mesh.mNumFaces();
						int[] indicesList = new int[faceCount * 3];

						AIFace.Buffer indices = mesh.mFaces();

						for (int i = 0; i < faceCount; i++) {
							AIFace face = indices.get(i);
							indicesList[i * 3] = face.mIndices().get(0);
							indicesList[i * 3 + 1] = face.mIndices().get(1);
							indicesList[i * 3 + 2] = face.mIndices().get(2);
						}

						out.writeInt(vertexList.length);
						byte[] buffer = new byte[4 * vertexList.length];
						for (int i = 0; i < vertexList.length; i++) {
							int val = Float.floatToIntBits(vertexList[i]);
							buffer[4 * i] = (byte) (val >> 24);
							buffer[4 * i + 1] = (byte) (val >> 16);
							buffer[4 * i + 2] = (byte) (val >> 8);
							buffer[4 * i + 3] = (byte) (val);
						}
						buffer = compress(buffer);
						out.writeInt(buffer.length);
						out.write(buffer);

						out.writeInt(texCoordList.length);
						buffer = new byte[4 * texCoordList.length];
						for (int i = 0; i < texCoordList.length; i++) {
							int val = Float.floatToIntBits(texCoordList[i]);
							buffer[4 * i] = (byte) (val >> 24);
							buffer[4 * i + 1] = (byte) (val >> 16);
							buffer[4 * i + 2] = (byte) (val >> 8);
							buffer[4 * i + 3] = (byte) (val);
						}
						buffer = compress(buffer);
						out.writeInt(buffer.length);
						out.write(buffer);

						out.writeInt(normalList.length);
						buffer = new byte[4 * normalList.length];
						for (int i = 0; i < normalList.length; i++) {
							int val = Float.floatToIntBits(normalList[i]);
							buffer[4 * i] = (byte) (val >> 24);
							buffer[4 * i + 1] = (byte) (val >> 16);
							buffer[4 * i + 2] = (byte) (val >> 8);
							buffer[4 * i + 3] = (byte) (val);
						}
						buffer = compress(buffer);
						out.writeInt(buffer.length);
						out.write(buffer);

						out.writeInt(indicesList.length);
						buffer = new byte[4 * indicesList.length];
						for (int i = 0; i < indicesList.length; i++) {
							int val = indicesList[i];
							buffer[4 * i] = (byte) (val >> 24);
							buffer[4 * i + 1] = (byte) (val >> 16);
							buffer[4 * i + 2] = (byte) (val >> 8);
							buffer[4 * i + 3] = (byte) (val);
						}
						buffer = compress(buffer);
						out.writeInt(buffer.length);
						out.write(buffer);

						out.writeUTF(f.getName());
						System.out.println("[ResourceManager]: " + f.getName() + " packed");
					}
				}

				out.close();
				System.out.println("[ResourceManager]: " + packedModels + " models packed");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void packTextures(String folder) {
		if (new File(folder).exists()) {
			try {
				int textures = 0;
				File output = new File(folder + "/textures.pck");
				DataOutputStream out = new DataOutputStream(new FileOutputStream(output));

				List<File> files = FileManager.getFiles(folder);

				for (File f : files) {
					if (f.getName().endsWith(".png") || f.getName().endsWith(".jpg") || f.getName().endsWith(".jpeg")) {
						textures++;
					}
				}
				out.writeInt(textures);

				for (File f : files) {
					if (f.getName().endsWith(".png") || f.getName().endsWith(".jpg") || f.getName().endsWith(".jpeg")) {
						BufferedImage image = ImageIO.read(f);
						int width = image.getWidth();
						int height = image.getHeight();
						int[] data = image.getRGB(0, 0, width, height, null, 0, width);
						byte[] rawData = new byte[data.length * 4];

						for (int x = 0; x < width; x++) {
							for (int y = 0; y < height; y++) {
								int pixel = data[x * height + y];
								rawData[(x * height + y) * 4] = (byte) ((pixel >> 16) & 0xff);
								rawData[(x * height + y) * 4 + 1] = (byte) ((pixel >> 8) & 0xff);
								rawData[(x * height + y) * 4 + 2] = (byte) (pixel & 0xff);
								rawData[(x * height + y) * 4 + 3] = (byte) ((pixel >> 24) & 0xff);
							}
						}

						byte[] compressedData = compress(rawData);

						out.writeInt(width);
						out.writeInt(height);
						out.writeInt(compressedData.length);

						out.write(compressedData);
						out.writeUTF(f.getName());

						System.out.println("[ResourceManager]: " + f.getName() + " packed");
					}
				}

				out.close();
				System.out.println("[ResourceManager]: " + textures + " textures packed");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void loadStates() {
		StateManager.initState(new StateDungeon());
		StateManager.initState(new StateCreatorHub());
		StateManager.initState(new StateUnitCreator());
		StateManager.initState(new StateTitle());
		StateManager.initState(new StateSplash());
		StateManager.initState(new StateMatchLobby());
	}

	public static void loadModels(String folder) {
		long time = System.nanoTime();
		File pack = new File(folder + "/models.pck");
		if (pack.exists()) {
			try {
				DataInputStream in = new DataInputStream(new FileInputStream(pack));

				int size = in.readInt();

				for (int i = 0; i < size; i++) {

					int verts = in.readInt();
					byte[] vertBytes = new byte[in.readInt()];
					float[] vertices = new float[verts];
					in.readFully(vertBytes);
					vertBytes = decompress(vertBytes);
					for (int v = 0; v < verts; v++) {
						vertices[v] = ByteBuffer.wrap(new byte[] { vertBytes[v * 4], vertBytes[v * 4 + 1],
								vertBytes[v * 4 + 2], vertBytes[v * 4 + 3] }).getFloat();
					}

					int texs = in.readInt();
					byte[] texBytes = new byte[in.readInt()];
					float[] texices = new float[texs];
					in.readFully(texBytes);
					texBytes = decompress(texBytes);
					for (int v = 0; v < texs; v++) {
						texices[v] = ByteBuffer.wrap(new byte[] { texBytes[v * 4], texBytes[v * 4 + 1],
								texBytes[v * 4 + 2], texBytes[v * 4 + 3] }).getFloat();
					}

					int normals = in.readInt();
					byte[] normalBytes = new byte[in.readInt()];
					float[] normalices = new float[normals];
					in.readFully(normalBytes);
					normalBytes = decompress(normalBytes);
					for (int v = 0; v < normals; v++) {
						normalices[v] = ByteBuffer.wrap(new byte[] { normalBytes[v * 4], normalBytes[v * 4 + 1],
								normalBytes[v * 4 + 2], normalBytes[v * 4 + 3] }).getFloat();
					}

					int indis = in.readInt();
					byte[] indiBytes = new byte[in.readInt()];
					int[] indiices = new int[indis];
					in.readFully(indiBytes);
					indiBytes = decompress(indiBytes);
					for (int v = 0; v < indis; v++) {
						indiices[v] = ByteBuffer.wrap(new byte[] { indiBytes[v * 4], indiBytes[v * 4 + 1],
								indiBytes[v * 4 + 2], indiBytes[v * 4 + 3] }).getInt();
					}

					models.put(in.readUTF(), new Model(vertices, texices, normalices, indiices));
				}

				in.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		List<File> list = FileManager.getFiles(folder);
		for (File f : list) {
			if (f.getName().endsWith(".dae")) {
				if (!models.containsKey(f.getName())) {
					loadModel(f.getPath(), f.getName());
				}
			}
		}
		System.out.println("[ResourceManager]: " + models.size() + " models loaded in "
				+ (int) ((System.nanoTime() - time) / 100.0) / 10000.0 + "ms");
	}

	public static void loadModelAtlas() {
		File f = new File("res/data/atlas/atlas_model.json");
		if (f.exists()) {
			try {
				JSONArray ja = (JSONArray) new JSONParser().parse(new FileReader(f));
				for (int i = 0; i < ja.size(); i++) {
					JSONObject jo = (JSONObject) ja.get(i);

					String source = (String) jo.get("source");
					JSONArray copies = (JSONArray) jo.get("copies");
					for (int j = 0; j < copies.size(); j++) {
						models.put((String) copies.get(j), models.get(source));
					}
				}

				System.out.println("[ResourceManager]: Model atlas loaded with " + models.size() + " models");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void loadAudio() {
		for (File f : FileManager.getFiles("res/audio")) {
			if (f.getName().endsWith(".ogg")) {
				AudioManager.loadAudio(f);
			}
		}
	}

	public static void loadShaders() {
		long time = System.nanoTime();
		new ShaderAdditiveBloom("additive_bloom");
		new ShaderDrawImage("draw_image");
		new ShaderGaussianBlur("gaussian_blur");
		new ShaderMain3DBloom("main_3d_bloom");
		new ShaderMain3D("main_3d");
		new ShaderMain3DCrystal("main_3d_crystal");
		new ShaderShadow3D("shadow_3d");
		new ShaderStatic3D("static_3d");
		new ShaderMain3DNoBloom("main_3d_nobloom");
		new ShaderMain3DNoBloomTexColor("main_3d_nobloom_texcolor");
		new ShaderMain3DMedial("main_3d_medial");
		new ShaderMain3DEnemy("main_3d_enemy");
		new ShaderMain3DHousing("main_3d_housing");
		new ShaderMain3DWater("main_3d_water");
		new ShaderMain3DLava("main_3d_lava");
		new ShaderStatic3D("static_3d_editor");
		new ShaderStatic3DCrafting("static_3d_crafting");
		new ShaderDeferredLighting("deferred_lighting");
		System.out.println("[ResourceManager]: " + shaders.size() + " shaders loaded in "
				+ (int) ((System.nanoTime() - time) / 100.0) / 10000.0 + "ms");
	}

	public static void loadTextures(String folder) {
		long time = System.nanoTime();

		for (File pack : FileManager.getFiles(folder)) {
			if (pack.getName().endsWith(".pck")) {
				try {
					DataInputStream in = new DataInputStream(new FileInputStream(pack));

					int size = in.readInt();
					for (int i = 0; i < size; i++) {
						int width = in.readInt();
						int height = in.readInt();
						byte[] data = new byte[in.readInt()];
						in.readFully(data);

						textures.put(in.readUTF(), new Texture(width, height, decompress(data)));
					}
					in.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		List<File> list = FileManager.getFiles(folder);
		for (File f : list) {
			if (f.getName().endsWith(".png")) {
				if (!textures.containsKey(f.getName())) {
					textures.put(f.getName(), new Texture(f.getPath()));
				}
			}
		}

		System.out.println("[ResourceManager]: " + textures.size() + " textures loaded in "
				+ (int) ((System.nanoTime() - time) / 100.0) / 10000.0 + "ms");
	}

	public static void buildTextures() {
		for (Texture t : textures.values()) {
			if (!t.built) {
				t.build();
			}
		}
	}

	public static void buildModels() {
		for (Model m : models.values()) {
			if (!m.built) {
				m.build();
			}
		}
	}

	public static void loadTextureAtlas() {
		File f = new File("res/data/atlas/atlas_texture.json");
		if (f.exists()) {
			try {
				JSONArray ja = (JSONArray) new JSONParser().parse(new FileReader(f));
				for (int i = 0; i < ja.size(); i++) {
					JSONObject jo = (JSONObject) ja.get(i);

					String source = (String) jo.get("source");
					JSONArray copies = (JSONArray) jo.get("copies");
					for (int j = 0; j < copies.size(); j++) {
						textures.put((String) copies.get(j), textures.get(source));
					}
				}

				System.out.println("[ResourceManager]: Texture atlas loaded with " + textures.size() + " textures");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private static void loadModel(String file, String name) {
		AIScene scene = Assimp.aiImportFile(file,
				Assimp.aiProcess_JoinIdenticalVertices | Assimp.aiProcess_Triangulate);

		if (scene == null) {
			System.err.println("[FileManager]: There was an error loading the model  " + file);
		}

		AIMesh mesh = AIMesh.create(scene.mMeshes().get(0));
		int vertexCount = mesh.mNumVertices();

		AIVector3D.Buffer vertices = mesh.mVertices();
		AIVector3D.Buffer normals = mesh.mNormals();

		float[] vertexList = new float[vertexCount * 3];
		float[] normalList = new float[vertexCount * 3];
		float[] texCoordList = new float[vertexCount * 2];

		for (int i = 0; i < vertexCount; i++) {
			AIVector3D vertex = vertices.get(i);
			vertexList[i * 3] = vertex.x();
			vertexList[i * 3 + 1] = vertex.y();
			vertexList[i * 3 + 2] = vertex.z();

			AIVector3D normal = normals.get(i);
			normalList[i * 3] = normal.x();
			normalList[i * 3 + 1] = normal.y();
			normalList[i * 3 + 2] = normal.z();

			if (mesh.mNumUVComponents().get(0) != 0) {
				AIVector3D texture = mesh.mTextureCoords(0).get(i);
				texCoordList[i * 2] = texture.x();
				texCoordList[i * 2 + 1] = texture.y();
			}
		}

		int faceCount = mesh.mNumFaces();
		int[] indicesList = new int[faceCount * 3];

		AIFace.Buffer indices = mesh.mFaces();

		for (int i = 0; i < faceCount; i++) {
			AIFace face = indices.get(i);
			indicesList[i * 3] = face.mIndices().get(0);
			indicesList[i * 3 + 1] = face.mIndices().get(1);
			indicesList[i * 3 + 2] = face.mIndices().get(2);
		}

		models.put(name, new Model(vertexList, texCoordList, normalList, indicesList));
	}

	public static byte[] compress(byte[] in) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			DeflaterOutputStream defl = new DeflaterOutputStream(out);
			defl.write(in);
			defl.flush();
			defl.close();

			return out.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(150);
			return null;
		}
	}

	public static byte[] decompress(byte[] in) {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InflaterOutputStream infl = new InflaterOutputStream(out);
			infl.write(in);
			infl.flush();
			infl.close();

			return out.toByteArray();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(150);
			return null;
		}
	}

}
