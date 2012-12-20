package nl.groep5.xchange.controllers;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.Control;
import nl.groep5.xchange.Communicator;
import nl.groep5.xchange.Settings;
import nl.groep5.xchange.models.DownloadableFile;

public class DownloadController extends Control implements Initializable {

	private static ObservableList<DownloadableFile> pendingDownloads = FXCollections
			.observableArrayList();

	public static void addDownload(DownloadableFile downloadableFile) {
		System.out.println("Added download " + downloadableFile.getFileName());
		pendingDownloads.add(downloadableFile);
		Communicator.startDownload(downloadableFile);
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		File[] downloadingFiles = new File(Settings.getSharedFolder())
				.listFiles(new FilenameFilter() {

					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(Settings.getTmpExtension());
					}
				});

		for (File file : downloadingFiles) {
			DownloadableFile downloadableFile = Communicator
					.getDownloadableFileFromName(file.getName());
			if (downloadableFile != null) {
				pendingDownloads.add(downloadableFile);
				Communicator.startDownload(downloadableFile);
			}
		}
	}
}