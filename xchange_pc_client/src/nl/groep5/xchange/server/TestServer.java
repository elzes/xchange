package nl.groep5.xchange.server;

import java.io.IOException;
import java.net.ServerSocket;

import nl.groep5.xchange.Settings;

public class TestServer {

	public static void main(String[] args) {
		new TestServer();
	}

	private ServerSocket socket;
	private boolean running = true;

	public TestServer() {
		try {
			socket = new ServerSocket(Settings.getRouterPort());
			ClientHandler clientHandler;
			while (running) {
				clientHandler = new ClientHandler(socket.accept());
				clientHandler.start();
			}
			socket.close();
			System.out.println("done");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
