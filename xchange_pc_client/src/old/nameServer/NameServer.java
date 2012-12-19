package old.nameServer;

/**
 * This class implements a separate application (server process)
 * handles the LIST, ADD and REMOVE commands
 * list of peers is stored in memory only
 */

import java.io.PrintWriter;
import java.net.ServerSocket;
import java.util.ArrayList;

public class NameServer {

	private static final boolean DEBUG = true;
	private String ip = "";
	public static final int port = 9001;
	private ArrayList<String> list;

	public NameServer() {
		list = new ArrayList<String>();
		if (DEBUG) {
			System.out.println("Nameserver started ...");
		}
		try {
			listenAndHandle();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * create and listen on server socket
	 * 
	 */

	public void listenAndHandle() throws Exception {
		ServerSocket ss = new ServerSocket(port);

		// listen for incoming connections
		while (true) {
			NameServerClientHandler nameServerClientHandler = new NameServerClientHandler(
					ss.accept(), this);
			nameServerClientHandler.run();
		}
	}

	public void handleAdd(String line, PrintWriter out) throws Exception {
		if (DEBUG) {
			System.out.println("Nameserver received : " + line);
		}
		String[] s = line.split(" ");
		if (s.length != 2) {
			handleError(out);
			return;
		}
		String ip = s[1];
		synchronized (list) {
			if (!list.contains(ip)) {
				list.add(ip);
			} else {
				if (DEBUG) {
					System.out.println("Nameserver : duplicate ip : " + ip);
				}
			}
		}

		out.println("OK");
		if (DEBUG) {
			System.out.println("Nameserver : added " + ip);
		}
	}

	public void handleRemove(String line, PrintWriter out) throws Exception {
		String[] s = line.split(" ");
		if (s.length != 2) {
			handleError(out);
			return;
		}
		String ip = s[1];
		synchronized (list) {
			list.remove(ip);
		}

		out.println("OK");
		if (DEBUG) {
			System.out.println("Nameserver : removed " + ip);
		}
	}

	public void handleList(PrintWriter out) throws Exception {
		if (DEBUG) {
			System.out.println("Nameserver received LIST request");
		}
		System.out.println("Handle list");
		synchronized (list) {
			for (String ip : list) {
				out.print(ip + " ");
			}
		}

		// send EOL
		out.println("");
	}

	public void handleError(PrintWriter out) throws Exception {
		out.println("FAIL");
	}

	public static void main(String[] args) throws Exception {
		new NameServer();
	}
}