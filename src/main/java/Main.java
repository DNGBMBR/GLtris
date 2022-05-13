public class Main {
	public static void main(String[] args) {
		Window window = Window.getInstance();
		window.initialize();
		window.run();
		window.destroy();
	}
}
