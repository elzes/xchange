package nl.groep5.xchange.models;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import nl.groep5.xchange.Settings;

public class DownloadableFile {

	private final String fileName;
	private final String fileSize;
	private final Peer peer;

	public DownloadableFile(String fileName, String fileSize, Peer peer) {
		this.fileName = fileName;
		this.fileSize = fileSize;
		this.peer = peer;
	}

	public String getFileName() {
		return fileName;
	}

	public float getFileSize() {
		return Float.parseFloat(fileSize);
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

		checkFileExistanceAndCreate(file);

		if (file.exists())
			return file;

		return null;
	}

	public File getDownloadStatusFile() throws IOException {
		String fileName = Settings.getInfoFolder()
				+ getFileName().replace(Settings.getTmpExtension(),
						Settings.getInfoExtension());
		System.out.println("getDownloadStatusFile " + fileName);
		File file = new File(fileName);
		checkFileExistanceAndCreate(file);

		if (file.exists())
			return file;

		return null;
	}

	private void checkFileExistanceAndCreate(File file) throws IOException,
			FileNotFoundException {

		if (!file.exists() && getCompleteFile() == null) {
			file.createNewFile();
			RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
			for (int i = 0; i <= getFileSize(); i++) {
				randomAccessFile.write((byte) '0');
			}
		}
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
}