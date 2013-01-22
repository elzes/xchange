package nl.groep5.xchange;

import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import nl.groep5.xchange.models.DownloadableFile;

public class DownloadList extends SimpleListProperty<DownloadableFile> {

	private static ObservableList<DownloadableFile> downloadList = FXCollections
			.observableArrayList();

	public DownloadList() {
		super(downloadList);
	}

	@Override
	public int indexOf(Object arg0) {
		if (arg0 == null)
			return -1;

		if (!(arg0 instanceof DownloadableFile))
			return -1;

		int i = 0;
		DownloadableFile other = (DownloadableFile) arg0;
		for (DownloadableFile file : this) {
			if (file.getRealFileName().equals(other.getRealFileName()))
				return i;
			i++;
		}

		return -1;
	}

	@Override
	public boolean contains(Object arg0) {
		if (!(arg0 instanceof DownloadableFile))
			return false;

		DownloadableFile other = (DownloadableFile) arg0;
		for (DownloadableFile file : this) {
			if (file.getRealFileName().equals(other.getRealFileName()))
				return true;
		}
		return false;
	}
}
