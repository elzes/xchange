package nl.groep5.xchange;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

public class NameServer {

	private String ip;
	private int port;

	public NameServer(String nameServerIp, int nameServerPort) {
		this.ip = nameServerIp;
		this.port = nameServerPort;
	}

	public String sendCommand(String command) throws UnknownHostException,
			IOException, ConnectException {

		try {
			Socket socket = new Socket(ip, port);
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));

			PrintWriter printWriter = new PrintWriter(socket.getOutputStream(),
					true);
			System.out.println("Going to write to NameServer command  "
					+ command);
			printWriter.println(command);

			String line = null;
			do {
				line = bufferedReader.readLine();
			} while (line == null);

			if (line.startsWith("FAIL"))
				throw new IOException();

			return line;
		} catch (ConnectException e) {
			e.printStackTrace();
			throw new UnknownHostException();
		}

	}
}
