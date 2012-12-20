package nl.groep5.xchange.externalInput;

import java.net.ServerSocket;

import nl.groep5.xchange.Settings;

public class OtherPeerListener extends Thread {

	@Override
	public void run() {
		try {
			listenAndHandle();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void listenAndHandle() throws Exception {
		ServerSocket ss = new ServerSocket(Settings.getIncomingConnectionPort());

		// listen for incoming connections
		while (true) {
			IncomingConnectionHandler incomingConnectionHandler = new IncomingConnectionHandler(
					ss.accept());
			incomingConnectionHandler.start();
		}
	}
}
