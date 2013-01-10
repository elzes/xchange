package nl.groep5.xchange.storageServer;

import nl.groep5.xchange.Settings;

public abstract class StorageServerCommand {

	private String fileName;
	protected String[] command;

	public StorageServerCommand(String line) throws InvalidCommandException {
		System.out.println("new command " + line);
		command = line.split(Settings.getSplitCharRegEx());
		if (command.length < getMinCommandLength()) {
			throw new InvalidCommandException("invalid command length");
		}

		// merge filename if it contains <sp>
		String fileName = command[1];

		setFileName(fileName);
	}

	protected abstract int getMinCommandLength();

	protected void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFileName() {
		return fileName;
	}
}