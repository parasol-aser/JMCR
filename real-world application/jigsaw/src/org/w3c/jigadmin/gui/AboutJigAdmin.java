// AboutJigAdmin.java
// $Id: AboutJigAdmin.java,v 1.1 2010/06/15 12:21:49 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.gui;

import java.awt.Component;
import java.awt.Color;
import java.awt.Container;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseAdapter;

import javax.swing.BoxLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;

import org.w3c.jigadmin.widgets.Icons;
import org.w3c.jigadmin.widgets.ClosableDialog;

import org.w3c.tools.widgets.Utilities;

/**
 * The About Jigadmin dialog.
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class AboutJigAdmin extends ClosableDialog {

    private JLabel teamlabel = null;

    private AboutJigAdmin (Frame frame, String title, boolean modal) {
	super(frame, title, modal);
	build();
    }

    private JLabel getLabel(String label) {
	JLabel lbl = new JLabel(label, JLabel.CENTER);
	lbl.setForeground(Color.black);
	lbl.setFont(Utilities.boldFont);
	return lbl;  
    }

    /**
     * The dialog is about to be closed.
     */
    protected void close() {
	setVisible(false);
	dispose();
    }

    /**
     * Build the interface.
     */
    protected void build() {
	Container cont = getContentPane();
	cont.setLayout(new BorderLayout());

	JLabel w3clabel  = new JLabel(Icons.w3chIcon);
	JLabel jiglabel  = new JLabel(Icons.jigsawIcon);
	teamlabel = new JLabel(Icons.serverIcon);
	teamlabel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

	JLabel label1 = getLabel("JigAdmin, The Jigsaw Administration tool");
	JLabel label2 = getLabel("Version 2.0");
	JLabel label3 = getLabel("A program by the Jigsaw Team (W3C)");
	JLabel label4 = getLabel("http://www.w3.org/Jigsaw/");
	JLabel label5 = getLabel("(c) COPYRIGHT MIT, INRIA and Keio, 1999.");

	teamlabel.addMouseListener(new MouseAdapter() {
	    public void mouseEntered(MouseEvent e) {
		teamlabel.setIcon(Icons.teamIcon);
	    }

	    public void mouseExited(MouseEvent e) {
		teamlabel.setIcon(Icons.serverIcon);
	    }
	});

	JButton okb = new JButton("OK");
	okb.setFont(Utilities.boldFont);
	okb.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		close();
	    }
	});

	JPanel p1 = new JPanel(new FlowLayout());
	p1.add(w3clabel);
	p1.add(jiglabel);

	JPanel p2 = new JPanel();
	p2.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
	p2.setLayout(new BoxLayout(p2, BoxLayout.Y_AXIS));
	p2.add(label1);
	p2.add(label2);
	p2.add(label3);
	p2.add(label4);
	p2.add(label5);

	JPanel p3 = new JPanel(new BorderLayout());
	p3.add(teamlabel, BorderLayout.WEST);
	p3.add(p2, BorderLayout.CENTER);

	JPanel p4 = new JPanel();
	p4.add(okb);

	cont.add(p1, BorderLayout.NORTH);
	cont.add(p3, BorderLayout.CENTER);
	cont.add(p4, BorderLayout.SOUTH);
	pack();
    }

    /**
     * Show the About Jigadmin dialog.
     * @param parent the parent Component.
     */
    public static void show(Component parent) {
	Frame frame = JOptionPane.getFrameForComponent(parent);
	AboutJigAdmin aj = new AboutJigAdmin(frame, "About JigAdmin", false);
	aj.setLocationRelativeTo(frame);
	aj.show();
    }
}
