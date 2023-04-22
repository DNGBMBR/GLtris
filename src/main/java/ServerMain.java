import game.pieces.PieceFactory;
import network.lobby.GameServer;
import network.lobby.ServerHandler;
import org.json.simple.parser.ParseException;
import server_interface.ServerPanel;
import settings.GameSettings;
import settings.ServerSettings;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.Properties;

public class ServerMain {
	public static final String PROPERTIES_LOCATION = "./server.properties";

	public static void main(String[] args) {
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

		ServerSettings settings;
		try {
			settings = new ServerSettings(new File(PROPERTIES_LOCATION));
		} catch (Exception e) {
			System.out.println("could not find usual properties, using defaults.");
			settings = new ServerSettings();
			try {
				settings.writeSettings(new File(PROPERTIES_LOCATION));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		try {
			GameServer server = new GameServer(settings, panel);
			server.setPacketHandler(ServerHandler.class);
			server.start();

			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) {
					server.stop();
					super.windowClosing(e);
					e.getWindow().dispose();
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}

		frame.add(panel);

		frame.pack();
		frame.setVisible(true);
	}
}
