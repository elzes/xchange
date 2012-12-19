package nl.groep5.xchange.control;
import javafx.fxml.FXML;
import javafx.scene.control.Control;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;


public class DownloadsController extends Control {

	
	@FXML protected void handleS(KeyEvent keyEvent){
		if(keyEvent.getCode() == KeyCode.ENTER) {
			System.out.println("Key pressed!");
		}
	}
}
