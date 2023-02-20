import network.lobby.GameServer;
import network.lobby.ServerHandler;
import server_interface.ServerPanel;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

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

		try {
			GameServer server = new GameServer(2678, panel);
			server.setPacketHandler(ServerHandler.class);
			server.start();
			frame.add(panel);

			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					server.stop();
					super.windowClosing(e);
					e.getWindow().dispose();
				}
			});

			frame.pack();

			frame.setVisible(true);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
