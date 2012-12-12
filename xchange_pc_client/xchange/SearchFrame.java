package xchange;

/*
 * Creates frame/window for executing a file search
 */

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class SearchFrame extends JFrame implements ActionListener
{

    private final int W = 500; // frame width
    private final int H = 500; // frame height
    private GUI g;

    // model and jtable holding list of peers
    private DefaultTableModel model;
    private JTable table;

    private JTextField jpattern;
    private JButton jbtSearch, jbtClear, jbtAddToList, jbtClose;

    public SearchFrame(GUI g)
    {

        this.g = g;

        // set layout as BorderLayout
        setLayout(new BorderLayout(5, 10));

        // *** add the top panel
        JPanel top = new JPanel();

        // set layout 1 row 2 columns
        top.setLayout(new GridLayout(1, 2));

        jpattern = new JTextField("*");
        top.add(jpattern);

        jbtSearch = new JButton("Search");
        jbtSearch.addActionListener(this);
        top.add(jbtSearch);

        add(top, BorderLayout.NORTH);

        // *** add the center panel
        model = new DefaultTableModel();
        model.addColumn("Filename");
        model.addColumn("Size in bytes");
        model.addColumn("IP address");

        table = new JTable(model);

        TableColumn col = table.getColumnModel().getColumn(0);
        // set column width of "filename"
        col.setPreferredWidth(200);
        // set single selection
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        // *** add the bottom panel
        JPanel bottom = new JPanel();
        // set layout 1 row 2 columns
        bottom.setLayout(new GridLayout(1, 2));
        jbtAddToList = new JButton("AddToList");
        jbtAddToList.addActionListener(this);
        jbtClear = new JButton("Clear");
        jbtClear.addActionListener(this);
        jbtClose = new JButton("Close");
        jbtClose.addActionListener(this);

        bottom.add(jbtAddToList);
        bottom.add(jbtClear);
        bottom.add(jbtClose);

        add(bottom, BorderLayout.SOUTH);

        setSize(W, H);
        setResizable(false);
        setTitle("Search file and add to download list");
        setLocationRelativeTo(null); // center the frame
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e)
    {
        // handle the button events
        if (e.getSource() == jbtSearch)
        {
            clearTable();
            // do a LIST on the namerver
            for (String ip : g.ns.getPeers())
            {
                // do a SEARCH on all peers
                for (Fileinfo f : g.xc.searchFile(jpattern.getText(), ip))
                {
                    if (Debug.DEBUG)
                    {
                        System.out.println("Found file : " + f.name + " " + f.size + " " + ip);
                    }
                    model.addRow(new Object[] { f.name, f.size, ip });
                }
            }
        }

        if (e.getSource() == jbtClear)
        {
            clearTable();
        }

        if (e.getSource() == jbtClose)
        {
            dispose();
        }

        if (e.getSource() == jbtAddToList)
        {
            // add selected file to download list in GUI
            if (g.downloadList.size() < g.MAX_DOWNLOADS)
            {
                // get the row from the JTable
                int row = table.getSelectedRow();
                String filename = (String) model.getValueAt(row, 0);
                long size = (Long) model.getValueAt(row, 1);
                String ip = (String) model.getValueAt(row, 2);

                // add it to the list of Fileinfo objects
                g.addToDownloadList(filename, size, ip);
            } else
            {
                JOptionPane.showMessageDialog(this, "Reached maximum nr of downloads !");
            }
        }
    }

    // clear all rows in the tablemodel
    private void clearTable()
    {
        for (int i = model.getRowCount() - 1; i >= 0; i--)
        {
            model.removeRow(i);
        }
    }
}
