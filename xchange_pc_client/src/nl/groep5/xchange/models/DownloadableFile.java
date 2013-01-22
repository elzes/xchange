package nl.groep5.xchange.models;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import javafx.scene.control.ProgressBar;
import nl.groep5.xchange.Downloader;
import nl.groep5.xchange.Settings;
import nl.groep5.xchange.controllers.DownloadController;

public class DownloadableFile {

	private String fileName;
	private String fileSize;
	private Peer peer;
	private ProgressBar progressBar;
	private Downloader downLoader;

	public DownloadableFile(String fileName, String fileSize, Peer peer) {
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.peer = peer;
	}

	public String getFileName() {
		return fileName;
	}

	public long getFileSize() {
		return Long.parseLong(fileSize);
	}

	public String getIp() {
		return peer.getIp();
	}

	private String getFileNameWithoutExtension() {
		return fileName.substring(0, fileName.lastIndexOf('.'));
	}

	public File getDownloadTargetFile() throws FileNotFoundException,
			IOException {
		File file = new File(Settings.getSharedFolder() + getFileName()
				+ Settings.getTmpExtension());

		if (!file.exists() /* && getCompleteFile() == null TODO activate */) {
			file.createNewFile();
			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
			randomAccessFile.setLength(getFileSize());
			randomAccessFile.close();
		}

		if (file.exists())
			return file;

		return null;
	}

	public File getDownloadStatusFile() throws IOException {
		String fileName = Settings.getInfoFolder() + getFileName()
				+ Settings.getInfoExtension();

		File file = new File(fileName);
		if (!file.exists() /* && getCompleteFile() == null TODO activate */) {
			file.createNewFile();
			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");

			for (int i = 1; i <= getNoOfBlocks(); i++) {
				randomAccessFile.write((byte) '0');
			}
			randomAccessFile.close();
		}

		if (file.exists())
			return file;

		return null;
	}

	public int getNoOfBlocks() {
		int noOfBlocks = (int) getFileSize() / Settings.getBlockSize();
		if (getFileSize() % Settings.getBlockSize() != 0) {
			noOfBlocks++;
		}
		return noOfBlocks;
	}

	public File getCompleteFile() {
		File file = new File(Settings.getSharedFolder() + getFileName());
		if (file.exists())
			return file;

		return null;
	}

	public String getRealFileName() {
		if (getFileName().endsWith(Settings.getTmpExtension())) {
			return getFileNameWithoutExtension();
		} else {
			return getFileName();
		}
	}

	public Peer getPeer() {
		return peer;
	}

	public int getRestSize() {
		return (int) (getFileSize() % Settings.getBlockSize());
	}

	public ProgressBar getProgressBar() {
		if (progressBar == null) {
			progressBar = new ProgressBar();
		}

		progressBar.setProgress(getProgress());
		progressBar.setPrefWidth(DownloadController.PROGRESSBAR_WIDTH);
		return progressBar;
	}

	public double getProgress() {
		File statusFile;
		try {
			statusFile = getDownloadStatusFile();
			if (statusFile == null)
				return 1;

			RandomAccessFile progressFile = new RandomAccessFile(statusFile,
					"r");
			byte[] byteArray = new byte[(int) progressFile.length()];
			progressFile.seek(0);
			progressFile.readFully(byteArray);
			progressFile.close();

			String content = new String(byteArray);

			double curBlock = content.indexOf('0');// double for division later

			if (curBlock == -1)
				return 1;

			return curBlock / getNoOfBlocks();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return 0;
	}

	public void updateProgressBar() {
		getProgressBar();
	}

	public void setDownloader(Downloader downLoader) {
		this.downLoader = downLoader;
	}

	public Downloader getDownLoader() {
		return downLoader;
	}

	public void setPeer(Peer peer) {
		this.peer = peer;
	}

	public String getRouterCommand() {
		ArrayList<String> commandArgs = new ArrayList<String>();
		commandArgs.add(getRealFileName());
		commandArgs.add("" + getFileSize());
		commandArgs.add("" + getNoOfBlocks());
		File statusFile;
		try {
			statusFile = getDownloadStatusFile();

			if (statusFile == null)
				return "";

			RandomAccessFile randomAccesStatus = new RandomAccessFile(
					statusFile, "r");
			byte[] content = new byte[(int) randomAccesStatus.length()];
			randomAccesStatus.readFully(content, 0, content.length);
			randomAccesStatus.close();
			String stringContent = new String(content);
			commandArgs.add(stringContent);

			String command = "";// Settings.getListStartSign();
			for (String s : commandArgs) {
				command += s + Settings.getSplitChar();
			}
			command = command.substring(0, command.length() - 1);// strip last
																	// split
																	// char
			command += "";// Settings.getListStopSign();

			return command;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}

	public void completeDownload() throws FileNotFoundException, IOException {
		if (Settings.DEBUG) {
			System.out.println("complete download of " + getRealFileName());
		}
		getDownloadStatusFile().delete();

		File newFileName = new File(Settings.getSharedFolder()
				+ getRealFileName());

		// delete if new file already exists
		if (newFileName.exists()) {
			if (Settings.DEBUG) {
				System.out
						.println("Going to delete downloaded file because target already exsists");
			}
			getDownloadTargetFile().delete();
		}

		getDownloadTargetFile().renameTo(newFileName);

		DownloadController.removeDownload(this);

	}
}