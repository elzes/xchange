package nl.groep5.xchange.storageServer;

public class StorageServerGetCommand extends StorageServerCommand {

	private int blockNr;

	public StorageServerGetCommand(String line) throws InvalidCommandException {
		super(line);
		int blockNr;
		try {
			blockNr = Integer.parseInt(command[2]);

		} catch (NumberFormatException e) {
			new InvalidCommandException("Invalid filesize");
			return;
		}
		setBlockNr(blockNr);
	}

	private void setBlockNr(int blockNr) {
		this.blockNr = blockNr;
	}

	public int getBlockNr() {
		return blockNr;
	}

	@Override
	protected int getMinCommandLength() {
		return 3;
	}
}