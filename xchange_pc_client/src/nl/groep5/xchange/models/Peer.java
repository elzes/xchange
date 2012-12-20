package nl.groep5.xchange.models;

import nl.groep5.xchange.Settings;

public class Peer {

	private String ip;
	private static int port = Settings.getIncomingConnectionPort();

	public Peer(String ip) {
		this.ip = ip;
	}

	@Override
	public String toString() {
		return ip;
	}

	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}
}