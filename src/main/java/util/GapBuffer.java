package util;

public class GapBuffer {
	private static int DEFAULT_BUFFER_GROW_CAPACITY = 10;

	char[] buffer;
	//gap includes gapStart, excludes gapEnd
	int gapStart;
	int gapEnd;

	int numChars;

	int maxCapacity;
	int bufferGrowCapacity;

	public GapBuffer() {
		this.buffer = new char[DEFAULT_BUFFER_GROW_CAPACITY];
		this.gapStart = 0;
		this.gapEnd = DEFAULT_BUFFER_GROW_CAPACITY;
		this.maxCapacity = -1;
		this.bufferGrowCapacity = DEFAULT_BUFFER_GROW_CAPACITY;

		this.numChars = 0;
	}

	public GapBuffer(String defaultText) {
		this.buffer = new char[defaultText.length() + DEFAULT_BUFFER_GROW_CAPACITY];
		this.gapStart = defaultText.length();
		this.gapEnd = defaultText.length() + DEFAULT_BUFFER_GROW_CAPACITY;
		this.maxCapacity = -1;
		this.bufferGrowCapacity = DEFAULT_BUFFER_GROW_CAPACITY;

		this.numChars = 0;

		for (int i = 0; i < defaultText.length(); i++) {
			this.buffer[i] = defaultText.charAt(i);
		}
	}

	public GapBuffer(int maxCapacity, int bufferGrowCapacity) {
		this.buffer = new char[bufferGrowCapacity];
		this.gapStart = 0;
		this.gapEnd = bufferGrowCapacity;
		this.maxCapacity = maxCapacity;
		this.bufferGrowCapacity = bufferGrowCapacity;

		numChars = 0;
	}

	public GapBuffer(String defaultText, int maxCapacity, int bufferGrowCapacity) {
		this.buffer = new char[defaultText.length() + bufferGrowCapacity];
		this.gapStart = defaultText.length();
		this.gapEnd = defaultText.length() + bufferGrowCapacity;
		this.maxCapacity = maxCapacity;
		this.bufferGrowCapacity = bufferGrowCapacity;

		this.numChars = defaultText.length();

		for (int i = 0; i < defaultText.length(); i++) {
			this.buffer[i] = defaultText.charAt(i);
		}
	}

	public void left() {
		if (gapStart > 0) {
			gapStart--;
			gapEnd--;
			buffer[gapEnd] = buffer[gapStart];
		}
	}

	public void right() {
		if (gapEnd < buffer.length) {
			buffer[gapStart] = buffer[gapEnd];
			gapStart++;
			gapEnd++;
		}
	}

	public void insert(char c) {
		if (maxCapacity > 0 && numChars >= maxCapacity) {
			return;
		}
		if (gapStart == gapEnd) {
			growBuffer();
		}
		buffer[gapStart] = c;
		gapStart++;
		numChars++;
	}

	public void delete() {
		if (gapStart > 0) {
			gapStart--;
			numChars--;
		}
	}

	public void growBuffer() {
		char[] newBuffer = new char[buffer.length + bufferGrowCapacity];
		System.arraycopy(buffer, 0, newBuffer, 0, gapStart);
		System.arraycopy(buffer, gapEnd, newBuffer, gapEnd + bufferGrowCapacity, buffer.length - gapEnd);
		gapEnd += bufferGrowCapacity;
		buffer = newBuffer;
	}

	public void setPosition(int index) {
		if (index < gapStart) {
			while (gapStart > index && gapStart > 0) {
				left();
			}
		}
		else {
			while (gapStart < index && gapEnd < buffer.length) {
				right();
			}
		}
	}

	public String getText() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < gapStart; i++) {
			sb.append(buffer[i]);
		}
		for (int i = gapEnd; i < buffer.length; i++) {
			sb.append(buffer[i]);
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < gapStart; i++) {
			sb.append(buffer[i]);
		}
		sb.append('[');
		for (int i = gapStart; i < gapEnd; i++) {
			sb.append(buffer[i]);
		}
		sb.append(']');
		for (int i = gapEnd; i < buffer.length; i++) {
			sb.append(buffer[i]);
		}
		return sb.toString();
	}
}
