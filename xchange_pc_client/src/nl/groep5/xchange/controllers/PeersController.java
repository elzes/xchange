package nl.groep5.xchange.controllers;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import nl.groep5.xchange.Communicator;

public class PeersController extends Control
{

    @FXML
    protected void handleTabChange(Event event)
    {
        Tab tab = (Tab) event.getSource();
        if (tab.isSelected())
        {
            Communicator.updatePeers();
        }

        @SuppressWarnings("unchecked")
        ListView<Object> peers = (ListView<Object>) lookup("#peersListView");
    }
}