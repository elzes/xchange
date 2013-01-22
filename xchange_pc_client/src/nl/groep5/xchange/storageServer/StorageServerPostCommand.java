package nl.groep5.xchange.storageServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.apache.commons.io.IOUtils;

public class StorageServerPostCommand extends StorageServerCommand {
	private int seekDistance;
	private int fileSize;
	private byte[] bytes;
	private int blockSize;

	public StorageServerPostCommand(String line, InputStream dis,
			PrintWriter printWriter) throws InvalidCommandException {
		super(line);

		int fileSize;
		try {
			fileSize = Integer.parseInt(command[2]);

		} catch (NumberFormatException e) {
			new InvalidCommandException("Invalid filesize");
			return;
		}
		setFileSize(fileSize);

		int seekDistance;
		try {
			seekDistance = Integer.parseInt(command[3]);
		} catch (NumberFormatException e) {
			new InvalidCommandException("Invalid seekdistance");
			return;
		}
		setSeekDistance(seekDistance);

		int blockSize;
		try {
			blockSize = Integer.parseInt(command[4]);
		} catch (NumberFormatException e) {
			new InvalidCommandException("Invalid blocknr");
			return;
		}
		setBlockSize(blockSize);
		printWriter.println("OK");

		byte[] bytes = new byte[getBlockSize()];
		System.out.println("GOing to read " + getBlockSize() + " bytes");

		try {
			IOUtils.readFully(dis, bytes, 0, blockSize);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		System.out.println("Bytes received:"+new String(bytes));
		setBitesToWrite(bytes);
	}

	private void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
	}

	public int getBlockSize() {
		return blockSize;
	}

	private void setSeekDistance(int seekDistance) {
		this.seekDistance = seekDistance;
	}

	private void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}

	public long getFileSize() {
		return fileSize;
	}

	public int getSeekDistance() {
		return seekDistance;
	}

	private void setBitesToWrite(byte[] bytes) {
		this.bytes = bytes;
	}

	public byte[] getBytesToWrite() {
		return bytes;
	}

	@Override
	protected int getMinCommandLength() {
		return 5;
	}
}