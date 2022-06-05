package settings;

import dev.dewy.nbt.Nbt;
import dev.dewy.nbt.io.CompressionType;
import dev.dewy.nbt.tags.array.IntArrayTag;
import dev.dewy.nbt.tags.collection.CompoundTag;

import java.io.File;
import java.io.IOException;

import static org.lwjgl.glfw.GLFW.*;

public class KeybindingSettings {
	private static final String PATH = "./keysettings.nbt";

	private static final String MOVE_LEFT_KEY = "left";
	private static final String MOVE_RIGHT_KEY = "right";
	private static final String SOFT_DROP_KEY = "soft_drop";

	private static final String ROTATE_CW_KEY = "cw";
	private static final String ROTATE_CCW_KEY = "ccw";
	private static final String ROTATE_180_KEY = "half";

	private static final String HOLD_KEY = "hold";
	private static final String HARD_DROP_KEY = "hard_drop";

	private static final int[] MOVE_LEFT = {glfwGetKeyScancode(GLFW_KEY_A), 0};
	private static final int[] MOVE_RIGHT = {glfwGetKeyScancode(GLFW_KEY_D), 0};
	private static final int[] SOFT_DROP = {glfwGetKeyScancode(GLFW_KEY_S), 0};

	private static final int[] ROTATE_CW = {glfwGetKeyScancode(GLFW_KEY_SLASH), 0};
	private static final int[] ROTATE_CCW = {glfwGetKeyScancode(GLFW_KEY_PERIOD), 0};
	private static final int[] ROTATE_180 = {glfwGetKeyScancode(GLFW_KEY_COMMA), 0};

	private static final int[] HOLD = {glfwGetKeyScancode(GLFW_KEY_F), 0};
	private static final int[] HARD_DROP = {glfwGetKeyScancode(GLFW_KEY_SPACE), 0};

	private KeybindingSettings() {}

	private static CompoundTag properties;

	private static CompoundTag getProperties() {
		if (properties == null) {
			try {
				Nbt nbt = new Nbt();
				properties = nbt.fromFile(new File(PATH));
			} catch (IOException e) {
				setDefaultProperties();
			}
		}
		return properties;
	}

	public static void saveSettings() {
		CompoundTag p = getProperties();

		File destination = new File(PATH);

		try {
			if(!destination.exists()) {
				if(!destination.createNewFile()) {
					return;
				}
			}
			Nbt nbt = new Nbt();
			nbt.toFile(p, destination, CompressionType.GZIP);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void setDefaultProperties() {
		properties = new CompoundTag();
		properties.put(new IntArrayTag(MOVE_LEFT_KEY, MOVE_LEFT));
		properties.put(new IntArrayTag(MOVE_RIGHT_KEY, MOVE_RIGHT));
		properties.put(new IntArrayTag(SOFT_DROP_KEY, SOFT_DROP));

		properties.put(new IntArrayTag(ROTATE_CW_KEY, ROTATE_CW));
		properties.put(new IntArrayTag(ROTATE_CCW_KEY, ROTATE_CCW));
		properties.put(new IntArrayTag(ROTATE_180_KEY, ROTATE_180));

		properties.put(new IntArrayTag(HOLD_KEY, HOLD));
		properties.put(new IntArrayTag(HARD_DROP_KEY, HARD_DROP));

		saveSettings();
	}

	private static int[] getKeys(String keyBinding) {
		CompoundTag p = getProperties();
		return p.getIntArray(keyBinding).getValue();
	}

	private static void setKey(String keyBinding, int scancode, int index) {
		CompoundTag p = getProperties();
		int[] scancodes = p.getIntArray(keyBinding).getValue();
		if (index < 0 || index >= scancodes.length) {
			throw new IndexOutOfBoundsException("Index not within number of valid keys.");
		}
		scancodes[index] = scancode;
		p.put(new IntArrayTag(keyBinding, scancodes));
	}

	public static int[] getMoveLeftKeys() {
		return getKeys(MOVE_LEFT_KEY);
	}

	public static void setMoveLeftKey(int scancode, int index) {
		setKey(MOVE_LEFT_KEY, scancode, index);
	}

	public static int[] getMoveRightKeys() {
		return getKeys(MOVE_RIGHT_KEY);
	}

	public static void setMoveRightKey(int scancode, int index) {
		setKey(MOVE_RIGHT_KEY, scancode, index);
	}

	public static int[] getSoftDropKeys() {
		return getKeys(SOFT_DROP_KEY);
	}

	public static void setSoftDropKey(int scancode, int index) {
		setKey(SOFT_DROP_KEY, scancode, index);
	}

	public static int[] getRotateCWKeys() {
		return getKeys(ROTATE_CW_KEY);
	}

	public static void setRotateCWKey(int scancode, int index) {
		setKey(ROTATE_CW_KEY, scancode, index);
	}

	public static int[] getRotateCCWKeys() {
		return getKeys(ROTATE_CCW_KEY);
	}

	public static void setRotateCCWKey(int scancode, int index) {
		setKey(ROTATE_CCW_KEY, scancode, index);
	}

	public static int[] getRotate180Keys() {
		return getKeys(ROTATE_180_KEY);
	}

	public static void setRotate180Key(int scancode, int index) {
		setKey(ROTATE_180_KEY, scancode, index);
	}

	public static int[] getHoldKeys() {
		return getKeys(HOLD_KEY);
	}

	public static void setHoldKey(int scancode, int index) {
		setKey(HOLD_KEY, scancode, index);
	}

	public static int[] getHardDropKeys() {
		return getKeys(HARD_DROP_KEY);
	}

	public static void setHardDropKey(int scancode, int index) {
		setKey(HARD_DROP_KEY, scancode, index);
	}
}
