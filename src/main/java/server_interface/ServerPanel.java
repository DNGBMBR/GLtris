package server_interface;

import network.lobby.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ServerPanel extends JPanel{
	JTextArea textAreaLog;
	JTextArea textAreaPlayerList;

	java.util.List<CommandCallback> commandCallbacks = new ArrayList<>();

	static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

	StringBuilder log = new StringBuilder();

	public ServerPanel() {

		this.setLayout(new GridLayout(1, 1));

		//bottom left
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new GridLayout(2, 1));

		JPanel performancePanel = new JPanel();
		performancePanel.setLayout(new GridLayout(1, 1));
		performancePanel.setBorder(BorderFactory.createTitledBorder("Performance"));
		leftPanel.add(performancePanel);

		JPanel playerListPanel = new JPanel();
		playerListPanel.setLayout(new GridLayout(1, 1));
		playerListPanel.setBorder(BorderFactory.createTitledBorder("Players"));
		textAreaPlayerList = new JTextArea();
		textAreaPlayerList.setLineWrap(true);
		textAreaPlayerList.setEditable(false);
		JScrollPane scrollPanePlayerList = new JScrollPane(textAreaPlayerList);
		playerListPanel.add(scrollPanePlayerList);
		leftPanel.add(playerListPanel);

		this.add(leftPanel);

		//right
		JPanel logPanel = new JPanel();
		logPanel.setLayout(new BorderLayout());
		logPanel.setBorder(BorderFactory.createTitledBorder("Log"));
		textAreaLog = new JTextArea();
		textAreaLog.setLineWrap(false);
		textAreaLog.setEditable(false);
		JScrollPane scrollPaneLog = new JScrollPane(textAreaLog);
		JPanel logTextPanel = new JPanel();
		logTextPanel.setLayout(new GridLayout(1, 1));
		logTextPanel.add(scrollPaneLog);
		logPanel.add(logTextPanel, BorderLayout.CENTER);

		TextField textFieldCommandLine = new TextField();
		textFieldCommandLine.setEditable(true);
		textFieldCommandLine.addActionListener(new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String text = textFieldCommandLine.getText();
				parseCommand(text);
				textFieldCommandLine.setText("");
			}
		});
		logPanel.add(textFieldCommandLine, BorderLayout.SOUTH);
		this.add(logPanel);
	}

	private void parseCommand(String text) {
		for (CommandCallback callback : commandCallbacks) {
			callback.onCommandReceived(text);
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(920, 512);
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
	}

	//automatically prepends timestamp to string
	public void log(String s) {
		log.append("[").append(LocalTime.now().format(TIME_FORMAT)).append("] ").append(s).append("\n");
		textAreaLog.setText(log.toString());
	}

	public void updatePlayers(Collection<Player> players) {
		StringBuilder s = new StringBuilder();
		for (Player player : players) {
			String state = "";
			if (player.isSpectator()) {
				state = " S";
			}
			else if (player.isReady()) {
				state = " R";
			}
			s.append(player.getName()).append(state).append("\n");
		}
		textAreaPlayerList.setText(s.toString());
	}


	public void addCommandCallback(CommandCallback callback) {
		commandCallbacks.add(callback);
	}
}
