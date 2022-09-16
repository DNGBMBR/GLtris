import network.lobby.Server;
import server_interface.ServerPanel;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ServerMain {
	public static void main(String[] args) {
		//TODO: read config file for lobby settings
		createAndShowGUI();
	}

	private static void createAndShowGUI() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}

		JFrame.setDefaultLookAndFeelDecorated(true);

		JFrame frame = new JFrame();
		frame.setTitle("GLTris Server");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		ServerPanel panel = new ServerPanel();
		Server server = new Server(2678, panel);
		server.start();
		frame.add(panel);

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				server.close();
				super.windowClosing(e);
				e.getWindow().dispose();
			}
		});

		frame.pack();

		frame.setVisible(true);
	}

}
