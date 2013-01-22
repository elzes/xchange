package nl.groep5.xchange.storageServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.GregorianCalendar;

import nl.groep5.xchange.Settings;

import org.apache.commons.io.IOUtils;

public class Handler extends Thread {

	private Socket client;
	private InputStream inputStream;
	private OutputStream outputStream;
	private BufferedReader bufferedReader;
	private PrintWriter printWriter;

	public Handler(Socket client) {
		this.client = client;
	}

	@Override
	public void run() {
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
		// get socket i/o stream
		inputStream = s.getInputStream();
		outputStream = s.getOutputStream();

		// open a BufferedReader on it
		bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		// open a PrintWriter on it
		printWriter = new PrintWriter(outputStream, true);

		// get the message
		String line = null;
		do {
			line = bufferedReader.readLine();
		} while (line == null);
		System.out.println("Received commando " + line + " FROM "
				+ s.getRemoteSocketAddress()
				+ new GregorianCalendar().getTimeInMillis());

		if (line.equals("IS STORAGE SERVER")) {
			System.out.println("Command is IS STORAGE SERVER");
			printWriter.println("OK");
		} else if (line.startsWith("POST")) {
			System.out.println("Command is IS POST");
			handlePost(line);
		} else if (line.startsWith("GET")) {
			System.out.println("Command is IS GET");
			handleGet(line);
		} else if (line.startsWith("REMOVE")) {
			System.out.println("Command is IS REMOVE");
			handleRemove(line);
		} else {
			System.out.println("Command is UNKOWN");
			handleError();
		}
		s.close();
	}

	private void handlePost(String line) {
		if (Settings.DEBUG) {
			System.out.println("Storageserver received : " + line);
		}

		try {
			StorageServerPostCommand command = new StorageServerPostCommand(
					line, inputStream, printWriter);

			File file = FileHelper.loadFile(StorageServer.STORAGE_DIR
					+ command.getFileName());

			if (!file.exists()) {
				FileHelper.createRandomAccesFileFromFile(file,
						command.getFileSize());
			}

			FileHelper
					.writeByteArrayToFile(file, command.getSeekDistance(),
							command.getBytesToWrite(),
							command.getBytesToWrite().length);

			printWriter.println("OK");
		} catch (IOException e) {
			handleError();
			e.printStackTrace();
		} catch (InvalidCommandException e) {
			handleError();
			e.printStackTrace();
		}
		if (Settings.DEBUG) {
			System.out.println("Storageserver : added block to file");
		}
	}

	private void handleGet(String line) {
		if (Settings.DEBUG) {
			System.out.println("Storageserver received : " + line);
		}

		byte[] byteArray;
		try {
			StorageServerGetCommand storageServerGetCommand = new StorageServerGetCommand(
					line);
			byteArray = FileHelper.getBlockFromFile(
					new File(StorageServer.STORAGE_DIR
							+ storageServerGetCommand.getFileName()),
					storageServerGetCommand.getSeekDistance(),
					storageServerGetCommand.getCurBlockSize());

			printWriter.println("OK");
			bufferedReader.readLine();// block until client is ready
			System.out.println("Write size:" + byteArray.length);
			IOUtils.write(byteArray, outputStream);
		} catch (IOException e) {
			handleError();
			e.printStackTrace();
		} catch (InvalidCommandException e) {
			handleError();
			e.printStackTrace();
		}

		if (Settings.DEBUG) {
			System.out.println("Storageserver : sent block from file");
		}
	}

	private void handleRemove(String line) {
		if (Settings.DEBUG) {
			System.out.println("Storageserver received : " + line);
		}

		try {
			StorageServerRemoveCommand storageServerRemoveCommand = new StorageServerRemoveCommand(
					line);
			if (FileHelper.removeFile(StorageServer.STORAGE_DIR
					+ storageServerRemoveCommand.getFileName())) {
				printWriter.println("OK");
			} else {
				handleError();
			}
		} catch (InvalidCommandException e) {
			handleError();
			e.printStackTrace();
		}

		if (Settings.DEBUG) {
			System.out.println("Storageserver : all files removed !");
		}
	}

	private void handleError() {
		printWriter.println("FAIL");
	}
}
