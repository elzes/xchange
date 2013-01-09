package nl.groep5.xchange.communication;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.naming.CommunicationException;

import nl.groep5.xchange.Downloader;
import nl.groep5.xchange.Main;
import nl.groep5.xchange.Settings;
import nl.groep5.xchange.State;
import nl.groep5.xchange.controllers.DownloadController;
import nl.groep5.xchange.controllers.MainController;
import nl.groep5.xchange.models.DownloadableFile;
import nl.groep5.xchange.models.Peer;

public class Communicator {
	private static ObservableList<Peer> peers = FXCollections
			.observableArrayList();

	private static ObservableList<DownloadableFile> searchResults = FXCollections
			.observableArrayList();

	private static NameServer nameServer = new NameServer(Settings
			.getInstance().getNameServerIp(), Settings.getNameServerPort());

	private static Router router = Router.getInstance();

	private static StorageServer storageServer = new StorageServer(Settings
			.getInstance().getStorageServerIp(),
			Settings.getStorageServerPort());

	public static void resetConnections() {
		nameServer = new NameServer(Settings.getInstance().getNameServerIp(),
				Settings.getNameServerPort());

		Router.getInstance().resetSettings();

		storageServer = new StorageServer(Settings.getInstance()
				.getStorageServerIp(), Settings.getStorageServerPort());
	}

	public static boolean signUpToNameServer() {
		try {
			String response = nameServer.sendCommand("ADD");
			return response.equals("OK");
		} catch (IOException | CommunicationException e) {
			if (Settings.DEBUG) {
				System.out.println("could not connect to nameserver.");
			}
		}
		return false;
	}

	public static boolean unregisterFromNameServer() {
		try {
			String response = nameServer.sendCommand("REMOVE");
			return response.equals("OK");
		} catch (IOException | CommunicationException e) {
			if (Settings.DEBUG) {
				System.out.println("could not unregister from nameserver.");
			}
		}
		return false;
	}

	public static boolean updatePeers() {
		try {
			String result = nameServer.sendCommand("LIST");
			peers.clear();
			for (String s : result.split(Settings.getSplitCharRegEx())) {
				peers.add(new Peer(s));
			}

		} catch (IOException e) {
			if (Settings.DEBUG) {
				System.out.println("Failed to update peerlist.");
			}
			return false;
		} catch (CommunicationException e) {
			if (Settings.DEBUG) {
				System.out.println("Failed to update peerlist. Server error."
						+ e.getMessage());
			}
			return false;
		}
		return true;
	}

