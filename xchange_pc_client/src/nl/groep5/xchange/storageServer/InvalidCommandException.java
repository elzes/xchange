package nl.groep5.xchange.storageServer;

public class InvalidCommandException extends Exception {
	private static final long serialVersionUID = 8647107631515595103L;

	public InvalidCommandException(String message) {
		super(message);
	}

}
