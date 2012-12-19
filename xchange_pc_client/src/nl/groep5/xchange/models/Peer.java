package nl.groep5.xchange.models;

public class Peer {

	private String ip;

	public Peer(String ip) {
		this.ip = ip;
	}

	@Override
	public String toString() {
		return ip;
	}
}
