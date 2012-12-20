package old.xchange.gui;

/**
 * NOTE : this application was developed on linux, for Windows all "/" in directory names should be changed in "\"
 *
 * compile as "javac xchange/GUI.java" and run as "java xchange.GUI"
 *
 * settings.txt is in dir ./xchange
 * shared files are in sub dir ./xchange/shared
 * info files are in sub dir ./xchange/INFO
 *
 * NOTE :there are 4 servers (Xchange peer can act as a server and as a client),
 * so there must be 4 ips and ports defined (Xchange client, nameserver, storageserver and router)
 *
 */

import java.awt.Color;
import java.awt.Container;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;

import old.xchange.Debug;
import old.xchange.Download;
import old.xchange.Fileinfo;
import old.xchange.Router;
import old.xchange.State;
import old.xchange.XNameServer;
import old.xchange.XStorageServer;
import old.xchange.Xchange;

public class GUI extends JFrame implements ActionListener {

	private static final long serialVersionUID = -8754927216594188985L;
	// constants
	public static final int MAX_DOWNLOADS = 8;
	private final int W = 680; // frame width
	private final int H = 400; // frame height

	// gui state; depending on state menu items are enabled/disabled
	public State g_state;

	/*
	 * volatile : make sure that every thread accessing this field will read
	 * this current value instead of using a local/cached copy
	 */

	public volatile boolean stop_all_downloads = false; // evaluated in
														// Download.run

	// references
	public Xchange xchange;
	public XNameServer nameServer;
	public XStorageServer storageServer;
	public Router router;

	// list of Fileinfo objects
	public ArrayList<Fileinfo> downloadList;

	// list of Download objects (execute the actual downloading)
	public ArrayList<Download> downloads;

	// menu items
	JMenuItem miSettings;
	JMenuItem miShare;
	JMenuItem miExit;
	JMenuItem miSearch;
	JMenuItem miList;
	JMenuItem miStart;
	JMenuItem miStop;
	JMenuItem miStartRT;
	JMenuItem miStopRT;

	// to show file attributes
	JLabel[] labelFile, labelSize;
	JProgressBar[] progFile;

	public GUI() {

		// create objects and store references
		xchange = new Xchange();
		nameServer = new XNameServer(this);
		storageServer = new XStorageServer(this);
		router = new Router();

		downloadList = new ArrayList<Fileinfo>();
		downloads = new ArrayList<Download>();

		// check if directory xchange/shared exists
		if (!check_shared()) {
			JOptionPane.showMessageDialog(this,
					"Please create directory xchange/shared with r+w access.");
			System.err
					.println("ERROR : directory xchange/shared does not exist.");
			System.exit(0);
		}

		// check if directory xchange/INFO exists
		if (!check_info()) {
			JOptionPane.showMessageDialog(this,
					"Please create directory xchange/INFO with r+w access.");
			System.err
					.println("ERROR : directory xchange/INFO does not exist.");
			System.exit(0);
		}

		// mesaage if settings.txt does not exist or is not fully defined
		g_state = State.NO_SETTINGS; // assume settings are not yet defined
		if (loadSettings()) {
			g_state = State.WITH_SETTINGS;
			// register this client with the nameserver
			try {
				nameServer.register(xchange.ip);
			} catch (Exception e) {
				System.err.println("ERROR : Forgot to start the Nameserver ?");
				e.printStackTrace();
			}
		} else {
			JOptionPane.showMessageDialog(this,
					"Please first define settings (File menu > Settings).");
		}

		MakeFrame();
		updateGUI();
	}

	/*
	 * Note there are 4 frames :- this main frame with menubars- the list peers
	 * frame- the search file frame- the define IP addresses frame
	 */

