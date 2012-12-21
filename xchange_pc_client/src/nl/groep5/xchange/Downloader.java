package nl.groep5.xchange;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import nl.groep5.xchange.communication.Communicator;
import nl.groep5.xchange.controllers.DownloadController;
import nl.groep5.xchange.models.DownloadableFile;

public class Downloader extends Thread {

	private DownloadableFile downloadableFile;
	private RandomAccessFile progressFile;
	private int curBlock;
	private RandomAccessFile targetFile;
	public boolean running;

	public Downloader(DownloadableFile downloadableFile) {
		this.downloadableFile = downloadableFile;
	}

	@Override
	public void run() {
		running = true;
		if (Settings.DEBUG) {
			System.out.println("Downloader.run()");
		}
		try {
			progressFile = new RandomAccessFile(
					downloadableFile.getDownloadStatusFile(), "rw");
			targetFile = new RandomAccessFile(
					downloadableFile.getDownloadTargetFile(), "rw");

			if (Settings.DEBUG) {
				System.out.println("complete: " + downloadIsComplete());
			}
			if (downloadIsComplete()) {
				completeDownload();
				return;
			}

			int blockSize;
			while (!downloadIsComplete() && running) {

				if (curBlock < downloadableFile.getNoOfBlocks() - 1) {
					blockSize = Settings.getBlockSize();
				} else {
					blockSize = downloadableFile.getRestSize();
				}

				byte[] result = Communicator.GetBlockFromPeer(downloadableFile,
						curBlock, blockSize);

				targetFile.seek(Settings.getBlockSize() * curBlock);
				targetFile.write(result);

				progressFile.seek(curBlock);
				progressFile.write((byte) '1');
				downloadableFile.updateProgressBar();
			}

			if (downloadIsComplete()) {
				completeDownload();
			} else {
				progressFile.close();
				targetFile.close();
			}

			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean downloadIsComplete() throws IOException {
		byte[] byteArray = new byte[(int) progressFile.length()];
		progressFile.seek(0);
		progressFile.readFully(byteArray);

		String content = new String(byteArray);
		curBlock = content.indexOf('0');

		return curBlock == -1;

	}

	private void completeDownload() throws IOException {
		progressFile.close();
		targetFile.close();

		if (Settings.DEBUG) {
			System.out.println("complete download of "
					+ downloadableFile.getRealFileName());
		}
		downloadableFile.getDownloadStatusFile().delete();

		File newFileName = new File(Settings.getSharedFolder()
				+ downloadableFile.getRealFileName());

		// delete if new file already exists
		if (newFileName.exists()) {
			if (Settings.DEBUG) {
				System.out
						.println("Going to delete downloaded file because target already exsists");
			}
			downloadableFile.getDownloadTargetFile().delete();
		}

		downloadableFile.getDownloadTargetFile().renameTo(newFileName);

		DownloadController.removeDownload(downloadableFile);
	}
}