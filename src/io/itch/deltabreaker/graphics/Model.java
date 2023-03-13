package io.itch.deltabreaker.graphics;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL40;

import io.itch.deltabreaker.core.ResourceManager;
import io.itch.deltabreaker.core.Startup;
import io.itch.deltabreaker.graphics.shader.Shader;
import io.itch.deltabreaker.math.Matrix4f;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;

public class Model {

	public float[] storedVertices;
	public float[] storedTexCoords;
	public float[] storedNormals;
	public int[] storedIndices;

	private int drawCount;
	private int vertexID;
	private int textureID;
	private int normalID;
	private int indexID;
	private int shadeID;
	private int modelID;

	private int sample = 0;

	public boolean built = false;

	public Model(float[] vertices, float[] texCoords, float[] normals, int[] indices) {
		drawCount = indices.length;

		storedVertices = vertices;
		storedTexCoords = texCoords;
		storedNormals = normals;
		storedIndices = indices;
	}

	public void build() {
		vertexID = GL40.glGenBuffers();
		GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, vertexID);
		GL40.glBufferData(GL40.GL_ARRAY_BUFFER, createFloatBuffer(storedVertices), GL40.GL_STATIC_DRAW);

		textureID = GL40.glGenBuffers();
		GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, textureID);
		GL40.glBufferData(GL40.GL_ARRAY_BUFFER, createFloatBuffer(storedTexCoords), GL40.GL_STATIC_DRAW);

		normalID = GL40.glGenBuffers();
		GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, normalID);
		GL40.glBufferData(GL40.GL_ARRAY_BUFFER, createFloatBuffer(storedNormals), GL40.GL_STATIC_DRAW);

		shadeID = GL40.glGenBuffers();
		modelID = GL40.glGenBuffers();

		indexID = GL40.glGenBuffers();
		GL40.glBindBuffer(GL40.GL_ELEMENT_ARRAY_BUFFER, indexID);
		GL40.glBufferData(GL40.GL_ELEMENT_ARRAY_BUFFER, createIntBuffer(storedIndices), GL40.GL_STATIC_DRAW);

		GL40.glBindBuffer(GL40.GL_ELEMENT_ARRAY_BUFFER, 0);
		GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, 0);

		GL40.glEnableVertexAttribArray(0);
		GL40.glEnableVertexAttribArray(1);
		GL40.glEnableVertexAttribArray(2);
		GL40.glEnableVertexAttribArray(3);
		GL40.glEnableVertexAttribArray(4);
		GL40.glEnableVertexAttribArray(5);
		GL40.glEnableVertexAttribArray(6);
		GL40.glEnableVertexAttribArray(7);
		GL40.glEnableVertexAttribArray(8);

		built = true;
	}

	public void render(Vector3f position, Vector3f rotation, Vector3f scale, Texture texture, Shader shader, Material material, Vector4f shade, boolean shadow) {
		if (!Startup.shadowMapping || shadow) {
			texture.bind(sample);
			if (Startup.shadowMapping) {
				Startup.shadowMap.depthMap.bind(sample + 1);
				shader = ResourceManager.shaders.get("shadow_3d");
			}
			shader.bind();
			shader.use(sample, material);

			GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, vertexID);
			GL40.glVertexAttribPointer(0, 3, GL40.GL_FLOAT, false, 0, 0);

			GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, textureID);
			GL40.glVertexAttribPointer(1, 2, GL40.GL_FLOAT, false, 0, 0);

			GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, normalID);
			GL40.glVertexAttribPointer(2, 3, GL40.GL_FLOAT, false, 0, 0);

			GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, shadeID);
			GL40.glBufferData(GL40.GL_ARRAY_BUFFER, createFloatBuffer(shade.getElements()), GL40.GL_DYNAMIC_DRAW);
			GL40.glVertexAttribPointer(3, 4, GL40.GL_FLOAT, false, 0, 0);
			GL40.glVertexAttribDivisor(3, 1);

			GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, modelID);
			Matrix4f result = Matrix4f.transform(position, rotation, scale);
			GL40.glBufferData(GL40.GL_ARRAY_BUFFER, createFloatBuffer(result.getAll()), GL40.GL_DYNAMIC_DRAW);
			Matrix4f.release(result);

			int size = 16;
			for (int i = 0; i < 4; i++) {
				GL40.glVertexAttribPointer(4 + i, 4, GL40.GL_FLOAT, false, 4 * size, i * size);
				GL40.glVertexAttribDivisor(4 + i, 1);
			}

			GL40.glBindBuffer(GL40.GL_ELEMENT_ARRAY_BUFFER, indexID);
			GL40.glDrawElementsInstanced(GL40.GL_TRIANGLES, drawCount, GL40.GL_UNSIGNED_INT, 0, 1);

			GL40.glBindBuffer(GL40.GL_ELEMENT_ARRAY_BUFFER, 0);
			GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, 0);
		}
	}

	public void renderBatchInstanced(int amount, ArrayList<Matrix4f> matrices, Texture texture, Shader shader, Material material, ArrayList<Vector4f> shade, boolean shadow) {
		if (!Startup.shadowMapping || shadow) {
			texture.bind(sample);
			if (Startup.shadowMapping) {
				Startup.shadowMap.depthMap.bind(sample + 1);
				shader = ResourceManager.shaders.get("shadow_3d");
			}
			shader.bind();
			shader.use(sample, material);

			GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, vertexID);
			GL40.glVertexAttribPointer(0, 3, GL40.GL_FLOAT, false, 0, 0);

			GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, textureID);
			GL40.glVertexAttribPointer(1, 2, GL40.GL_FLOAT, false, 0, 0);

			GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, normalID);
			GL40.glVertexAttribPointer(2, 3, GL40.GL_FLOAT, false, 0, 0);

			GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, shadeID);
			GL40.glBufferData(GL40.GL_ARRAY_BUFFER, createFloatBuffer4f(shade), GL40.GL_DYNAMIC_DRAW);
			GL40.glVertexAttribPointer(3, 4, GL40.GL_FLOAT, false, 0, 0);
			GL40.glVertexAttribDivisor(3, 1);

			GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, modelID);
			GL40.glBufferData(GL40.GL_ARRAY_BUFFER, createFloatArray(matrices), GL40.GL_DYNAMIC_DRAW);

			int size = 16;
			for (int i = 0; i < 4; i++) {
				GL40.glVertexAttribPointer(4 + i, 4, GL40.GL_FLOAT, false, 4 * size, i * size);
				GL40.glVertexAttribDivisor(4 + i, 1);
			}

			GL40.glBindBuffer(GL40.GL_ELEMENT_ARRAY_BUFFER, indexID);
			GL40.glDrawElementsInstanced(GL40.GL_TRIANGLES, drawCount, GL40.GL_UNSIGNED_INT, 0, amount);

			GL40.glBindBuffer(GL40.GL_ELEMENT_ARRAY_BUFFER, 0);
			GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, 0);
		}
	}

	public void renderWater(int amount, ArrayList<Vector4f> positions, Texture texture, Shader shader, Material material) {
		if (!Startup.shadowMapping) {
			texture.bind(sample);
			shader.bind();
			shader.use(sample, material);

			GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, vertexID);
			GL40.glVertexAttribPointer(0, 3, GL40.GL_FLOAT, false, 0, 0);

			GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, textureID);
			GL40.glVertexAttribPointer(1, 2, GL40.GL_FLOAT, false, 0, 0);

			GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, normalID);
			GL40.glVertexAttribPointer(2, 3, GL40.GL_FLOAT, false, 0, 0);

			GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, shadeID);
			GL40.glBufferData(GL40.GL_ARRAY_BUFFER, createFloatBuffer4f(positions), GL40.GL_DYNAMIC_DRAW);
			GL40.glVertexAttribPointer(3, 4, GL40.GL_FLOAT, false, 0, 0);
			GL40.glVertexAttribDivisor(3, 1);

			GL40.glBindBuffer(GL40.GL_ELEMENT_ARRAY_BUFFER, indexID);
			GL40.glDrawElementsInstanced(GL40.GL_TRIANGLES, drawCount, GL40.GL_UNSIGNED_INT, 0, amount);

			GL40.glBindBuffer(GL40.GL_ELEMENT_ARRAY_BUFFER, 0);
			GL40.glBindBuffer(GL40.GL_ARRAY_BUFFER, 0);
		}
	}
	
	public static float[] createFloatArray(ArrayList<Vector3f> positions, ArrayList<Vector3f> rotations, ArrayList<Vector3f> scales) {
		float[] array = new float[positions.size() * 16];
		for (int i = 0; i < positions.size(); i++) {
			float[] matrix = Matrix4f.transformArray(positions.get(i), rotations.get(i), scales.get(i));

			int position = i * 16;
			for (int j = 0; j < 16; j++) {
				array[position + j] = matrix[j];
			}
		}
		return array;
	}

	public static float[] createFloatArray(ArrayList<Matrix4f> matrices) {
		float[] array = new float[matrices.size() * 16];
		for (int i = 0; i < matrices.size(); i++) {
			float[] matrix = matrices.get(i).getAll();

			int position = i * 16;
			for (int j = 0; j < 16; j++) {
				array[position + j] = matrix[j];
			}
		}
		return array;
	}

	public static FloatBuffer createFloatBuffer4f(ArrayList<Vector4f> data) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.size() * 4);
		for (Vector4f v : data) {
			buffer.put(v.getElements());
		}
		buffer.flip();
		return buffer;
	}

	public static FloatBuffer createFloatBuffer(float[] data) {
		FloatBuffer buffer = BufferUtils.createFloatBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}

	public static IntBuffer createIntBuffer(int[] data) {
		IntBuffer buffer = BufferUtils.createIntBuffer(data.length);
		buffer.put(data);
		buffer.flip();
		return buffer;
	}

}