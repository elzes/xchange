package nl.groep5.xchange.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

public class NameServer {

	private static final int SOCKET_TIMEOUT = 5;
	private String ip;
	private int port;

	public NameServer(String nameServerIp, int nameServerPort) {
		this.ip = nameServerIp;
		this.port = nameServerPort;
	}

	public String sendCommand(String command) throws UnknownHostException,
			IOException, ConnectException {

		try {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(ip, port),
					SOCKET_TIMEOUT * 1000);
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

			System.out.println("Done with command " + command);
			return line;
		} catch (ConnectException e) {
			throw new UnknownHostException();
		}

	}
}
