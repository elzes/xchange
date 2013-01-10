package nl.groep5.xchange.storageServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import nl.groep5.xchange.Settings;
import old.helpers.FileHelper;
import old.xchange.Debug;

/**
 * This class implements a separate application (server process) handles the
 * GET, POST and REMOVE commands blocks are stored in ./files directory
 */

public class StorageServer {

	private static final String STORAGE_DIR = "./files/";
	private final int port = Settings.getStorageServerPort();
	private final int BLOCKSIZE = 64 * 1024;

	// un-buffered socket stream (flushing has no purpose)
	InputStream dis;
	OutputStream dos;

	// buffered socket stream
	BufferedReader in;
	PrintWriter out;

	public StorageServer() {
		if (Debug.DEBUG) {
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
		ServerSocket ss = new ServerSocket(port);

		// listen for incoming connections
		while (true) {
			Socket cs = ss.accept();
			// handle a single request
			handle(cs);
			// close client socket and resume listening
			cs.close();
		}
	}

	/**
	 * handle incoming messages all messages are strings ending on "\n"
	 */
	private void handle(Socket s) throws Exception {
		// get socket i/o stream
		dis = s.getInputStream();
		dos = s.getOutputStream();

		// open a BufferedReader on it
		in = new BufferedReader(new InputStreamReader(dis));
		// open a PrintWriter on it
		out = new PrintWriter(dos, true);

		// get the message
		String line = null;
		do {
			line = in.readLine();
		} while (line == null);

		if (line.equals("IS STORAGE SERVER")) {
			out.println("OK");
		}
		if (line.startsWith("POST")) {
			handlePost(line);
		}

		else if (line.startsWith("GET")) {
			handleGet(line);
		}

		else if (line.startsWith("REMOVE")) {
			handleRemove(line);
		}

		else {
			handleError();
		}
		s.close();
	}

	private void handlePost(String line) {
		if (Debug.DEBUG) {
			System.out.println("Storageserver received : " + line);
		}

		try {
			StorageServerPostCommand command = new StorageServerPostCommand(
					line);

			File file = FileHelper
					.loadFile(STORAGE_DIR + command.getFileName());

			if (!file.exists()) {
				FileHelper.createRandomAccesFileFromFile(file,
						command.getFileSize());
			}

			FileHelper
					.writeByteArrayToFile(file,
							BLOCKSIZE * command.getBlockNr(),
							command.getBytesToWrite(),
							command.getBytesToWrite().length);

			out.println("OK");
		} catch (IOException e) {
			handleError();
			e.printStackTrace();
		} catch (InvalidCommandException e) {
			handleError();
			e.printStackTrace();
		}
		if (Debug.DEBUG) {
			System.out.println("Storageserver : added block to file");
		}
	}

	private void handleGet(String line) {
		if (Debug.DEBUG) {
			System.out.println("Storageserver received : " + line);
		}

		byte[] byteArray;
		try {
			StorageServerGetCommand storageServerGetCommand = new StorageServerGetCommand(
					line);
			byteArray = FileHelper.getBlockFromFile(new File(
					storageServerGetCommand.getFileName()),
					storageServerGetCommand.getBlockNr(), BLOCKSIZE);
			out.println("OK" + new String(byteArray));
		} catch (IOException e) {
			handleError();
			e.printStackTrace();
		} catch (InvalidCommandException e) {
			handleError();
			e.printStackTrace();
		}

		if (Debug.DEBUG) {
			System.out.println("Storageserver : sent block from file");
		}
	}

	private void handleRemove(String line) {
		if (Debug.DEBUG) {
			System.out.println("Storageserver received : " + line);
		}

		try {
			StorageServerRemoveCommand storageServerRemoveCommand = new StorageServerRemoveCommand(
					line);
			if (FileHelper.removeFile(STORAGE_DIR
					+ storageServerRemoveCommand.getFileName())) {
				out.println("OK");
			} else {
				handleError();
			}
		} catch (InvalidCommandException e) {
			handleError();
			e.printStackTrace();
		}

		if (Debug.DEBUG) {
			System.out.println("Storageserver : all files removed !");
		}
	}

	private void handleError() {
		out.println("FAIL");
	}

	public static void main(String[] args) throws Exception {
		new StorageServer();
	}
}
