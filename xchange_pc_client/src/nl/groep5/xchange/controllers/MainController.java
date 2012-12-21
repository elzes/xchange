package nl.groep5.xchange.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;

import javax.swing.JFileChooser;

import nl.groep5.xchange.Main;
import nl.groep5.xchange.State;

public class MainController extends AnchorPane implements Initializable {

	@FXML
	Button buttonSettings;
	@FXML
	MenuItem settings;
	@FXML
	MenuItem shareFile;
	@FXML
	MenuItem exit;
	@FXML
	MenuItem startPC;
	@FXML
	MenuItem stopPC;
	@FXML
	MenuItem startRouter;
	@FXML
	MenuItem stopRouter;

	private Main application;

	public void setApp(Main application) {
		this.application = application;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub

	}

	@FXML
	protected void SettingsClick(ActionEvent event) {
		application.showSettings();
	}

	@FXML
	protected void ShareFileClick(ActionEvent event) {
		try {
			JFileChooser jfc = new JFileChooser(".");
			jfc.setDialogTitle("Add a file to be shared");
			jfc.setApproveButtonText("Share");

			if (jfc.showOpenDialog(jfc) == JFileChooser.APPROVE_OPTION) {
				File fSelected = jfc.getSelectedFile();
				if (fSelected == null) {
					return;
				} else {
					File fi = fSelected;
					File fo = new File("xchange/shared/" + fSelected.getName());

					try {
						FileInputStream fis = new FileInputStream(fi);
						FileOutputStream fos = new FileOutputStream(fo);

						// Define the size of our buffer for buffering file data
						byte[] buffer = new byte[4096];
						// each time read and write up to buffer.length bytes
						// read counts nr of bytes available
						int read;
						while ((read = fis.read(buffer)) != -1) {
							fos.write(buffer, 0, read);
						}
						// Finally close the input and output stream after we've
						// finished with them.
						fis.close();
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@FXML
	protected void ExitClick(ActionEvent event) {
		System.exit(0);
	}

	@FXML
	protected void StartPCDownloadClick(ActionEvent event) {
		application.state = State.LOCAL_START;
		updateGUI();
	}

	@FXML
	protected void StopPCDownloadClick(ActionEvent event) {
		application.state = State.LOCAL_STOP;
		updateGUI();
	}

	@FXML
	protected void StartRouterDownloadClick(ActionEvent event) {
		application.state = State.ROUTER_START;
		updateGUI();
	}

	@FXML
	protected void StopRouterDownloadClick(ActionEvent event) {
		application.state = State.ROUTER_STOP;
		updateGUI();
	}

	public void updateGUI() {
		switch (application.state) {
		case NO_SETTINGS:
			settings.setDisable(false);
			shareFile.setDisable(true);
			startPC.setDisable(true);
			stopPC.setDisable(true);
			startRouter.setDisable(true);
			stopRouter.setDisable(true);
			break;
		case WITH_SETTINGS:
			settings.setDisable(false);
			shareFile.setDisable(false);
			startPC.setDisable(false);
			stopPC.setDisable(true);
			startRouter.setDisable(false);
			stopRouter.setDisable(true);
			break;
		case LOCAL_START:
			settings.setDisable(true);
			shareFile.setDisable(true);
			startPC.setDisable(true);
			stopPC.setDisable(false);
			startRouter.setDisable(true);
			stopRouter.setDisable(true);
			break;
		case LOCAL_STOP:
			settings.setDisable(false);
			shareFile.setDisable(false);
			startPC.setDisable(false);
			stopPC.setDisable(true);
			startRouter.setDisable(false);
			stopRouter.setDisable(true);
			break;
		case ROUTER_START:
			settings.setDisable(true);
			shareFile.setDisable(true);
			startPC.setDisable(true);
			stopPC.setDisable(true);
			startRouter.setDisable(true);
			stopRouter.setDisable(false);
			break;
		case ROUTER_STOP:
			settings.setDisable(false);
			shareFile.setDisable(false);
			startPC.setDisable(false);
			stopPC.setDisable(true);
			startRouter.setDisable(true);
			stopRouter.setDisable(true);
			break;
		}
	}
}