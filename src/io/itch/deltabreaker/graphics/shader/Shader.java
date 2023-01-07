package io.itch.deltabreaker.graphics.shader;

import java.io.File;
import java.nio.FloatBuffer;

import org.lwjgl.opengl.GL40;
import org.lwjgl.system.MemoryStack;

import io.itch.deltabreaker.core.FileManager;
import io.itch.deltabreaker.core.ResourceManager;
import io.itch.deltabreaker.graphics.Material;
import io.itch.deltabreaker.math.Matrix4f;
import io.itch.deltabreaker.math.Vector3f;
import io.itch.deltabreaker.math.Vector4f;

public abstract class Shader {

	private int programID;
	private int vertexID;
	private int geoID;
	private int fragmentID;

	public boolean setStaticUniforms = false;

	public Shader(String file) {
		programID = GL40.glCreateProgram();

		vertexID = GL40.glCreateShader(GL40.GL_VERTEX_SHADER);
		GL40.glShaderSource(vertexID, FileManager.readAsString("res/shader/" + file + "/" + file + ".vert"));
		GL40.glCompileShader(vertexID);
		if (GL40.glGetShaderi(vertexID, GL40.GL_COMPILE_STATUS) != 1) {
			System.err.println("[Shader]: There was an error compiling " + file + "/" + file + ".vert");
			System.err.println(GL40.glGetShaderInfoLog(vertexID));
			System.exit(0);
		}

		if (new File("res/shader/" + file + "/" + file + ".geo").exists()) {
			geoID = GL40.glCreateShader(GL40.GL_GEOMETRY_SHADER);
			GL40.glShaderSource(geoID, FileManager.readAsString("res/shader/" + file + "/" + file + ".geo"));
			GL40.glCompileShader(geoID);
			if (GL40.glGetShaderi(geoID, GL40.GL_COMPILE_STATUS) != 1) {
				System.err.println("[Shader]: There was an error compiling " + file + "/" + file + ".geo");
				System.err.println(GL40.glGetShaderInfoLog(geoID));
				System.exit(0);
			}
		}

		fragmentID = GL40.glCreateShader(GL40.GL_FRAGMENT_SHADER);
		GL40.glShaderSource(fragmentID, FileManager.readAsString("res/shader/" + file + "/" + file + ".frag"));
		GL40.glCompileShader(fragmentID);
		if (GL40.glGetShaderi(fragmentID, GL40.GL_COMPILE_STATUS) != 1) {
			System.err.println("[Shader]: There was an error compiling " + file + "/" + file + ".frag");
			System.err.println(GL40.glGetShaderInfoLog(fragmentID));
			System.exit(0);
		}

		GL40.glAttachShader(programID, vertexID);
		GL40.glAttachShader(programID, geoID);
		GL40.glAttachShader(programID, fragmentID);

		GL40.glBindAttribLocation(programID, 0, "vertices");
		GL40.glBindAttribLocation(programID, 1, "tex_coords");

		GL40.glLinkProgram(programID);
		if (GL40.glGetProgrami(programID, GL40.GL_LINK_STATUS) != 1) {
			System.err.println("[Shader]: There was an error linking the program for shader " + file);
			System.err.println(GL40.glGetProgramInfoLog(programID));
			System.exit(0);
		}
		GL40.glValidateProgram(programID);
		if (GL40.glGetProgrami(programID, GL40.GL_VALIDATE_STATUS) != 1) {
			System.err.println("[Shader]: There was an error validating the program for shader " + file);
			System.err.println(GL40.glGetProgramInfoLog(programID));
			System.exit(0);
		}
		ResourceManager.shaders.put(file, this);
	}

	public abstract void use(int sample, Material material);

	public abstract void setStaticUniforms();

	public void setUniform(String name, boolean value) {
		int location = GL40.glGetUniformLocation(programID, name);
		if (location != -1) {
			GL40.glUniform1i(location, (value) ? 1 : 0);
		}
	}

	public void setUniform(String name, float x, float y) {
		int location = GL40.glGetUniformLocation(programID, name);
		if (location != -1) {
			GL40.glUniform2f(location, x, y);
		}
	}

	public void setUniform(String name, Vector3f value) {
		int location = GL40.glGetUniformLocation(programID, name);
		if (location != -1) {
			GL40.glUniform3f(location, value.getX(), value.getY(), value.getZ());
		}
	}

	public void setUniform(String name, Vector4f value) {
		int location = GL40.glGetUniformLocation(programID, name);
		if (location != -1) {
			GL40.glUniform4f(location, value.getX(), value.getY(), value.getZ(), value.getW());
		}
	}

	public void setUniform(String name, float x, float y, float z, float w) {
		int location = GL40.glGetUniformLocation(programID, name);
		if (location != -1) {
			GL40.glUniform4f(location, x, y, z, w);
		}
	}

	public void setUniform(String name, int value) {
		int location = GL40.glGetUniformLocation(programID, name);
		if (location != -1) {
			GL40.glUniform1i(location, value);
		}
	}

	public void setUniform(String name, float value) {
		int location = GL40.glGetUniformLocation(programID, name);
		if (location != -1) {
			GL40.glUniform1f(location, value);
		}
	}

	public void setUniform(String name, Matrix4f value) {
		int location = GL40.glGetUniformLocation(programID, name);
		if (location != -1) {
			try (MemoryStack stack = MemoryStack.stackPush()) {
				final FloatBuffer matrixBuffer = stack.mallocFloat(16);
				matrixBuffer.put(value.getAll()).flip();
				GL40.glUniformMatrix4fv(location, true, matrixBuffer);
			}
		}
	}

	public void bind() {
		GL40.glUseProgram(programID);
	}

}
