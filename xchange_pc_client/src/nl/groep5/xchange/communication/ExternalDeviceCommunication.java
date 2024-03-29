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

import nl.groep5.xchange.Settings;

public class ExternalDeviceCommunication {

	protected static final int SOCKET_TIMEOUT = 5;
	protected String ip;
	protected int port;
	public Socket socket;
	public BufferedReader bufferedReader;
	public PrintWriter printWriter;

	public ExternalDeviceCommunication(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public String sendCommand(String command, boolean keepAlive)
			throws UnknownHostException, IOException, ConnectException,
			CommunicationException {

		if (ip == null)
			throw new CommunicationException("Ip is null");

		try {
			socket = new Socket();
			socket.connect(new InetSocketAddress(ip, port),
					SOCKET_TIMEOUT * 1000);
			bufferedReader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			printWriter = new PrintWriter(socket.getOutputStream(),
					true);
			if (Settings.DEBUG) {
				System.out.println("Going to write to "
						+ this.getClass().getName() + " command:  " + command);
			}
			printWriter.println(command);

			String line = null;
			do {
				line = bufferedReader.readLine();
			} while (line == null);

			if (!keepAlive) {
				printWriter.close();
				bufferedReader.close();
				socket.close();
			}
			if (Settings.DEBUG) {
				System.out.println("Response from " + this.getClass().getName()
						+ " " + line);
			}

			if (line.startsWith("FAIL"))
				throw new CommunicationException();

			if (Settings.DEBUG) {
				System.out.println("Done with command " + command
						+ " to device " + this.getClass().getName());
			}

			return line;
		} catch (ConnectException e) {
			if (Settings.DEBUG) {
				System.out.println("ConnectException "
						+ this.getClass().getName() + " command:  " + command);
			}
			throw new UnknownHostException();
		}
	}
}