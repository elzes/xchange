package nl.groep5.xchange.storageServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;

import nl.groep5.xchange.Settings;

/**
 * This class implements a separate application (server process) handles the
 * GET, POST and REMOVE commands blocks are stored in ./files directory
 */

public class StorageServer {

	static final String STORAGE_DIR = "./files/";
	private final int port = Settings.getStorageServerPort();
	final static int BLOCKSIZE = 64 * 1024;

	// un-buffered socket stream (flushing has no purpose)
	InputStream dis;
	OutputStream dos;

	// buffered socket stream
	BufferedReader in;
	PrintWriter out;

	public StorageServer() {
		if (Settings.DEBUG) {
			System.out.println("Storageserver started ...");
		}
		// check if ./files directory exists
		File file = new File(STORAGE_DIR);
		if (!file.exists()) {
			file.mkdirs();
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
		@SuppressWarnings("resource")
		ServerSocket ss = new ServerSocket(port);

		// listen for incoming connections
		while (true) {
			Handler handler = new Handler(ss.accept());
			handler.start();
		}
	}

	public static void main(String[] args) throws Exception {
		new StorageServer();
	}
}