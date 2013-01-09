package nl.groep5.xchange.controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import nl.groep5.xchange.Main;
import nl.groep5.xchange.Settings;
import nl.groep5.xchange.State;
import nl.groep5.xchange.communication.Communicator;

public class MainController extends AnchorPane implements Initializable {

	@FXML
	Button buttonSettings;
	@FXML
	static MenuItem settings;
	@FXML
	static MenuItem shareFile;
	@FXML
	MenuItem exit;
	@FXML
	static MenuItem startPC;
	@FXML
	static MenuItem stopPC;
	@FXML
	static MenuItem startRouter;
	@FXML
	static MenuItem stopRouter;

	private Main application;

	public void setApp(Main application) {
		this.application = application;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		processStateChange();
	}

	@FXML
	protected void SettingsClick(ActionEvent event) {
		application.showSettings();
	}

	@FXML
	protected void ShareFileClick(ActionEvent event) {

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Add a file to be shared");
		List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);

		if (selectedFiles != null) {
			for (File file : selectedFiles) {
				File newFile = new File(Settings.getSharedFolder()
						+ file.getName());
				try {
					Files.copy(file.toPath(), newFile.toPath(),
							StandardCopyOption.REPLACE_EXISTING);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

	@FXML
	protected void ExitClick(ActionEvent event) {
		Platform.exit();
	}

	@FXML
	protected void StartPCDownloadClick(ActionEvent event) {
		Main.state = State.LOCAL_START;
		processStateChange();
	}

	@FXML
	protected void StopPCDownloadClick(ActionEvent event) {
		Main.state = State.LOCAL_STOP;
		processStateChange();
	}

	@FXML
	protected void StartRouterDownloadClick(ActionEvent event) {
		Main.state = State.ROUTER_START;
		processStateChange();
	}

	@FXML
	protected void StopRouterDownloadClick(ActionEvent event) {
		Main.state = State.ROUTER_STOP;
		processStateChange();
	}

	public static void processStateChange() {

		try {
			Settings.getInstance().save();
		} catch (IOException e) {
			e.printStackTrace();
		}

		if (settings == null || shareFile == null || startPC == null
				|| stopPC == null || startRouter == null || stopRouter == null)
			return;

		switch (Main.state) {
		case NO_SETTINGS:
			settings.setDisable(false);
			shareFile.setDisable(true);
			startPC.setDisable(true);
			stopPC.setDisable(true);
			startRouter.setDisable(true);
			stopRouter.setDisable(true);
			Settings.getInstance().setState(Main.state);
			break;
		case WITH_SETTINGS:
			settings.setDisable(false);
			shareFile.setDisable(false);
			startPC.setDisable(false);
			stopPC.setDisable(true);
			startRouter.setDisable(false);
			stopRouter.setDisable(true);
			Settings.getInstance().setState(Main.state);
			break;
		case LOCAL_START:
			settings.setDisable(true);
			shareFile.setDisable(true);
			startPC.setDisable(true);
			stopPC.setDisable(false);
			startRouter.setDisable(true);
			stopRouter.setDisable(true);
			DownloadController.startDownloads();
			Settings.getInstance().setState(Main.state);
			break;
		case LOCAL_STOP:
			settings.setDisable(false);
			shareFile.setDisable(false);
			startPC.setDisable(false);
			stopPC.setDisable(true);
			startRouter.setDisable(false);
			stopRouter.setDisable(true);
			DownloadController.stopDownloads();
			Settings.getInstance().setState(Main.state);
			break;
		case ROUTER_START:
			if (Communicator.startRouterDownload()) {
				settings.setDisable(true);
				shareFile.setDisable(true);
				startPC.setDisable(true);
				stopPC.setDisable(true);
				startRouter.setDisable(true);
				stopRouter.setDisable(false);
				Settings.getInstance().setState(Main.state);
			} else {
				Main.showDialog("Could not start router download");
			}
			break;
		case ROUTER_STOP:
			if (Communicator.stopRouterDownload()) {
				settings.setDisable(false);
				shareFile.setDisable(false);
				startPC.setDisable(true);
				stopPC.setDisable(true);
				startRouter.setDisable(true);
				stopRouter.setDisable(true);
				Settings.getInstance().setState(Main.state);
			} else {
				Main.showDialog("Could not stop router download");
			}
			break;
		}

		try {
			Settings.getInstance().save();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}