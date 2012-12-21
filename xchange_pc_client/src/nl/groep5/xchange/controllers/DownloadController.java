package nl.groep5.xchange.controllers;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Control;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import nl.groep5.xchange.Downloader;
import nl.groep5.xchange.Main;
import nl.groep5.xchange.Settings;
import nl.groep5.xchange.State;
import nl.groep5.xchange.communication.Communicator;
import nl.groep5.xchange.models.DownloadableFile;

public class DownloadController extends Control implements Initializable {

	public static final double PROGRESSBAR_WIDTH = 160;
	@FXML
	TableView<DownloadableFile> downloads;
	@FXML
	TableColumn<DownloadableFile, String> fileName;
	@FXML
	TableColumn<DownloadableFile, String> fileSize;
	@FXML
	TableColumn<DownloadableFile, ProgressBar> progress;

	private static ObservableList<DownloadableFile> pendingDownloads = FXCollections
			.observableArrayList();

	public static void addDownload(DownloadableFile downloadableFile) {
		if (Settings.DEBUG) {
			System.out.println("Added download "
					+ downloadableFile.getFileName());
		}
		pendingDownloads.add(downloadableFile);
		if (Main.state == State.LOCAL_START)
			Communicator.startDownload(downloadableFile);
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {

		downloads.setItems(pendingDownloads);
		downloads.setPlaceholder(new Text("No pending downloads"));
		fileName.setCellValueFactory(new PropertyValueFactory<DownloadableFile, String>(
				"fileName"));
		fileSize.setCellValueFactory(new PropertyValueFactory<DownloadableFile, String>(
				"fileSize"));
		progress.setCellValueFactory(new PropertyValueFactory<DownloadableFile, ProgressBar>(
				"ProgressBar"));

		reloadPendingDownloads();
	}

	private void reloadPendingDownloads() {

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
			}
		}
	}

	public static void progressUpdated(final DownloadableFile downloadableFile) {
		pendingDownloads.remove(downloadableFile);
		pendingDownloads.add(downloadableFile);
	}

	public static void removeDownload(DownloadableFile downloadableFile) {
		pendingDownloads.remove(downloadableFile);
	}

	public static void startDownloads() {
		for (DownloadableFile downloadableFile : pendingDownloads) {
			Communicator.startDownload(downloadableFile);
		}
	}

	public static void stopDownloads() {
		for (DownloadableFile downloadableFile : pendingDownloads) {
			Downloader downloader = downloadableFile.getDownLoader();
			if (downloader != null) {
				downloader.running = false;
			}
		}
	}
}