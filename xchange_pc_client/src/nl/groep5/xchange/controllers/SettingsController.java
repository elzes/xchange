package nl.groep5.xchange.controllers;


import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

import nl.groep5.xchange.Main;

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
		NameServerIP.setText(application.getNameServerIP());
		StorageServerIP.setText(application.getStorageServerIP());
		RouterIP.setText(application.getRouterIP());
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
		application.saveSettings(hm);
	}
	
	@FXML
	protected void cancelClick(ActionEvent ae){
		application.cancelSettings();
	}
}
