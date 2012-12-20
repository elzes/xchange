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
		System.out.println("Downloader.run()");
		try {
			progressFile = new RandomAccessFile(
					downloadableFile.getDownloadStatusFile(), "rw");
			targetFile = new RandomAccessFile(
					downloadableFile.getDownloadTargetFile(), "rw");

			System.out.println("complete: " + downloadIsComplete());
			if (downloadIsComplete()) {
				completeDownload();
				return;
			}

			int blockSize;
			while (!downloadIsComplete()) {
				System.out.println("download not complete "
						+ downloadableFile.getRealFileName());
				System.out.println("download block" + curBlock + "//"
						+ downloadableFile.getNoOfBlocks());

				if (curBlock < downloadableFile.getNoOfBlocks() - 1) {
					blockSize = Settings.getBlockSize();
				} else {
					blockSize = downloadableFile.getRestSize();
				}
				System.out.println("blockSize " + blockSize);
				byte[] result = Communicator.GetBlockFromPeer(downloadableFile,
						curBlock, blockSize);

				System.out.println("--" + new String(result) + "--");

				targetFile.seek(Settings.getBlockSize() * curBlock);
				targetFile.write(result);

				progressFile.seek(curBlock);
				progressFile.write((byte) '1');
			}

			completeDownload();
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

		System.out.println("complete download of "
				+ downloadableFile.getRealFileName());

		downloadableFile.getDownloadStatusFile().delete();

		File newFileName = new File(Settings.getSharedFolder()
				+ downloadableFile.getRealFileName());

		// delete if new file already exists
		if (newFileName.exists()) {
			System.out
					.println("Going to delete downloaded file because target already exsists");
			downloadableFile.getDownloadTargetFile().delete();
		}

		downloadableFile.getDownloadTargetFile().renameTo(newFileName);
	}
}