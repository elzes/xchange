package nl.groep5.xchange;

import java.io.IOException;
import java.net.ConnectException;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import nl.groep5.xchange.externalInput.OtherPeerListener;

public class Main extends Application {

	private static final int PEER_UPDATE_TIME = 30;// time in seconds
	private Timeline peerUpdater;
	private OtherPeerListener otherPeerListener;

	@Override
	public void start(Stage stage) throws Exception {
		Parent root = FXMLLoader
				.load(getClass().getResource("views/main.fxml"));
		stage.setTitle("FXML Welcome");
		stage.setScene(new Scene(root, 640, 400));
		stage.show();

		initSytem();
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

	public static void main(String[] args) {
		launch(args);
	}
}