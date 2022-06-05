package settings;

import dev.dewy.nbt.Nbt;
import dev.dewy.nbt.io.CompressionType;
import dev.dewy.nbt.tags.collection.CompoundTag;
import dev.dewy.nbt.tags.primitive.*;
import org.ini4j.Wini;

import java.io.*;

public class LocalSettings {
	//units are in frames
	private static final double DEFAULT_ARR = 0.0;
	private static final double DEFAULT_DAS = 8.0;
	private static final int DEFAULT_SDF = 6;

	private static final String PATH = "./settings.nbt";

	private static final String OPTION_SDF = "sdf";
	private static final String OPTION_ARR = "arr";
	private static final String OPTION_DAS = "das";
	private static final String OPTION_DAS_CANCEL = "das_cancel";

	private LocalSettings() {}

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
		properties.put(new IntTag(OPTION_SDF, DEFAULT_SDF));
		properties.put(new DoubleTag(OPTION_ARR, DEFAULT_ARR));
		properties.put(new DoubleTag(OPTION_DAS, DEFAULT_DAS));
		properties.put(new ByteTag(OPTION_DAS_CANCEL, 0));
	}

	public static int getSDF() {
		properties = getProperties();
		IntTag tag = properties.getInt(OPTION_SDF);
		if (tag == null) {
			setDAS(DEFAULT_SDF);
			tag = properties.getInt(OPTION_SDF);
		}
		return tag.getValue();
	}

	public static void setSDF(int sdf) {
		CompoundTag p = getProperties();
		p.put(new IntTag(OPTION_SDF, sdf));
	}

	public static double getARR() {
		properties = getProperties();
		DoubleTag tag = properties.getDouble(OPTION_ARR);
		if (tag == null) {
			setDAS(DEFAULT_ARR);
			tag = properties.getDouble(OPTION_ARR);
		}
		return tag.getValue();
	}

	public static void setARR(double arr) {
		CompoundTag p = getProperties();
		p.put(new DoubleTag(OPTION_ARR, arr));
	}

	public static double getDAS() {
		properties = getProperties();
		DoubleTag tag = properties.getDouble(OPTION_DAS);
		if (tag == null) {
			setDAS(DEFAULT_DAS);
			tag = properties.getDouble(OPTION_DAS);
		}
		return tag.getValue();
	}

	public static void setDAS(double das) {
		CompoundTag p = getProperties();
		p.put(new DoubleTag(OPTION_DAS, das));
	}

	public static boolean getDASCancel() {
		properties = getProperties();
		ByteTag tag = properties.getByte(OPTION_DAS_CANCEL);
		if (tag == null) {
			setDASCancel(false);
			tag = properties.getByte(OPTION_DAS_CANCEL);
		}
		return tag.getValue() != 0;
	}

	public static void setDASCancel(boolean isDASCancel) {
		CompoundTag p = getProperties();
		p.put(new ByteTag(OPTION_DAS_CANCEL, isDASCancel ? 1 : 0));
	}
}
