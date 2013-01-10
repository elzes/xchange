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

public class Router {

	protected static final int SOCKET_TIMEOUT = 5;
	private static Router instance;
	private static boolean sended;
	private Socket socket;
	private BufferedReader bufferedReader;
	private PrintWriter printWriter;

	private Router() {
		resetSettings();
	}

	public static Router getInstance() {
		if (instance == null) {
			instance = new Router();
		}

		return instance;
	}

	public void resetSettings() {
		System.out.println("reset router settings.");

		try {
			if (bufferedReader != null)
				bufferedReader.close();
			if (printWriter != null)
				printWriter.close();
			if (socket != null)
				socket.close();

			socket = new Socket();

			socket.connect(new InetSocketAddress(Settings.getInstance()
					.getRouterIp(), Settings.getRouterPort()),
					SOCKET_TIMEOUT * 1000);

			bufferedReader = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));

			printWriter = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String sendCommand(String command) throws UnknownHostException,
			IOException, ConnectException, CommunicationException {

		if (socket == null || printWriter == null || bufferedReader == null) {
			throw new CommunicationException();
		}
		return "FALSE";
		/*
		 * try { if (Settings.DEBUG) { System.out.println("Going to write to " +
		 * this.getClass().getName() + " command: " + command); } if
		 * (Router.sended) return "FAIL";
		 * 
		 * Router.sended = false; // printWriter.println(command);
		 * printWriter.write("SET|192.168.1.105|192.168.1.10");
		 * printWriter.flush(); System.out.println("Command sended");
		 * 
		 * String line = null; do { line = bufferedReader.readLine(); } while
		 * (line == null);
		 * 
		 * if (Settings.DEBUG) { System.out.println("Response from " +
		 * this.getClass().getName() + " " + line); }
		 * 
		 * if (line.startsWith("FAIL")) throw new CommunicationException();
		 * 
		 * if (Settings.DEBUG) { System.out.println("Done with command " +
		 * command + " to device " + this.getClass().getName()); }
		 * 
		 * return line; } catch (ConnectException e) { if (Settings.DEBUG) {
		 * System.out.println("ConnectException " + this.getClass().getName() +
		 * " command:  " + command); } throw new UnknownHostException(); }
		 */
	}
}