package old.StorageServer;

public class StorageServerPostCommand extends StorageServerCommand {
	private int blockNr;
	private int fileSize;
	private byte[] bytes;

	public StorageServerPostCommand(String line) throws InvalidCommandException {
		super(line);

		int fileSize;
		try {
			fileSize = Integer.parseInt(command[command.length - 4]);

		} catch (NumberFormatException e) {
			new InvalidCommandException("Invalid filesize");
			return;
		}
		setFileSize(fileSize);

		int blockNr;
		try {
			blockNr = Integer.parseInt(command[command.length - 3]);
		} catch (NumberFormatException e) {
			new InvalidCommandException("Invalid blocknr");
			return;
		}

		setBlockNr(blockNr);

		String bytesToWrite = command[command.length - 1];
		setBitesToWrite(bytesToWrite.getBytes());
	}

	private void setBlockNr(int blockNr) {
		this.blockNr = blockNr;
	}

	private void setFileSize(int fileSize) {
		this.fileSize = fileSize;
	}

	public long getFileSize() {
		return fileSize;
	}

	public int getBlockNr() {
		return blockNr;
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

	@Override
	protected int getCommandCountAfterFileName() {
		return 3;
	}
}