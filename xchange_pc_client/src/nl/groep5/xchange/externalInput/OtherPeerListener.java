package nl.groep5.xchange.externalInput;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;

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
			try {
				IncomingConnectionHandler incomingConnectionHandler = new IncomingConnectionHandler(
						serverSocket.accept());
				incomingConnectionHandler.start();
			} catch (SocketException e) {
				// error on application closed.
				System.out
						.println("SocketExceiont OtherPeerLister probably program closed");
			}

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
