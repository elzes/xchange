package nl.groep5.xchange.storageServer;

public class StorageServerGetCommand extends StorageServerCommand {

	private int seekDistance;
	private int curBlockSize;

	public StorageServerGetCommand(String line) throws InvalidCommandException {
		super(line);
		int seekDistance;
		try {
			seekDistance = Integer.parseInt(command[2]);

		} catch (NumberFormatException e) {
			new InvalidCommandException("Invalid seekdistance");
			return;
		}
		setSeekDistance(seekDistance);

		int curBlockSize;
		try {
			curBlockSize = Integer.parseInt(command[3]);

		} catch (NumberFormatException e) {
			new InvalidCommandException("Invalid blocksize");
			return;
		}
		setCurBlockSize(curBlockSize);
	}

	private void setCurBlockSize(int curBlockSize) {
		this.curBlockSize = curBlockSize;
	}

	public int getCurBlockSize() {
		return curBlockSize;
	}

	private void setSeekDistance(int seekDistance) {
		this.seekDistance = seekDistance;
	}

	public int getSeekDistance() {
		return seekDistance;
	}

	@Override
	protected int getMinCommandLength() {
		return 4;
	}
}