package xchange;

/*
 * Creates frame/window for defining IP adresses of
 * change client, nameserver, storage server and router
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class SettingsFrame extends JFrame implements ActionListener
{

    private final int W = 350; // frame width
    private final int H = 190; // frame height

    private GUI g;

    private JTextField jtf_xc;
    private JTextField jtf_ns;
    private JTextField jtf_ss;
    private JTextField jtf_rt;

    JButton jbtOK;
    JButton jbtCancel;

    public SettingsFrame(GUI g)
    {
        this.g = g;

        // load settings from settings.txt
        boolean def = g.loadSettings();

        // this Xchange client
        JLabel jlb_xc = new JLabel("Client IP");
        jlb_xc.setBounds(10, 10, 180, 20);
        add(jlb_xc);

        jtf_xc = new JTextField();
        jtf_xc.setBounds(160, 10, 100, 20);
        jtf_xc.setText(g.xc.ip);
        add(jtf_xc);

        // nameserver
        JLabel jlb_ns = new JLabel("Name Server IP");
        jlb_ns.setBounds(10, 35, 180, 20);
        add(jlb_ns);

        jtf_ns = new JTextField();
        jtf_ns.setBounds(160, 35, 100, 20);
        jtf_ns.setText(g.ns.ip);
        add(jtf_ns);

        // storage server
        JLabel jlb_ss = new JLabel("Storage Server IP");
        jlb_ss.setBounds(10, 60, 180, 20);
        add(jlb_ss);

        jtf_ss = new JTextField();
        jtf_ss.setBounds(160, 60, 100, 20);
        jtf_ss.setText(g.ss.ip);
        add(jtf_ss);

        // router
        JLabel jlb_rt = new JLabel("Router IP");
        jlb_rt.setBounds(10, 85, 180, 20);
        add(jlb_rt);

        jtf_rt = new JTextField();
        jtf_rt.setBounds(160, 85, 100, 20);
        jtf_rt.setText(g.rt.ip);
        add(jtf_rt);

        jbtOK = new JButton("OK");
        jbtOK.setBounds(270, 130, 60, 20);
        add(jbtOK);
        jbtOK.addActionListener(this);

        jbtCancel = new JButton("Cancel");
        jbtCancel.setBounds(180, 130, 80, 20);
        add(jbtCancel);
        jbtCancel.addActionListener(this);

        setSize(W, H);
        setResizable(false);
        setLayout(null);
        setTitle("Define IP addresses");
        setLocationRelativeTo(null); // center the frame
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e)
    {
        // handle the button events

        if (e.getSource() == jbtOK)
        {
            if (Debug.DEBUG)
            {
                System.out.println("OK pressed");
            }
            try
            {
                // write settings to settings.txt and to corresponding objects
                if (!check_complete())
                {
                    JOptionPane.showMessageDialog(this, "Please fill in all fields !");
                    return;
                } else
                {
                    // fill a hashmap
                    HashMap<String, String> hm = new HashMap<String, String>();
                    hm.put("xc", jtf_xc.getText());
                    hm.put("ns", jtf_ns.getText());
                    hm.put("ss", jtf_ss.getText());
                    hm.put("rt", jtf_rt.getText());
                    g.writeSettings(hm);
                    dispose();
                    g.g_state = State.WITH_SETTINGS;
                    if (Debug.DEBUG)
                    {
                        System.out.println("updateGUI() called");
                    }
                    // register this client with the nameserver
                    try
                    {
                        g.ns.register(g.xc.ip);
                    } catch (Exception ioe)
                    {
                        ioe.printStackTrace();
                    }
                    g.updateGUI();
                }
            } catch (Exception ex)
            {
                ex.printStackTrace();
                return;
            }
        }

        if (e.getSource() == jbtCancel)
        {
            if (Debug.DEBUG)
            {
                System.out.println("CANCEL pressed");
            }
            dispose();
        }
    }

    // check if all textfields are filled in
    private boolean check_complete()
    {
        if (jtf_xc.getText().equals(""))
            return false;
        if (jtf_ns.getText().equals(""))
            return false;
        if (jtf_ss.getText().equals(""))
            return false;
        if (jtf_rt.getText().equals(""))
            return false;
        return true;
    }
}
