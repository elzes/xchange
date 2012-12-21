package nl.groep5.xchange;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
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
import nl.groep5.xchange.communication.Communicator;
import nl.groep5.xchange.controllers.MainController;
import nl.groep5.xchange.controllers.SettingsController;
import nl.groep5.xchange.externalInput.OtherPeerListener;

/**
 * Main Application. This class handles navigation and user session.
 */
public class Main extends Application {

	private static final int PEER_UPDATE_TIME = 30;// time in seconds
	private Timeline peerUpdater;
	private OtherPeerListener otherPeerListener;

	private Stage stage;
	public static State state;
	private final double MINIMUM_WINDOW_WIDTH = 200.0;
	private final double MINIMUM_WINDOW_HEIGHT = 150.0;
	private final double MAXIMUM_WINDOW_WIDTH = 660.0;
	private final double MAXIMUM_WINDOW_HEIGHT = 420.0;

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) {
		Application.launch(Main.class, (java.lang.String[]) null);
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			stage = primaryStage;
			stage.setTitle("XChange application");
			stage.setMinWidth(MINIMUM_WINDOW_WIDTH);
			stage.setMinHeight(MINIMUM_WINDOW_HEIGHT);
			stage.setMaxWidth(MAXIMUM_WINDOW_WIDTH);
			stage.setMaxHeight(MAXIMUM_WINDOW_HEIGHT);

			try {
				Settings.getInstance().load();
			} catch (IOException e) {
				if (Settings.DEBUG) {
					System.out.println("Could not load the settings");
				}
			}
			if (Settings.getInstance().validate()) {
				gotoMain();
			} else {
				gotoSettings();
			}
			primaryStage.show();
			initSytem();
		} catch (Exception ex) {
			ex.printStackTrace();
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

	public void showSettings() {
		gotoSettings();
	}

	public void closeSettings() {
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

	@Override
	public void stop() throws Exception {
		otherPeerListener.stopListening();
		super.stop();
	}
}
