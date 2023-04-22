package network.general;

public abstract class MessageSerializerLarge{
	public static final int MAX_SEGMENT_SIZE = 4090;
	private static final int HEADER_SIZE = 4;
	private static final int MAX_DATA_PER_SEGMENT = MessageSerializerLarge.MAX_SEGMENT_SIZE - HEADER_SIZE;

	public MessageSerializerLarge() {

	}

	public MessageSerializerLarge(byte[][] data) {
		deserialize(data);
	}

	public byte[][] segment(byte[] data) {
		int numSegments = data.length / MAX_DATA_PER_SEGMENT + (data.length % MAX_DATA_PER_SEGMENT == 0 ? 0 : 1); //ceiling division
		byte[][] segmentedData = new byte[numSegments][];
		for (int i = 0; i < numSegments; i++) {
			int segmentedDataLength = i == segmentedData.length - 1 ? (data.length % MAX_DATA_PER_SEGMENT) + HEADER_SIZE : MessageSerializerLarge.MAX_SEGMENT_SIZE;
			segmentedData[i] = new byte[segmentedDataLength];
			segmentedData[i][0] = MessageConstants.SERVER;
			segmentedData[i][1] = MessageConstants.MESSAGE_SERVER_LOBBY_STATE;
			segmentedData[i][2] = (byte) i;
			segmentedData[i][3] = (byte) numSegments;
			System.arraycopy(data, MAX_DATA_PER_SEGMENT * i, segmentedData[i], HEADER_SIZE, segmentedDataLength - HEADER_SIZE);
		}
		return segmentedData;
	}

	public byte[] assemble(byte[][] segmentedData) {
		int dataLength = 0;
		for (int i = 0; i < segmentedData.length; i++) {
			dataLength += segmentedData[i].length - HEADER_SIZE;
		}

		byte[] fullData = new byte[dataLength];
		for (int i = 0, index = 0; i < segmentedData.length; i++) {
			System.arraycopy(segmentedData[i], HEADER_SIZE, fullData, index, segmentedData[i].length - HEADER_SIZE);
			index += segmentedData[i].length - HEADER_SIZE;
		}

		return fullData;
	}

	public abstract byte[][] serialize();

	public abstract void deserialize(byte[][] data);

	public static int getSegmentNumber(byte[] data) {
		assert data.length >= HEADER_SIZE;
		return data[2] & 0xFF;
	}

	public static int getNumSegments(byte[] data) {
		assert data.length >= HEADER_SIZE && data[3] > 0;
		return data[3] & 0xFF;
	}
}
