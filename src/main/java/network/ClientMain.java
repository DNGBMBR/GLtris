package network;

import network.general.*;

import java.net.*;

public class ClientMain {
	public static void main(String[] args) {
		try {
			InetAddress address = InetAddress.getLocalHost();
			Client client = new Client(address, 2678, new OnReceive() {
				@Override
				public void onReceive(SenderThread destination, Object gameInfo) {
					System.out.println("client has received: " + gameInfo);
					destination.sendMessage("poger");
					//TODO: figure out why this is throwing a corruptedstreamexception or whatever
				}
			}, new OnConnect() {
				@Override
				public void onConnect(SenderThread destination) {
					System.out.println("Connected client");
					destination.sendMessage("pppppppp");
				}
			});
			client.start();
			System.out.println("client started");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
	}
}
