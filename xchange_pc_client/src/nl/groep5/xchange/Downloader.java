package nl.groep5.xchange;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ConnectException;
import java.util.ArrayList;

import nl.groep5.xchange.communication.Communicator;
import nl.groep5.xchange.controllers.DownloadController;
import nl.groep5.xchange.models.DownloadableFile;

public class Downloader extends Thread {

	private DownloadableFile downloadableFile;
	private RandomAccessFile progressFile;
	private int curBlock;
	private RandomAccessFile targetFile;
	public boolean running;
	private ArrayList<Integer> excludeList = new ArrayList<Integer>();

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
		} catch (IOException e) {
			e.printStackTrace();
		}

		int blockSize;

		try {
			while (!downloadIsComplete() && running) {

				if (curBlock < downloadableFile.getNoOfBlocks() - 1) {
					blockSize = Settings.getBlockSize();
				} else {
					blockSize = downloadableFile.getRestSize();
				}
				while (downloadableFile.getPeer() == null) {
					Communicator.searchPeerForBlock(downloadableFile, curBlock,
							blockSize);
				}
				byte[] result;
				try {
					result = Communicator.GetBlockFromPeer(downloadableFile,
							curBlock, blockSize);
					if (result == null) {
						excludeList.add(curBlock);
						continue;
					}

					targetFile.seek(Settings.getBlockSize() * curBlock);
					targetFile.write(result);

					progressFile.seek(curBlock);
					progressFile.write((byte) '1');
					downloadableFile.updateProgressBar();
				} catch (ConnectException e) {
					e.printStackTrace();
					downloadableFile.setPeer(null);// reset peer
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		try {
			if (downloadIsComplete()) {
				completeDownload();
			} else {
				progressFile.close();
				targetFile.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return;

	}

	private boolean downloadIsComplete() throws IOException {
		byte[] byteArray = new byte[(int) progressFile.length()];
		progressFile.seek(0);
		progressFile.readFully(byteArray);

		String content = new String(byteArray);
		curBlock = content.indexOf('0', curBlock);
		if (Settings.DEBUG) {
			System.out.println("block " + curBlock + "selected");
		}

		if (excludeList.contains(new Integer(curBlock))) {
			System.out.println("Curblock in excludeList");
			curBlock = content.indexOf('0', curBlock + 1);
			System.out.println("New curblock " + curBlock);
		}

		if (curBlock == -1) {
			curBlock = content.indexOf('0');
			System.out
					.println("last curblock is last to be downloaded curblock now again "
							+ curBlock);
		}

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