	public static ObservableList<DownloadableFile> search(String pattern) {
		searchResults.clear();
		for (Peer peer : peers) {
			try {
				searchOnPeer(peer, pattern);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return searchResults;
	}

	public static void searchOnPeer(Peer peer, String pattern)
			throws UnknownHostException, IOException {
		Socket socket = new Socket(peer.getIp(), peer.getPort());

		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(socket.getInputStream()));
		PrintWriter printWriter = new PrintWriter(socket.getOutputStream(),
				true);
		printWriter.println("SEARCH " + pattern);

		String line = null;
		do {
			line = bufferedReader.readLine();
		} while (line == null);

		if (line.startsWith("FAIL"))
			throw new IOException();

		System.out.println("Line :" + line);
		System.out.println("SPLIT CHAR " + Settings.getSplitCharRegEx());
		String[] result = line.split(Settings.getSplitCharRegEx());
		System.out.println("RESULT: " + Arrays.toString(result));

		if (result.length % 2 == 0) {
			for (int i = 0; i < result.length; i += 2) {
				final String fileName = result[i];
				File[] foundFiles = new File(Settings.getSharedFolder())
						.listFiles(new FileFilter() {

							@Override
							public boolean accept(File file) {
								return file.getName().toLowerCase()
										.equals(fileName.toLowerCase());
							}
						});
				// skip file if local exists
				if (foundFiles.length > 0)
					continue;

				final DownloadableFile downloadableFile = new DownloadableFile(
						fileName, result[i + 1], peer);

				searchResults.add(downloadableFile);
			}
		}
		if (Settings.DEBUG) {
			System.out.println("RESULT: " + line);
		}
	}

	public static ObservableList<Peer> getPeers() {
		return peers;
	}

	public static ObservableList<DownloadableFile> getSearchResults() {
		return searchResults;
	}

	public static void clearSearchResults() {
		searchResults.clear();
	}

	public static void startDownload(DownloadableFile downloadableFile) {
		Downloader downLoader = new Downloader(downloadableFile);
		downloadableFile.setDownloader(downLoader);
		downLoader.start();
	}

	public static byte[] GetBlockFromPeer(DownloadableFile downloadableFile,
			int curBlock, int size) throws UnknownHostException, IOException {

		Socket socket = new Socket(downloadableFile.getPeer().getIp(),
				downloadableFile.getPeer().getPort());

		BufferedInputStream bufferedInputStream = new BufferedInputStream(
				socket.getInputStream(), Settings.getBlockSize());
		PrintWriter printWriter = new PrintWriter(socket.getOutputStream(),
				true);

		printWriter.println("GET " + downloadableFile.getRealFileName()
				+ Settings.getSplitChar() + curBlock);

		byte[] byteArray = new byte[size];

		int n = -1;
		while (n < 1) {
			n = bufferedInputStream.read(byteArray, 0, size);
		}

		String response = new String(byteArray);
		if (response.startsWith("FAIL")) {
			byteArray = null;
		}
		if (Settings.DEBUG) {
			System.out.println(response);
		}

		return byteArray;
	}

	public static boolean setRouterSettings() {
		return true;/*
					 * TODO try { String result = router.sendCommand("SET" +
					 * Settings.getSplitChar() +
					 * Settings.getInstance().getNameServerIp() +
					 * Settings.getSplitChar() +
					 * Settings.getInstance().getStorageServerIp()); if
					 * (result.equals("OK")) return true; } catch
					 * (CommunicationException | IOException e) {
					 * e.printStackTrace(); } // TODO change to false return
					 * true;
					 */
	}

	public static boolean startRouterDownload() {
		String command = "START" + Settings.getSplitChar();
		for (DownloadableFile downloadableFile : DownloadController.pendingDownloads) {
			command += downloadableFile.getRouterCommand();
			command += Settings.getSplitChar();
		}
		command = command.substring(0, command.length() - 1);

		try {
			if (Settings.DEBUG) {
				System.out.println(command);
			}
			String answer = router.sendCommand(command);
			if (answer.startsWith("OK"))
				return true;

		} catch (CommunicationException | IOException e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	public static boolean stopRouterDownload() {
		try {
			String answer = router.sendCommand("STOP");
			if (!answer.startsWith("OK"))
				return false;

			answer = answer.substring("OK ".length());
			String[] downloadedFiles = answer.split(Settings.getListStopSign()
					+ Settings.getListStartSign());
			System.out.println("router answer " + answer);
			mergeRouterDownload(downloadedFiles);
			Main.state = State.LOCAL_STOP;
			MainController.processStateChange();
		} catch (CommunicationException | IOException e) {
			e.printStackTrace();
			Main.state = State.LOCAL_STOP;
			MainController.processStateChange();
			return false;
		}
		return true;
	}

	private static void mergeRouterDownload(String[] downloadedFiles) {

		for (String downloadedFile : downloadedFiles) {
			downloadedFile = downloadedFile.replace(
					Settings.getListStartSign(), "");
			downloadedFile = downloadedFile.replace(Settings.getListStopSign(),
					"");

			if (Settings.DEBUG) {
				System.out.println("downloadedFile: " + downloadedFile);
			}

			String[] args = downloadedFile.split(Settings.getSplitCharRegEx());
			if (args.length < 3)
				return;
			String[] remoteStatus = args[2].split("|");// split on all signs
			DownloadableFile downloadableFile = new DownloadableFile(args[0],
					args[1], null);
			try {
				RandomAccessFile downloadStatusFile = new RandomAccessFile(
						downloadableFile.getDownloadStatusFile(), "r");
				byte[] localStatusTmp = new byte[(int) downloadStatusFile
						.length()];
				downloadStatusFile.readFully(localStatusTmp, 0,
						localStatusTmp.length);
				String[] localStatus = new String(localStatusTmp).split("|");
				ArrayList<Integer> remoteDownloadedBlocks = compareDownloadedBlocks(
						localStatus, remoteStatus);
				mergeRemoteDownloadedBlocks(downloadableFile,
						remoteDownloadedBlocks);

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				Main.state = State.LOCAL_STOP;
				MainController.processStateChange();
			}
		}
	}

	private static void mergeRemoteDownloadedBlocks(
			DownloadableFile downloadableFile,
			ArrayList<Integer> remoteDownloadedBlocks) throws IOException {

		try {
			RandomAccessFile statusFile = new RandomAccessFile(
					downloadableFile.getDownloadStatusFile(), "rw");
			RandomAccessFile targetFile = new RandomAccessFile(
					downloadableFile.getDownloadTargetFile(), "rw");
			if (statusFile == null || targetFile == null)
				return;

			for (Integer blockNr : remoteDownloadedBlocks) {
				String response = storageServer.sendCommand("GET "
						+ downloadableFile.getRealFileName()
						+ Settings.getSplitChar() + blockNr);
				if (response.startsWith("OK ")) {
					response = response.substring("OK ".length());
					byte[] bytes = response.getBytes();
					targetFile.seek(blockNr * Settings.getBlockSize());
					targetFile.write(bytes);

					statusFile.seek(blockNr);
					statusFile.write((byte) '1');
				}
			}
			if (Settings.DEBUG) {
				System.out.println("Done with merging "
						+ downloadableFile.getRealFileName());
			}
			storageServer.sendCommand("REMOVE "
					+ downloadableFile.getRealFileName());

			statusFile.close();
			targetFile.close();
		} catch (IOException e) {
			e.printStackTrace();
			throw new IOException();
		} catch (CommunicationException e) {
			e.printStackTrace();
			throw new IOException();
		}
	}

	private static ArrayList<Integer> compareDownloadedBlocks(
			String[] localStatus, String[] remoteStatus) {

		ArrayList<Integer> remoteDownloaded = new ArrayList<>();
		for (int i = 0; i < localStatus.length; i++) {
			if (localStatus[i] == "0" && remoteStatus[i] == "1") {
				remoteDownloaded.add(i);
			}
		}

		return remoteDownloaded;
	}

	public static boolean testStorageServer() {
		try {
			String result = storageServer.sendCommand("IS STORAGE SERVER");
			if (result.equals("OK"))
				return true;
		} catch (CommunicationException | IOException e) {
			e.printStackTrace();
		}

		return false;
	}

	public static void searchPeerForBlock(DownloadableFile downloadableFile,
			int curBlock, int size) {
		for (Peer peer : peers) {
			try {
				downloadableFile.setPeer(peer);
				GetBlockFromPeer(downloadableFile, curBlock, size);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}