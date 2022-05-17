package render;

import org.lwjgl.BufferUtils;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

public class Shader {
	private int shaderProgramID;

	private String vertexShaderSrc;
	private String fragmentShaderSrc;

	private Map<String, Integer> uniformLocations;

	public Shader(String vertexShaderFile, String fragmentShaderFile) throws IOException, URISyntaxException {
		ClassLoader loader = getClass().getClassLoader();

		InputStream vertexInputStream = loader.getResourceAsStream(vertexShaderFile);
		this.vertexShaderSrc = new String(vertexInputStream.readAllBytes(), StandardCharsets.UTF_8);

		InputStream fragmentInputStream = loader.getResourceAsStream(fragmentShaderFile);
		this.fragmentShaderSrc = new String(fragmentInputStream.readAllBytes(), StandardCharsets.UTF_8);

		vertexInputStream.close();
		fragmentInputStream.close();
	}

	public void compile() {
		int[] status = new int[1];

		int vertexShaderID = glCreateShader(GL_VERTEX_SHADER);
		glShaderSource(vertexShaderID, vertexShaderSrc);
		glCompileShader(vertexShaderID);
		glGetShaderiv(vertexShaderID, GL_COMPILE_STATUS, status);
		if (status[0] != GL_TRUE) {
			System.err.println(glGetShaderInfoLog(vertexShaderID));
		}

		int fragmentShaderID = glCreateShader(GL_FRAGMENT_SHADER);
		glShaderSource(fragmentShaderID, fragmentShaderSrc);
		glCompileShader(fragmentShaderID);
		glGetShaderiv(fragmentShaderID, GL_COMPILE_STATUS, status);
		if (status[0] != GL_TRUE) {
			System.err.println(glGetShaderInfoLog(fragmentShaderID));
		}

		shaderProgramID = glCreateProgram();
		glAttachShader(shaderProgramID, vertexShaderID);
		glAttachShader(shaderProgramID, fragmentShaderID);

		glLinkProgram(shaderProgramID);

		glGetProgramiv(shaderProgramID, GL_LINK_STATUS, status);
		if (status[0] != GL_TRUE) {
			System.err.println(glGetProgramInfoLog(shaderProgramID));
			String errorLog = glGetProgramInfoLog(shaderProgramID);
			System.err.println(errorLog);
			glDeleteProgram(shaderProgramID);
		}

		glDetachShader(shaderProgramID, vertexShaderID);
		glDetachShader(shaderProgramID, fragmentShaderID);

		glDeleteShader(vertexShaderID);
		glDeleteShader(fragmentShaderID);

		uniformLocations = new HashMap<>();

		int numUniforms = glGetProgrami(shaderProgramID, GL_ACTIVE_UNIFORMS);

		for (int i = 0; i < numUniforms; i++) {
			IntBuffer size = BufferUtils.createIntBuffer(1);//change to buffer
			IntBuffer type = BufferUtils.createIntBuffer(1);
			String uniformName = glGetActiveUniform(shaderProgramID, i, size, type);
			int uniformLocation = glGetUniformLocation(shaderProgramID, uniformName);
			uniformLocations.put(uniformName, uniformLocation);
		}
	}

	public void bind() {
		glUseProgram(shaderProgramID);
	}

	public void destroy() {
		glDeleteProgram(shaderProgramID);
	}

	public void uploadUniform1f(String uniformName, float val) {
		Integer location = uniformLocations.get(uniformName);
		if (location == null) {
			throw new IllegalArgumentException("No uniform with the given name exists.");
		}
		glUniform1f(location, val);
	}

	public void uploadUniform2f(String uniformName, float x, float y) {
		Integer location = uniformLocations.get(uniformName);
		if (location == null) {
			throw new IllegalArgumentException("No uniform with the given name exists.");
		}
		glUniform2f(location, x, y);
	}

	public void uploadUniform2fv(String uniformName, float[] arr) {
		Integer location = uniformLocations.get(uniformName);
		if (location == null) {
			throw new IllegalArgumentException("No uniform with the given name exists.");
		}
		glUniform2fv(location, arr);
	}

	public void uploadUniform3f(String uniformName, float x, float y, float z) {
		Integer location = uniformLocations.get(uniformName);
		if (location == null) {
			throw new IllegalArgumentException("No uniform with the given name exists.");
		}
		glUniform3f(location, x, y, z);
	}

	public void uploadUniform3fv(String uniformName, float[] arr) {
		Integer location = uniformLocations.get(uniformName);
		if (location == null) {
			throw new IllegalArgumentException("No uniform with the given name exists.");
		}
		glUniform3fv(location, arr);
	}

	public void uploadUniform4f(String uniformName, float x, float y, float z, float w) {
		Integer location = uniformLocations.get(uniformName);
		if (location == null) {
			throw new IllegalArgumentException("No uniform with the given name exists.");
		}
		glUniform4f(location, x, y, z, w);
	}

	public void uploadUniform4fv(String uniformName, float[] arr) {
		Integer location = uniformLocations.get(uniformName);
		if (location == null) {
			throw new IllegalArgumentException("No uniform with the given name exists.");
		}
		glUniform4fv(location, arr);
	}

	public void uploadUniform1i(String uniformName, int val) {
		Integer location = uniformLocations.get(uniformName);
		if (location == null) {
			throw new IllegalArgumentException("No uniform with the given name exists.");
		}
		glUniform1i(location, val);
	}

	public void uploadUniform2i(String uniformName, int x, int y) {
		Integer location = uniformLocations.get(uniformName);
		if (location == null) {
			throw new IllegalArgumentException("No uniform with the given name exists.");
		}
		glUniform2i(location, x, y);
	}

	public void uploadUniform3i(String uniformName, int x, int y, int z) {
		Integer location = uniformLocations.get(uniformName);
		if (location == null) {
			throw new IllegalArgumentException("No uniform with the given name exists.");
		}
		glUniform3i(location, x, y, z);
	}

	public void uploadUniform4i(String uniformName, int x, int y, int z, int w) {
		Integer location = uniformLocations.get(uniformName);
		if (location == null) {
			throw new IllegalArgumentException("No uniform with the given name exists.");
		}
		glUniform4i(location, x, y, z, w);
	}

	public void uploadUniformMatrix2fv(String uniformName, boolean transpose, float[] matrix) {
		Integer location = uniformLocations.get(uniformName);
		if (location == null) {
			throw new IllegalArgumentException("No uniform with the given name exists.");
		}
		glUniformMatrix2fv(location, transpose, matrix);
	}

	public void uploadUniformMatrix3fv(String uniformName, boolean transpose, float[] matrix) {
		Integer location = uniformLocations.get(uniformName);
		if (location == null) {
			throw new IllegalArgumentException("No uniform with the given name exists.");
		}
		glUniformMatrix3fv(location, transpose, matrix);
	}

	public void uploadUniformMatrix4fv(String uniformName, boolean transpose, float[] matrix) {
		Integer location = uniformLocations.get(uniformName);
		if (location == null) {
			throw new IllegalArgumentException("No uniform with the given name exists.");
		}
		glUniformMatrix4fv(location, transpose, matrix);
	}

	public void bindTexture2D(int textureID) {
		glBindTexture(GL_TEXTURE_2D, textureID);
	}
}
