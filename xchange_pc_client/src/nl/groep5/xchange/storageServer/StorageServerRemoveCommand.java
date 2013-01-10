package nl.groep5.xchange.storageServer;

public class StorageServerRemoveCommand extends StorageServerCommand {

	public StorageServerRemoveCommand(String line)
			throws InvalidCommandException {
		super(line);
	}

	@Override
	protected int getMinCommandLength() {
		return 2;
	}
}
