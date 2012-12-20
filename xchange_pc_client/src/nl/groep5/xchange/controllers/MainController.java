package nl.groep5.xchange.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import nl.groep5.xchange.Main;
import nl.groep5.xchange.State;

import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;

public class MainController extends AnchorPane implements Initializable 
{
	
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
	State state;
    

    private static final int PEER_UPDATE_TIME = 30;//time in seconds
    private Timeline peerUpdater;
    
    private Main application;
    
    public void setApp(Main application){
        this.application = application;
    }

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		
	}
	
	@FXML
	protected void SettingsClick(ActionEvent event) {
		application.setupSettings();
		System.out.println("Settings clicked!");
	}

	@FXML
	protected void ShareFileClick(ActionEvent event) {
		System.out.println("Share file clicked!");
	}

	@FXML
	protected void ExitClick(ActionEvent event) {
		System.exit(0);
	}

	@FXML
	protected void StartPCDownloadClick(ActionEvent event) {
		state = State.LOCAL_START;
		updateGUI();
	}

	@FXML
	protected void StopPCDownloadClick(ActionEvent event) {
		state = State.LOCAL_STOP;
		updateGUI();
	}

	@FXML
	protected void StartRouterDownloadClick(ActionEvent event) {
		state = State.ROUTER_START;
		updateGUI();
	}

	@FXML
	protected void StopRouterDownloadClick(ActionEvent event) {
		state = State.ROUTER_STOP;
		updateGUI();
	}

	public void updateGUI() {
		switch (state) {
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