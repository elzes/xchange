package nl.groep5.xchange.client.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBoxBuilder;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import nl.groep5.xchange.client.Main;
import nl.groep5.xchange.client.Settings;

public class SettingsController extends AnchorPane implements Initializable {

	@FXML
	TextField nameServerIP;
	@FXML
	TextField storageServerIP;
	@FXML
	TextField routerIP;
	@FXML
	Button cancelButton;

	private Main application;

	public void setApp(Main application) {
		this.application = application;

		Settings settings = Settings.getInstance();
		if (!settings.validate()) {
			cancelButton.setDisable(true);
		}

		nameServerIP.setText(settings.getNameServerIp());
		storageServerIP.setText(settings.getStorageServerIp());
		routerIP.setText(settings.getRouterIp());
	}

	@FXML
	protected void saveClick(ActionEvent actionEvent) {
		Settings settings = Settings.getInstance();
		settings.setNameServerIp(nameServerIP.getText());
		settings.setStorageServerIp(storageServerIP.getText());
		settings.setRouterIp(routerIP.getText());
		try {
			if (!settings.validate()) {
				final Stage dialogStage = new Stage();
				dialogStage.initModality(Modality.WINDOW_MODAL);
				dialogStage.setScene(new Scene(VBoxBuilder.create()
						.children(new Text("Invalid settings")).build()));
				dialogStage.show();
			} else {
				settings.save();
				application.closeSettings();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@FXML
	protected void cancelClick(ActionEvent actionEvent) {
		application.closeSettings();
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
	}
}
