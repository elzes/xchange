package nl.groep5.xchange;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;

import javax.naming.CommunicationException;

import nl.groep5.xchange.communication.StorageServer;
import nl.groep5.xchange.controllers.MainController;
import nl.groep5.xchange.models.DownloadableFile;

import org.apache.commons.io.IOUtils;

public class TestRouterResponse {

	static {
		try {
			Settings.getInstance().load();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static StorageServer storageServer = new StorageServer(Settings
			.getInstance().getStorageServerIp(),
			Settings.getStorageServerPort());

	public static void main(String[] args) {
		stopRouterDownload();
	}

	public static boolean stopRouterDownload() {
		String answer = "OK test.png 666232 11 11111111111";
		if (!answer.startsWith("OK"))
			return false;

		answer = answer.substring(("OK" + Settings.getSplitChar()).length());

		String[] tmpDownloadedFiles = answer.split(Settings.getSplitChar());
		int noOfDownloadedFiles = tmpDownloadedFiles.length / 4;

		System.out.println("no of downlaoden files: " + noOfDownloadedFiles);

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
				long seekDistance = blockNr * Settings.getBlockSize();
				if (blockNr == downloadableFile.getNoOfBlocks()) {
					seekDistance = downloadableFile.getRestSize();
				}

				int blockSize = Settings.getBlockSize();
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
}