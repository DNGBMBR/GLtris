package render;

public abstract class Batch {
	public abstract boolean isEmpty();

	public abstract boolean isEnoughRoom(int numVerticesToAdd);

	//need some way to add arbitrary vertex data

	public abstract void flush();
}
