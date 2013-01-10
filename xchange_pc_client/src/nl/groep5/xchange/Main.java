package nl.groep5.xchange;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import nl.groep5.xchange.communication.Communicator;
import nl.groep5.xchange.controllers.DownloadController;
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
		Application.launch(Main.class, (String[]) null);
	}

	@Override
	public void start(Stage primaryStage) {
		try {
			createDirectorys();

			stage = primaryStage;
			stage.setTitle("XChange");
			stage.setMinWidth(MINIMUM_WINDOW_WIDTH);
			stage.setMinHeight(MINIMUM_WINDOW_HEIGHT);
			stage.setMaxWidth(MAXIMUM_WINDOW_WIDTH);
			stage.setMaxHeight(MAXIMUM_WINDOW_HEIGHT);
			stage.getIcons().add(new Image("file:icon.png"));
			try {
				Settings.getInstance().load();
			} catch (IOException e) {
				if (Settings.DEBUG) {
					System.out.println("Could not load the settings");
				}
			}
			if (Settings.getInstance().validate()) {
				Communicator.setRouterSettings();
				if (Settings.getInstance().getState() == null) {
					Main.state = State.LOCAL_STOP;
				} else {
					Main.state = Settings.getInstance().getState();
				}
				System.out.println("STATE " + Main.state.toString());
				gotoMain();
			} else {
				Main.state = State.NO_SETTINGS;
				gotoSettings();
			}
			primaryStage.show();
			initSytem();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void createDirectorys() {
		File directory = new File(Settings.getSharedFolder());
		if (!directory.exists()) {
			directory.mkdirs();
		}

		directory = new File(Settings.getInfoFolder());
		if (!directory.exists()) {
			directory.mkdirs();
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
						if (!Communicator.updatePeers()) {
							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									Main.showDialog("Could not connect to the nameserver");
								}
							});
						}
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
		DownloadController.stopDownloads();
		Communicator.unregisterFromNameServer();
		super.stop();
	}

	public static void showDialog(String message) {
		final Stage dialog = new Stage();
		dialog.initStyle(StageStyle.UTILITY);
		Button okButton = new Button("Close");
        okButton.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent arg0) {
                dialog.close();
            }
          
        });
		Scene scene = new Scene(VBoxBuilder.create()
				.children(new Text(message), okButton)
				.alignment(Pos.CENTER)
				.spacing(6)
                .padding(new Insets(5))
                .build());
		dialog.setTitle("Warning!");
		dialog.setScene(scene);
		dialog.centerOnScreen();
		dialog.show();
	}
}