	public void MakeFrame() {
		JMenuBar jmb = new JMenuBar();

		// file menu
		JMenu mFile = new JMenu("File");
		jmb.add(mFile);

		miSettings = new JMenuItem("Settings");
		mFile.add(miSettings);
		miSettings.addActionListener(this);

		miShare = new JMenuItem("Share file");
		mFile.add(miShare);
		miShare.addActionListener(this);

		miExit = new JMenuItem("Exit");
		mFile.add(miExit);
		miExit.addActionListener(this);

		// network menu

		JMenu mNetwork = new JMenu("Network");
		jmb.add(mNetwork);

		miSearch = new JMenuItem("Search file (add to download list)");
		mNetwork.add(miSearch);
		miSearch.addActionListener(this);

		miList = new JMenuItem("List peers");
		mNetwork.add(miList);
		miList.addActionListener(this);

		// downloads menu

		JMenu mDownloads = new JMenu("Downloads");
		jmb.add(mDownloads);

		// local download by this client
		miStart = new JMenuItem("Start download");
		mDownloads.add(miStart);
		miStart.addActionListener(this);

		miStop = new JMenuItem("Stop download");
		mDownloads.add(miStop);
		miStop.addActionListener(this);

		miStartRT = new JMenuItem("Start router download (take over)");
		mDownloads.add(miStartRT);
		miStartRT.addActionListener(this);

		miStopRT = new JMenuItem("Stop router download");
		mDownloads.add(miStopRT);
		miStopRT.addActionListener(this);

		setJMenuBar(jmb);

		// add labels and progress bars to this frame

		Container pane = getContentPane();
		pane.setLayout(null);

		labelFile = new JLabel[MAX_DOWNLOADS];
		labelSize = new JLabel[MAX_DOWNLOADS];
		progFile = new JProgressBar[MAX_DOWNLOADS];

		JLabel h1 = new JLabel("size in bytes");
		h1.setBounds(240, 10, 80, 20);
		h1.setForeground(Color.blue);
		pane.add(h1);
		JLabel h2 = new JLabel("progress");
		h2.setBounds(380, 10, 80, 20);
		h2.setForeground(Color.blue);
		pane.add(h2);

		for (int i = 0; i < MAX_DOWNLOADS; i++) {
			labelFile[i] = new JLabel("Unassigned (" + prepend(i) + ")");
			// x, y, width, height
			labelFile[i].setBounds(30, 40 + 20 * i, 240, 20);
			labelSize[i] = new JLabel("?");
			labelSize[i].setBounds(240, 40 + 20 * i, 80, 20);
			progFile[i] = new JProgressBar(0, 0);
			progFile[i].setBounds(380, 40 + 20 * i, 250, 15);
			progFile[i].setForeground(Color.red);
			pane.add(labelFile[i]);
			pane.add(labelSize[i]);
			pane.add(progFile[i]);
		}

		setTitle("XChange");
		setSize(W, H);
		setResizable(false);
		setLocationRelativeTo(null); // center the frame

		// add a window listener to handle close event
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				handleExit();
			}
		});
		setVisible(true);
	}

	// Xchange application is fully user-driven, so GUI is the place to start
	public static void main(String[] args) throws Exception {
		new GUI();
	}

	/**
	 * gui event handler
	 */

	public void actionPerformed(ActionEvent e) {

		// handle menu items of the main frame; switch only works with int type
		// :-(

		if (e.getSource() == miSettings) {
			if (Debug.DEBUG) {
				System.out.println("new SettingsFrame");
			}
			new SettingsFrame(this);
		}

		if (e.getSource() == miShare) {
			addSharedFile();
		}

		if (e.getSource() == miExit) {
			handleExit();
		}

		if (e.getSource() == miSearch) {
			new SearchFrame(this);
		}

		if (e.getSource() == miList) {
			new ListPeersFrame(this.nameServer);
		}

		if (e.getSource() == miStart) {
			g_state = State.LOCAL_START;
			updateGUI();
			startLocalDownload();
		}

		if (e.getSource() == miStop) {
			g_state = State.LOCAL_STOP;
			clearDownloadList();
			updateGUI();
			stopLocalDownload();
		}

		if (e.getSource() == miStartRT) {
			g_state = State.ROUTER_START;
			updateGUI();
			startRouterDownload();
		}

		if (e.getSource() == miStopRT) {
			g_state = State.ROUTER_STOP;
			updateGUI();
			stopRouterDownload();
		}
	}

	/**
	 * convert to string; if i < 10 add a zero to align labels on the frame
	 */

	private String prepend(int i) {
		if (i < 10) {
			return "0" + i;
		} else {
			return "" + i;
		}
	}

	private boolean check_shared() {
		File file = new File("xchange/shared");
		if (file.exists()) {
			return true;
		} else {
			return false;
		}
	}

	private boolean check_info() {
		File file = new File("xchange/INFO");
		if (file.exists()) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * load the file settings.txt if file exisits return true; it is assumed
	 * that file has 4 lines of the form "xc = 127.0.0.1" if file does not
	 * exists return false
	 */

	public boolean loadSettings() {
		int i;
		String[] s = null;
		try {
			BufferedReader inStream = new BufferedReader(new FileReader(
					"xchange/settings.txt"));
			for (i = 0; i < 4; i++) {
				// read a line and split it
				s = inStream.readLine().split("=");
				if (s[0].equals("xc")) {
					xchange.ip = s[1];
				}
				if (s[0].equals("ns")) {
					nameServer.ip = s[1];
				}
				if (s[0].equals("ss")) {
					storageServer.ip = s[1];
				}
				if (s[0].equals("rt")) {
					router.ip = s[1];
				}
			}
		}

		catch (IOException e) {
			System.err.println("WARNING : xchange/settings.txt not found.");
			return false;
		}
		return true;
	}

	/*
	 * write settings to settings.txt and to corresponding objects
	 */

	public void writeSettings(HashMap<String, String> hm) {

		if (Debug.DEBUG) {
			System.out
					.println("GUI.writeSettings called with following HasMap values :");
			System.out.println(hm);
		}

		xchange.ip = hm.get("xc");
		nameServer.ip = hm.get("ns");
		storageServer.ip = hm.get("ss");
		router.ip = hm.get("rt");

		try {
			FileWriter of = new FileWriter("xchange/settings.txt");
			// using Interface Map.Entry
			for (Map.Entry<String, String> e : hm.entrySet()) {
				of.write(e.getKey() + "=" + e.getValue() + "\n");
			}
			of.close();
		}
		// if the named file does not exist, cannot be created, cannot be opened
		// ...
		catch (IOException e) {
			System.err.println("Error writing to xchange/settings.txt");
			e.printStackTrace();
		}
	}

	private void handleExit() {
		// unregister this cleint with nameserver
		try {
			nameServer.unregister(xchange.ip);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.exit(0);
	}

	// called in SearchFrame
	public void addToDownloadList(String filename, long size, String ip) {
		if (Debug.DEBUG) {
			System.out.println("addToDownloadList : " + filename + " " + size
					+ " " + ip);
		}

		Fileinfo f = new Fileinfo(this.xchange, filename, size, ip);

		// avoid adding existing file
		for (Fileinfo tmp : downloadList) {
			if (tmp.name.equals(f.name)) {
				if (Debug.DEBUG) {
					System.out.println("addToDownloadList : file " + filename
							+ " already in list");
				}
				return;
			}
		}

		downloadList.add(f);

		for (int i = 0; i < MAX_DOWNLOADS; i++) {
			// search unassigned label
			if (labelFile[i].getText().substring(0, 4).equals("Unas")) {
				labelFile[i].setText(f.name);
				Long l = (Long) f.size;
				labelSize[i].setText(l.toString());
				labelFile[i].updateUI();
				labelSize[i].updateUI();
				progFile[i].setMaximum(f.nr_of_blocks);
				progFile[i].setValue(0);
				break; // add only once
			}

		}
	}

	// when downloading is stopped
	public void clearDownloadList() {
		downloadList.clear();
		for (int i = 0; i < MAX_DOWNLOADS; i++) {
			labelFile[i].setText("Unassigned (" + prepend(i) + ")");
			labelSize[i].setText("?");
			labelFile[i].updateUI();
			labelSize[i].updateUI();
			progFile[i].setValue(0);
			break; // add only once
		}
	}

	/**
	 * add a (complete) file to be shared with other peers
	 */

	private void addSharedFile() {
		try {
			JFileChooser jfc = new JFileChooser(".");
			jfc.setDialogTitle("Add a file to be shared");
			jfc.setApproveButtonText("Share");

			if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				File fSelected = jfc.getSelectedFile();
				if (fSelected == null) {
					return;
				} else {
					File fi = fSelected;
					File fo = new File("shared/" + fSelected.getName());

					try {
						FileInputStream fis = new FileInputStream(fi);
						FileOutputStream fos = new FileOutputStream(fo);

						// Define the size of our buffer for buffering file data
						byte[] buffer = new byte[4096];
						// each time read and write up to buffer.length bytes
						// read counts nr of bytes available
						int read;
						while ((read = fis.read(buffer)) != -1) {
							fos.write(buffer, 0, read);
						}
						// Finally close the input and output stream after we've
						// finished with them.
						fis.close();
						fos.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

		catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * start the local downloads; hm contains a list filenames and ip addresses
	 */

	private void startLocalDownload() {
		int i = 0;

		if (Debug.DEBUG) {
			System.out.println("startLocalDownload");
		}
		// evaluated in Download.run
		stop_all_downloads = false;

		// list of downloads was created in SearchFrame
		for (Fileinfo f : downloadList) {
			// create thread
			Download d = new Download(this, f);
			// keep a list of Downloads
			downloads.add(d);
			d.start();
		}
	}

	/**
	 * Note that Thread.stop() is depricated : "Many uses of stop should be
	 * replaced by code that simply modifies some variable to indicate that the
	 * target thread should stop running. The target thread should check this
	 * variable regularly, and return from its run method."
	 */

	private void stopLocalDownload() {
		stop_all_downloads = true;
		if (Debug.DEBUG) {
			System.out.println("stopLocalDownload");
		}
	}

	private void startRouterDownload() {
		if (Debug.DEBUG) {
			System.out.println("startRouterDownload");
		}
	}

	private void stopRouterDownload() {
		if (Debug.DEBUG) {
			System.out.println("stopRouterDownload");
		}
	}

	/**
	 * enable/disable menu items based on GUI state
	 */

	public void updateGUI() {
		switch (g_state) {
		case NO_SETTINGS:
			miSettings.setEnabled(true);
			miShare.setEnabled(false);
			miSearch.setEnabled(false);
			miList.setEnabled(false);
			miStart.setEnabled(false);
			miStop.setEnabled(false);
			miStartRT.setEnabled(false);
			miStopRT.setEnabled(false);
			break;
		case WITH_SETTINGS:
			miSettings.setEnabled(true);
			miShare.setEnabled(true);
			miSearch.setEnabled(true);
			miList.setEnabled(true);
			miStart.setEnabled(true);
			miStop.setEnabled(false);
			miStartRT.setEnabled(true);
			miStopRT.setEnabled(false);
			break;
		case LOCAL_START:
			miSettings.setEnabled(false);
			miShare.setEnabled(true);
			miSearch.setEnabled(false);
			miList.setEnabled(true);
			miStart.setEnabled(false);
			miStop.setEnabled(true);
			miStartRT.setEnabled(false);
			miStopRT.setEnabled(false);
			break;
		case LOCAL_STOP:
			miSettings.setEnabled(true);
			miShare.setEnabled(true);
			miSearch.setEnabled(true);
			miList.setEnabled(true);
			miStart.setEnabled(true);
			miStop.setEnabled(false);
			miStartRT.setEnabled(true);
			miStopRT.setEnabled(false);
			break;
		case ROUTER_START:
			miSettings.setEnabled(false);
			miShare.setEnabled(true);
			miSearch.setEnabled(false);
			miList.setEnabled(true);
			miStart.setEnabled(false);
			miStop.setEnabled(false);
			miStartRT.setEnabled(false);
			miStopRT.setEnabled(true);
			break;
		case ROUTER_STOP:
			miSettings.setEnabled(true);
			miShare.setEnabled(true);
			miSearch.setEnabled(true);
			miList.setEnabled(true);
			miStart.setEnabled(true);
			miStop.setEnabled(false);
			miStartRT.setEnabled(false);
			miStopRT.setEnabled(false);
			break;
		}
	}

	/**
	 * when a new block is donloaded, update the progress bars called by
	 * Downloads
	 */

	public void updateProgress(String filename) {
		for (int i = 0; i < MAX_DOWNLOADS; i++) {
			if (labelFile[i].getText().equals(filename)) {
				progFile[i].setValue(progFile[i].getValue() + 1);
				Rectangle progressRect = progFile[i].getBounds();
				progressRect.x = 0;
				progressRect.y = 0;
				progFile[i].paintImmediately(progressRect);
				break;
			}
		}
	}

}
