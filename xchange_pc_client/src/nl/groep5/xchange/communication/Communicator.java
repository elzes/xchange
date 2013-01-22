package nl.groep5.xchange.communication;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.net.ConnectException;
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

import org.apache.commons.io.IOUtils;

public class Communicator {
	private static ObservableList<Peer> peers = FXCollections
			.observableArrayList();

	private static ObservableList<DownloadableFile> searchResults = FXCollections
			.observableArrayList();

	private static NameServer nameServer = new NameServer(Settings
			.getInstance().getNameServerIp(), Settings.getNameServerPort());

	private static StorageServer storageServer = new StorageServer(Settings
			.getInstance().getStorageServerIp(),
			Settings.getStorageServerPort());

	private static Router router;

	public static void resetConnections() {
		nameServer = new NameServer(Settings.getInstance().getNameServerIp(),
				Settings.getNameServerPort());

		router = new Router(Settings.getInstance().getRouterIp(),
				Settings.getRouterPort());

		storageServer = new StorageServer(Settings.getInstance()
				.getStorageServerIp(), Settings.getStorageServerPort());
	}

	public static boolean signUpToNameServer() {
		try {
			String response = nameServer.sendCommand("ADD", false);
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
			String response = nameServer.sendCommand("REMOVE", false);
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
			String result = nameServer.sendCommand("LIST", false);
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

		if (Settings.DEBUG) {
			System.out.println("Respond from peer " + line);
		}
		if (line.equals("FAIL")) {
			socket.close();
			throw new IOException();
		}
		System.out.println("Line :" + line);
		System.out.println("SPLIT CHAR " + Settings.getSplitCharRegEx());
		String[] result = line.split(Settings.getSplitCharRegEx());
		System.out.println("RESULT size: " + result.length + "Modulo:"
				+ (result.length % 2) + " content: " + Arrays.toString(result));

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
				if (foundFiles.length > 0) {
					// TODO continue;
				}

				final DownloadableFile downloadableFile = new DownloadableFile(
						fileName, result[i + 1], peer);

				searchResults.add(downloadableFile);
			}
		}
		socket.close();
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
		socket.close();

