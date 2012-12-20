package nl.groep5.xchange;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import nl.groep5.xchange.models.DownloadableFile;

public class Downloader extends Thread {

	private DownloadableFile downloadableFile;
	private RandomAccessFile progressFile;
	private int curBlock;
	private RandomAccessFile targetFile;

	public Downloader(DownloadableFile downloadableFile) {
		this.downloadableFile = downloadableFile;
	}

	@Override
	public void run() {

		try {
			progressFile = new RandomAccessFile(
					downloadableFile.getDownloadStatusFile(), "rw");
			targetFile = new RandomAccessFile(
					downloadableFile.getDownloadTargetFile(), "rw");

			if (downloadIsComplete()) {
				completeDownload();
				return;
			}

			while (!downloadIsComplete()) {
				byte[] result = Communicator.GetBlockFromPeer(downloadableFile,
						curBlock, Settings.getBlockSize());
				targetFile.seek(Settings.getBlockSize() * curBlock);
				targetFile.write(result);

				progressFile.seek(curBlock);
				progressFile.write((byte) '1');
			}

			completeDownload();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean downloadIsComplete() throws IOException {
		byte[] byteArray = new byte[(int) progressFile.length()];
		progressFile.readFully(byteArray);
		String content = new String(byteArray);
		curBlock = content.indexOf('0');
		return curBlock != -1;
	}

	private void completeDownload() throws IOException {
		downloadableFile.getDownloadStatusFile().delete();
		File newFileName = new File(Settings.getSharedFolder()
				+ downloadableFile.getFileName().replace(
						Settings.getTmpExtension(), ""));
		downloadableFile.getDownloadTargetFile().renameTo(newFileName);
	}
}