package nl.groep5.xchange.nameServer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class NameServerClientHandler extends Thread {

	private Socket client;
	private BufferedReader in;
	private PrintWriter out;
	private NameServer nameServer;

	public NameServerClientHandler(Socket client, NameServer nameServer) {
		this.client = client;
		this.nameServer = nameServer;
		System.out.println("name server reached");
	}

	@Override
	public void run() {
		System.out.println("NameServerClientHandler run");
		try {
			handle(client);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * handle incoming messages all messages are strings ending on "\n"
	 */
	private void handle(Socket s) throws Exception {

		// get socket input stream and open a BufferedReader on it
		in = new BufferedReader(new InputStreamReader(s.getInputStream()));

		// get socket output stream and open a PrintWriter on it
		out = new PrintWriter(s.getOutputStream(), true);

		// get the message
		String line = null;
		do {
			line = in.readLine();
		} while (line == null);

		String externalIp = s.getInetAddress().getHostAddress();

		if (line.startsWith("ADD")) {
			nameServer.handleAdd(line + " " + externalIp, out);
		}

		else if (line.startsWith("REMOVE")) {
			nameServer.handleRemove(line, out);
		}

		else if (line.startsWith("LIST")) {
			nameServer.handleList(out, externalIp);
		}

		else {
			nameServer.handleError(out);
		}
	}
}
