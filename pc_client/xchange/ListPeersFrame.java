package xchange;

/*
 * Creates frame/window to show a list of peers registered
 */

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

public class ListPeersFrame extends JFrame {

    private final int W = 130; // frame width
    private final int H = 250; // frame height

    private JButton jbtClose;

    public ListPeersFrame (XNameServer ns) {

	// set layout BorderLayout
	setLayout(new BorderLayout(5,10));
	
	// create a default JList and fill it
	try {
	    JList jlist = new JList(ns.getPeers());
	    add(jlist, BorderLayout.CENTER);
	}
	catch (Exception e) {
	    e.printStackTrace();
	}

	// and a close button
	jbtClose = new JButton("Close");
	add(jbtClose, BorderLayout.SOUTH);
        jbtClose.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) { dispose(); }
	    });

	setSize(W, H);
	setResizable(false);
	setTitle("List of peers registered");
	setLocationRelativeTo(null); // center the frame
	setVisible(true);
    }
}
