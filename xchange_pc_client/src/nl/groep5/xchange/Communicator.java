package nl.groep5.xchange;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import nl.groep5.xchange.models.DownloadableFile;
import nl.groep5.xchange.models.Peer;

public class Communicator {
	private static ObservableList<Peer> peers = FXCollections
			.observableArrayList();

	private static ObservableList<DownloadableFile> searchResults = FXCollections
			.observableArrayList();

	private static NameServer nameServer = new NameServer(
			Settings.getNameServerIp(), Settings.getNameServerPort());

	public static void signUpToNameServer() throws ConnectException,
			IOException {
		nameServer.sendCommand("ADD");
	}

	public static void updatePeers() {
		try {
			String result = nameServer.sendCommand("LIST");
			peers.clear();
			for (String s : result.split(" ")) {
				peers.add(new Peer(s));
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static ObservableList<DownloadableFile> search(String pattern) {
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

		String[] result = line.split(" ");
		searchResults.clear();

		if (result.length % 2 == 0) {
			for (int i = 0; i < result.length; i += 2) {

				final DownloadableFile downloadableFile = new DownloadableFile(
						result[i], result[i + 1], peer);

				searchResults.add(downloadableFile);
			}
		}
		System.out.println("RESULT: " + line);
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

	public static DownloadableFile getDownloadableFileFromName(String name) {
		ObservableList<DownloadableFile> results = search(name);

		for (DownloadableFile downloadableFile : results) {
			if (downloadableFile.getFileName().equals(name))
				return downloadableFile;
		}

		return null;
	}

	public static void startDownload(DownloadableFile downloadableFile) {
		Downloader downLoader = new Downloader(downloadableFile);
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

		printWriter.println("GET " + downloadableFile.getRealFileName() + " "
				+ curBlock);

		byte[] byteArray = new byte[Settings.getBlockSize()];

		int n = -1;
		while (n < 1) {
			n = bufferedInputStream.read(byteArray, 0, size);
		}

		return byteArray;
	}
}