package nl.groep5.xchange.client.controllers;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import nl.groep5.xchange.client.Communicator;
import nl.groep5.xchange.client.models.Peer;

public class PeersController implements Initializable {

	@FXML
	ListView<Peer> peersListView;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		peersListView.setItems(Communicator.getPeers());
	}
}