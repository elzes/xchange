package nl.groep5.xchange.communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.naming.CommunicationException;

public class ExternalDeviceCommunication {

	protected static final int SOCKET_TIMEOUT = 5;
	protected String ip;
	protected int port;

	public ExternalDeviceCommunication(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public String sendCommand(String command) throws UnknownHostException,
			IOException, ConnectException, CommunicationException {

		try {
			Socket socket = new Socket();
			socket.connect(new InetSocketAddress(ip, port),
					SOCKET_TIMEOUT * 1000);
			BufferedReader bufferedReader = new BufferedReader(
					new InputStreamReader(socket.getInputStream()));

			PrintWriter printWriter = new PrintWriter(socket.getOutputStream(),
					true);
			System.out.println("Going to write to " + this.getClass().getName()
					+ " command:  " + command);
			printWriter.println(command);

			String line = null;
			do {
				line = bufferedReader.readLine();
			} while (line == null);

			if (line.startsWith("FAIL"))
				throw new CommunicationException();

			System.out.println("Done with command " + command + " to device "
					+ this.getClass().getName());
			return line;
		} catch (ConnectException e) {
			throw new UnknownHostException();
		}
	}
}