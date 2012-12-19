package old.xchange;

/**
 * Class represents the i/f with name server
 * in other words : handles interaction with name server
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import old.xchange.gui.GUI;

public class XNameServer {
	GUI g;

	public String ip = "";
	public int port = 9001;

	BufferedReader in;
	PrintWriter out;

	public XNameServer(GUI g) {
		this.g = g;
	}

	/**
	 * register specified ip address at the name server (ADD) name server must
	 * be running at this point as a separate application
	 */

	public void register(String ip) throws Exception {
		String line = null;
		try {
			// make connection to name server socket
			Socket ns = new Socket(g.nameServer.ip, port);

			// get socket input stream and open a BufferedReader on it
			in = new BufferedReader(new InputStreamReader(ns.getInputStream()));
			// get socket output stream and open a PrintWriter on it
			out = new PrintWriter(ns.getOutputStream(), true);

			out.println("ADD " + ip);

			// wait for response
			do {
				line = in.readLine();
			} while (line == null);

			// close socket
			ns.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (Debug.DEBUG) {
			System.out.println("Received after ADD request :" + line);
		}
		if (line.equals("FAIL")) {
			System.err
					.println("ERROR : Nameserver returned FAIL on ADD request");
		}
	}

	/**
	 * unregister specified ip address at the name server (REMOVE)
	 */

	public void unregister(String ip) throws Exception {
		String line = null;
		try {
			// make connection to name server socket
			Socket ns = new Socket(g.nameServer.ip, port);

			// get socket input stream and open a BufferedReader on it
			in = new BufferedReader(new InputStreamReader(ns.getInputStream()));
			// get socket output stream and open a PrintWriter on it
			out = new PrintWriter(ns.getOutputStream(), true);

			out.println("REMOVE " + ip);

			// wait for response
			do {
				line = in.readLine();
			} while (line == null);

			// close socket
			ns.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (Debug.DEBUG) {
			System.out.println("Received after REMOVE request :" + line);
		}
		if (line.equals("FAIL")) {
			System.err
					.println("ERROR : Nameserver returned FAIL on REMOVE request");
		}
	}

	/**
	 * list all peers registered at name server (LIST)
	 */

	public String[] getPeers() {
		String line = null;
		try {
			// make connection to name server socket
			Socket ns = new Socket(g.nameServer.ip, port);

			// get socket input stream and open a BufferedReader on it
			in = new BufferedReader(new InputStreamReader(ns.getInputStream()));
			// get socket output stream and open a PrintWriter on it
			out = new PrintWriter(ns.getOutputStream(), true);

			out.println("LIST");

			// wait for response
			do {
				line = in.readLine();
			} while (line == null);

			// close socket
			ns.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (Debug.DEBUG) {
			System.out.println("Received after LIST request : " + line);
		}

		String[] s = { "" }; // avoid null-pointer exception
		if (line.startsWith("FAIL")) {
			System.err
					.println("ERROR : Nameserver returned FAIL on LIST request");
		} else {
			s = line.split(" ");
		}
		return s;
	}
}