		return byteArray;
	}

	public static boolean setRouterSettings() {

		try {
			String result = router.sendCommand(
					"SET" + Settings.getSplitChar()
							+ Settings.getInstance().getNameServerIp()
							+ Settings.getSplitChar()
							+ Settings.getInstance().getStorageServerIp(),
					false);
			if (result.equals("OK"))
				return true;
		} catch (CommunicationException | IOException e) {
			e.printStackTrace();
		}
		return false;

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
			String answer = router.sendCommand(command, false);
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
			String answer = router.sendCommand("STOP", false);

			if (!answer.startsWith("OK"))
				return false;

			answer = answer
					.substring(("OK" + Settings.getSplitChar()).length());

			String[] tmpDownloadedFiles = answer.split(Settings.getSplitChar());
			int noOfDownloadedFiles = tmpDownloadedFiles.length / 4;

			System.out
					.println("no of downlaoden files: " + noOfDownloadedFiles);

			String[] downloadedFiles = new String[noOfDownloadedFiles];
			for (int i = 0; i < tmpDownloadedFiles.length; i = i + 4) {
				downloadedFiles[i % 4] = tmpDownloadedFiles[i]
						+ Settings.getSplitChar() + tmpDownloadedFiles[i + 1]
						+ Settings.getSplitChar() + tmpDownloadedFiles[i + 2]
						+ Settings.getSplitChar() + tmpDownloadedFiles[i + 3];
				System.out.println("i is " + i + "module is" + (i % 4));
				System.out.println("content: " + downloadedFiles[i % 4]);
			}

			if (noOfDownloadedFiles > 0) {
				mergeRouterDownload(downloadedFiles);
			}

			Main.state = State.LOCAL_STOP;
			MainController.processStateChange();
			return true;
		} catch (ConnectException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (CommunicationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;//when it fails we will discard the router his download//TODO change this
	}

	private static void mergeRouterDownload(String[] downloadedFiles) {

		for (String downloadedFile : downloadedFiles) {
			if (Settings.DEBUG) {
				System.out.println("downloadedFile: " + downloadedFile);
			}

			String[] args = downloadedFile.split(Settings.getSplitCharRegEx());
			if (args.length < 3) {
				if (Settings.DEBUG) {
					System.out.println("not enough arguments");
				}
				return;
			}
			System.out.println("args[3]:" + args[3]);

			char[] remoteStatus = args[3].toCharArray();

			System.out.println("remote status: "
					+ Arrays.toString(remoteStatus));
			DownloadableFile downloadableFile = new DownloadableFile(args[0],
					args[1], null);
			try {
				RandomAccessFile downloadStatusFile = new RandomAccessFile(
						downloadableFile.getDownloadStatusFile(), "r");
				byte[] localStatusTmp = new byte[(int) downloadStatusFile
						.length()];
				downloadStatusFile.readFully(localStatusTmp, 0,
						localStatusTmp.length);
				// remove new line characters
				String localStatusTmp2 = new String(localStatusTmp).replace(
						"\n", "").replace("\r", "");
				System.out.println("local status: " + localStatusTmp2);

				char[] localStatus = new String(localStatusTmp2).toCharArray();

				System.out.println("local status: "
						+ Arrays.toString(localStatus));

				ArrayList<Integer> remoteDownloadedBlocks = compareDownloadedBlocks(
						localStatus, remoteStatus);
				System.out.println("no of remote downloaded blocks: "
						+ remoteDownloadedBlocks.size());
				mergeRemoteDownloadedBlocks(downloadableFile,
						remoteDownloadedBlocks);

				downloadStatusFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
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
			File test = new File(downloadableFile.getDownloadTargetFile()
					.getPath() + ".fromFileServer.png");
			RandomAccessFile targetFile = new RandomAccessFile(test, "rw");
			targetFile.setLength(downloadableFile.getFileSize());

			if (statusFile == null || targetFile == null) {
				if (statusFile != null) {
					statusFile.close();
				}

				return;
			}

			for (Integer blockNr : remoteDownloadedBlocks) {
				if (Settings.DEBUG) {
					System.out.println("merging block " + blockNr);
				}

				int blockSize = Settings.getBlockSize();
				if (blockNr == downloadableFile.getNoOfBlocks()) {
					blockSize = downloadableFile.getRestSize();
				}
				long seekDistance = blockNr * Settings.getBlockSize();

				String response = storageServer.sendCommand(
						"GET " + downloadableFile.getRealFileName()
								+ Settings.getSplitChar() + seekDistance
								+ Settings.getSplitChar() + blockSize, true);

				System.out.println("Response from storage server " + response);
				if (response.startsWith("OK")) {
					byte[] bytes = new byte[blockSize];
					System.out.println("GOing to read " + blockSize + " bytes");
					storageServer.printWriter.println("OK");
					IOUtils.readFully(storageServer.socket.getInputStream(),
							bytes, 0, blockSize);

					targetFile.seek(seekDistance);
					targetFile.write(bytes);

					statusFile.seek(blockNr);
					statusFile.write((byte) '1');
				}
			}
			if (Settings.DEBUG) {
				System.out.println("Done with merging "
						+ downloadableFile.getRealFileName());
			}

			storageServer.sendCommand(
					"REMOVE " + downloadableFile.getRealFileName(), false);

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
			char[] localStatus, char[] remoteStatus) throws Exception {

		if (localStatus.length != remoteStatus.length) {
			throw new Exception("size not equal local" + localStatus.length
					+ " remote" + remoteStatus.length);
		}
		ArrayList<Integer> remoteDownloaded = new ArrayList<>();
		for (int i = 0; i < localStatus.length; i++) {
			System.out.println("checking " + i + " local:" + localStatus[i]
					+ "remote:" + remoteStatus[i]);
			if (localStatus[i] == '0' && remoteStatus[i] == '1') {
				remoteDownloaded.add(i);
			}
		}

		return remoteDownloaded;
	}

	public static boolean testStorageServer() {
		try {
			String result = storageServer.sendCommand("IS STORAGE SERVER",
					false);
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