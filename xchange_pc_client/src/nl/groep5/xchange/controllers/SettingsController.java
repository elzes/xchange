package nl.groep5.xchange.controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import nl.groep5.xchange.Main;
import nl.groep5.xchange.Settings;
import nl.groep5.xchange.State;
import nl.groep5.xchange.communication.Communicator;

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
		if (fieldsAreEmpty()) {
			Main.showDialog("One or more fields are empty!");
			return;
		}
		Settings settings = Settings.getInstance();
		String tempNameServerIP = settings.getNameServerIp();
		String tempStorageServerIP = settings.getStorageServerIp();
		String tempRouterIP = settings.getRouterIp();
		settings.setNameServerIp(nameServerIP.getText());
		settings.setStorageServerIp(storageServerIP.getText());
		settings.setRouterIp(routerIP.getText());
		try {
			if (!settings.validate()) {
				Main.showDialog("Settings not valid");
				settings.setNameServerIp(tempNameServerIP);
				settings.setStorageServerIp(tempStorageServerIP);
				settings.setRouterIp(tempRouterIP);
			} else {
				settings.save();
				Communicator.resetConnections();
				Communicator.setRouterSettings();
				if (Main.state == null || Main.state == State.NO_SETTINGS) {
					Main.state = State.LOCAL_STOP;
					MainController.processStateChange();
				}
				application.closeSettings();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private boolean fieldsAreEmpty() {
		if (nameServerIP.getText().equals("")
				|| storageServerIP.getText().equals("")
				|| routerIP.getText().equals(""))
			return true;
		else
			return false;
	}

	@FXML
	protected void cancelClick(ActionEvent actionEvent) {
		application.closeSettings();
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
	}
}
