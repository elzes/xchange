package nl.groep5.xchange.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Control;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class SearchController extends Control
{

    @FXML
    protected void handleSearchField(KeyEvent keyEvent)
    {
        if (keyEvent.getCode() == KeyCode.ENTER)
        {
            TextField searchField = (TextField) keyEvent.getSource();
            String searchInput = searchField.getText();
            
        }
    }
}
