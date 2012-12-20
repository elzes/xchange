package nl.groep5.xchange.models;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import javafx.scene.control.ProgressBar;
import nl.groep5.xchange.Settings;

public class DownloadableFile {

	private final String fileName;
	private final String fileSize;
	private final Peer peer;
	private ProgressBar progressBar;

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
		File file = new File(Settings.getSharedFolder()
				+ getFileNameWithoutExtension() + Settings.getTmpExtension());

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
}