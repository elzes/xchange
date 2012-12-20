package nl.groep5.xchange;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import nl.groep5.xchange.controllers.MainController;
import nl.groep5.xchange.controllers.SettingsController;
import nl.groep5.xchange.externalInput.OtherPeerListener;

/**
 * Main Application. This class handles navigation and user session.
 */
public class Main extends Application {

	private static final int PEER_UPDATE_TIME = 30;// time in seconds
	private static boolean running;
	private Timeline peerUpdater;
	private OtherPeerListener otherPeerListener;

	private Stage stage;
	private final double MINIMUM_WINDOW_WIDTH = 200.0;
	private final double MINIMUM_WINDOW_HEIGHT = 150.0;
	private final double MAXIMUM_WINDOW_WIDTH = 660.0;
	private final double MAXIMUM_WINDOW_HEIGHT = 420.0;

	private String routerIP;
	private String storageServerIP;
	private String nameServerIP;

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		Application.launch(Main.class, (java.lang.String[]) null);
	}

	@Override
	public void start(Stage primaryStage) {
		running = true;
		try {
			stage = primaryStage;
			stage.setTitle("XChange application");
			stage.setMinWidth(MINIMUM_WINDOW_WIDTH);
			stage.setMinHeight(MINIMUM_WINDOW_HEIGHT);
			stage.setMaxWidth(MAXIMUM_WINDOW_WIDTH);
			stage.setMaxHeight(MAXIMUM_WINDOW_HEIGHT);
			gotoMain();
			primaryStage.show();
			initSytem();
		} catch (Exception ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void initSytem() throws ConnectException, IOException {
		Communicator.signUpToNameServer();
		otherPeerListener = new OtherPeerListener();
		otherPeerListener.start();

		peerUpdater = new Timeline(new KeyFrame(
				Duration.seconds(PEER_UPDATE_TIME),
				new EventHandler<ActionEvent>() {

					@Override
					public void handle(ActionEvent event) {
						Communicator.updatePeers();
					}
				}));
		peerUpdater.setCycleCount(Timeline.INDEFINITE);
		peerUpdater.playFrom(Duration.seconds(PEER_UPDATE_TIME - 1));// -1
																		// second
																		// for
																		// direct
																		// update:)
	}

	public void setupSettings() {
		gotoSettings();
	}

	public void saveSettings(HashMap<String, String> hm) {
		try {
			FileWriter of = new FileWriter("xchange/settings.txt");
			// using Interface Map.Entry
			for (Map.Entry<String, String> e : hm.entrySet()) {
				of.write(e.getKey() + "=" + e.getValue() + "\n");
			}
			of.close();
		}
		// if the named file does not exist, cannot be created, cannot be opened
		// ...
		catch (IOException e) {
			System.err.println("Error writing to xchange/settings.txt");
			e.printStackTrace();
		}
		gotoMain();
	}

	public void cancelSettings() {
		gotoMain();
	}

	private void gotoSettings() {
		try {
			SettingsController settings = (SettingsController) replaceSceneContent("views/settings.fxml");
			settings.setApp(this);
		} catch (Exception ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private void gotoMain() {
		try {
			MainController main = (MainController) replaceSceneContent("views/main.fxml");
			main.setApp(this);
		} catch (Exception ex) {
			Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	private Initializable replaceSceneContent(String fxml) throws Exception {
		FXMLLoader loader = new FXMLLoader();
		InputStream in = Main.class.getResourceAsStream(fxml);
		loader.setBuilderFactory(new JavaFXBuilderFactory());
		loader.setLocation(Main.class.getResource(fxml));
		AnchorPane page;
		try {
			page = (AnchorPane) loader.load(in);
		} finally {
			in.close();
		}
		Scene scene = new Scene(page, 640, 405);
		stage.setScene(scene);
		stage.sizeToScene();
		return (Initializable) loader.getController();
	}

	public boolean loadSettings() {
		int i;
		String[] s = null;
		try {
			BufferedReader inStream = new BufferedReader(new FileReader(
					"xchange/settings.txt"));
			for (i = 0; i < 3; i++) {
				// read a line and split it
				s = inStream.readLine().split("=");
				if (s[0].equals("ns")) {
					setNameServerIP(s[1]);
				}
				if (s[0].equals("ss")) {
					setStorageServerIP(s[1]);
				}
				if (s[0].equals("rt")) {
					setRouterIP(s[1]);
				}
			}
			inStream.close();
		}

		catch (IOException e) {
			System.err.println("WARNING : xchange/settings.txt not found.");
			return false;
		}
		return true;
	}

	public String getRouterIP() {
		return routerIP;
	}

	public void setRouterIP(String routerIP) {
		this.routerIP = routerIP;
	}

	public String getStorageServerIP() {
		return storageServerIP;
	}

	public void setStorageServerIP(String storageServerIP) {
		this.storageServerIP = storageServerIP;
	}

	public String getNameServerIP() {
		return nameServerIP;
	}

	public void setNameServerIP(String nameServerIP) {
		this.nameServerIP = nameServerIP;
	}

	@Override
	public void stop() throws Exception {
		otherPeerListener.stopListening();
		super.stop();
	}

	public static boolean isRunning() {
		return running;
	}
}
