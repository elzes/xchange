package nl.groep5.xchange.externalInput;

import java.io.IOException;
import java.net.ServerSocket;

import nl.groep5.xchange.Settings;

public class OtherPeerListener extends Thread {

	private ServerSocket serverSocket;
	private boolean isStopped;

	@Override
	public void run() {
		try {
			listenAndHandle();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void listenAndHandle() throws Exception {
		serverSocket = new ServerSocket(Settings.getIncomingConnectionPort());

		// listen for incoming connections
		while (!isStopped) {
			IncomingConnectionHandler incomingConnectionHandler = new IncomingConnectionHandler(
					serverSocket.accept());
			incomingConnectionHandler.start();
		}
	}

	public void stopListening() {
		try {
			isStopped = true;
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
