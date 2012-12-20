package old.StorageServer;

public class StorageServerRemoveCommand extends StorageServerCommand {

	public StorageServerRemoveCommand(String line)
			throws InvalidCommandException {
		super(line);
	}

	@Override
	protected int getCommandCountAfterFileName() {
		return 0;
	}

	@Override
	protected int getMinCommandLength() {
		return 2;
	}
}
