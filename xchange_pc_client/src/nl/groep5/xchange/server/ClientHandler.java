package nl.groep5.xchange.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import nl.groep5.xchange.Settings;

public class ClientHandler extends Thread {

	private static final String STOP_COMMAND = "STOP";
	private Socket client;
	private PrintWriter printWriter;
	private BufferedReader bufferedReader;

	public ClientHandler(Socket client) {
		this.client = client;
		try {
			this.printWriter = new PrintWriter(client.getOutputStream(), true);
			this.bufferedReader = new BufferedReader(new InputStreamReader(
					client.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (!client.isClosed()) {
			try {
				String input = bufferedReader.readLine();
				if (input == null)
					continue;

				String returnMessage = "OK";
				if (input.equals(STOP_COMMAND)) {
					returnMessage = "OK" + Settings.getSplitChar();
				}

				System.out.println("INPUT " + input);
				System.out.println("Returning OK");
				printWriter.println(returnMessage);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
