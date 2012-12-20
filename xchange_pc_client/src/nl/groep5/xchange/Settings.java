package nl.groep5.xchange;

public class Settings {

	private static String localIp;
	private static int incomingConnectionPort = 9000;
	private static String sharedFolder = "xchange/shared/";
	private static int blockSize = 64 * 1024;
	private static String tmpExtension = ".!xch";
	private static String infoExtension = ".!info";
	private static String infoFolder = "xchange/INFO/";

	private static String routerIP;
	private static String storageServerIP;
	private static String nameServerIP;

	public static String getNameServerIp() {
		return nameServerIP;
	}

	public static String getStorageServerIp() {
		return storageServerIP;
	}

	public static String getRouterIp() {
		return routerIP;
	}

	public static int getNameServerPort() {
		return 9001;
	}

	public static String getLocalIp() {
		return localIp;
	}

	public static void setLocalIp(String localIp) {
		Settings.localIp = localIp;
	}

	public static int getIncomingConnectionPort() {
		return incomingConnectionPort;
	}

	public static String getSharedFolder() {
		return sharedFolder;
	}

	public static int getBlockSize() {
		return blockSize;
	}

	public static String getTmpExtension() {
		return tmpExtension;
	}

	public static String getInfoFolder() {
		return infoFolder;
	}

	public static String getInfoExtension() {
		return infoExtension;
	}
	
	public static void setNameServerIp(String newNameServerIP) {
		nameServerIP = newNameServerIP;
	}

	public static void setStorageServerIp(String newStorageServerIP) {
		storageServerIP = newStorageServerIP;
	}

	public static void setRouterIp(String newRouterIP) {
		routerIP = newRouterIP;
	}
}