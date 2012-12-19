package nl.groep5.xchange;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import nl.groep5.xchange.models.Peer;

public class Communicator {
	private static ObservableList<Peer> peers = FXCollections
			.observableArrayList();
	private static NameServer nameServer = new NameServer(
			Settings.getNameServerIp(), Settings.getNameServerPort());

	public static void signUpToNameServer() {
		try {
			InetAddress thisIp = InetAddress.getLocalHost();

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void updatePeers() {
		try {
			String result = nameServer.sendCommand("LIST");
			peers.clear();
			for (String s : result.split(" ")) {
				peers.add(new Peer(s));
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ObservableList<Peer> getPeers() {
		return peers;
	}
}
