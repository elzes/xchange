package nl.groep5.xchange.controllers;


import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import nl.groep5.xchange.Main;
import nl.groep5.xchange.Settings;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
public class SettingsController  extends AnchorPane implements Initializable  {

	@FXML
	TextField NameServerIP;
	@FXML
	TextField StorageServerIP;
	@FXML
	TextField RouterIP;
	
	
	private Main application;
    
    
    public void setApp(Main application){
        this.application = application;
		application.loadSettings();
		NameServerIP.setText(Settings.getNameServerIp());
		StorageServerIP.setText(Settings.getStorageServerIp());
		RouterIP.setText(Settings.getRouterIp());
    }


	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		
	}
	
	@FXML
	protected void saveClick(ActionEvent ae){
		HashMap<String, String> hm = new HashMap<String, String>();
	    hm.put("ns", NameServerIP.getText());
	    hm.put("ss", StorageServerIP.getText());
	    hm.put("rt", RouterIP.getText());
	    Settings.setNameServerIp(NameServerIP.getText());
	    Settings.setStorageServerIp(StorageServerIP.getText());
	    Settings.setRouterIp(RouterIP.getText());
		application.saveSettings(hm);
	}
	
	@FXML
	protected void cancelClick(ActionEvent ae){
		application.cancelSettings();
	}
}
