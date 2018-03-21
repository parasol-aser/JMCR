// Probe.java
// $Id: Probe.java,v 1.1 2010/06/15 12:21:48 smhuang Exp $  
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.protocol.http.micp;

import java.awt.Button;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Window;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.UnknownHostException;

import java.io.IOException;
import java.io.PrintStream;

import java.util.EventObject;

/**
 * A real piece of fun, try it !
 */

class Stats implements MICP {
    int    queries = 0;
    int    hits    = 0;
    String lasturl = null;

    final synchronized void handle(int op, String url) {
	if (op == MICP_OP_QUERY)
	    queries++;
	else
	    hits++;
	lasturl = url;
    }

    final synchronized int getQueries() {
	return queries;
    }

    final synchronized int getHits() {
	return hits;
    }

    final synchronized String getLastURL() {
	return lasturl;
    }

}

class MICPReader extends Thread {
    InetAddress     addr   = null;
    int             port   = -1;
    Stats           stats  = null;
    MulticastSocket socket = null;
    MICPReadWrite   micprw = null;

    public void run() {
	byte        buffer[] = new byte[4096];
	MICPMessage msg      = new MICPMessage();
	while ( true ) {
	    try {
		DatagramPacket p = new DatagramPacket(buffer, buffer.length);
		socket.receive(p);
		micprw.decode(p.getData(), p.getLength(), msg);
		// Stat that message:
		stats.handle(msg.op, msg.url);
	    } catch (Exception ex) {
		ex.printStackTrace();
	    } 
	}
    }

    MICPReader(InetAddress a, int port, Stats stats) 
	throws UnknownHostException, IOException
    {
	// Init:
	this.micprw = new MICPReadWrite();
	this.addr   = a;
	this.port   = port;
	this.stats  = stats;
	// Create and join:
	this.socket = new MulticastSocket(port);
	this.socket.joinGroup(a);
	// Run !
	setName("mICP listener");
	start();
    }
}

public class Probe extends Panel implements Runnable, ActionListener {
    MICPReader reader  = null;
    Stats      stats   = null;
    long       refresh = 500;

    Label  hits    = null;
    Label  queries = null;
    Label  url     = null;
    Button exit    = null;

    /**
     * ActionListener implementation - exit on exit button.
     * @param e The event.
     */

    public void actionPerformed(ActionEvent e) {
	if ( e.getSource() == exit ) {
	    System.out.println("Bye !");
	    System.exit(0);
	}
    }

    protected synchronized void tick() {
	try {
	    wait(refresh);
	} catch (InterruptedException ex) {
	}
    }

    public void run() {
	while ( true ) {
	    // Display stats:
	    hits.setText(Integer.toString(stats.getHits()));
	    queries.setText(Integer.toString(stats.getQueries()));
	    url.setText(stats.getLastURL());
	    // Wait for interval:
	    tick();
	}
    }

    public Probe(InetAddress addr, int port, long refresh) 
	throws UnknownHostException, IOException
    {
	// Create objects:
	this.refresh = refresh;
	this.stats   = new Stats();
	this.reader  = new MICPReader(addr, port, stats);
	// Create widgets:
	GridBagLayout      gb = new GridBagLayout();
	setLayout(gb);
	GridBagConstraints ct = new GridBagConstraints();
	GridBagConstraints cv = new GridBagConstraints();
	// Create the title constraints:
	ct         = new GridBagConstraints() ;
	ct.gridx   = GridBagConstraints.RELATIVE ;
	ct.anchor  = GridBagConstraints.EAST ;
	ct.weighty = 1.0 ;
	// Create the value constraints:
	cv           = new GridBagConstraints() ;
	cv.gridx     = GridBagConstraints.RELATIVE ;
	cv.gridwidth = GridBagConstraints.REMAINDER ;
	cv.fill      = GridBagConstraints.HORIZONTAL ;
	cv.anchor    = GridBagConstraints.WEST ;
	cv.weightx   = 1.0 ;
	cv.weighty   = 1.0 ;
	// Add the number of queries label:
	Label title = new Label("queries");
	gb.setConstraints(title, ct);
	add(title);
	queries = new Label("0");
	queries.setBackground(Color.white);
	gb.setConstraints(queries, cv);
	add(queries);
	// Add the number of hits label:
	title = new Label("hits");
	gb.setConstraints(title, ct);
	add(title);
	hits = new Label("0");
	hits.setBackground(Color.white);
	gb.setConstraints(hits, cv);
	add(hits);
	// Add the last url label:
	title = new Label("url");
	gb.setConstraints(title, ct);
	add(title);
	url = new Label("0");
	url.setBackground(Color.white);
	gb.setConstraints(url, cv);
	add(url);
	// Add the exit button:
	exit = new Button("Exit");
	exit.addActionListener(this);
	gb.setConstraints(exit, ct);
	add(exit);
    }

    public static void usage() {
	PrintStream p = System.out;
	p.println("Probe -a <addr> -p <port> -r <refresh>"
		  + " -w <width> -h <height>");
	p.println("\taddr: multicast group address");
	p.println("\tport: multicast port");
	p.println("\trefresh: refresh interval in ms");
	p.println("\twidth: width at startup (pixels)");
	p.println("\theight: height at startup (pixels)");
	System.exit(1);
    }

    public static void main(String args[]) {
	InetAddress addr    = null;
	int         port    = -1;
	long        refresh = 500;
	int         width   = 330;
	int         height  = 130;
	try {
	    for (int i = 0 ; i < args.length ; i++) {
		if ( args[i].equals("-a") && (i+1 < args.length) ) {
		    addr = InetAddress.getByName(args[++i]);
		} else if ( args[i].equals("-p") && (i+1 < args.length) ) {
		    port = Integer.parseInt(args[++i]);
		} else if ( args[i].equals("-r") && (i+1 < args.length) ) {
		    refresh = Long.parseLong(args[++i]);
		} else if ( args[i].equals("-w") && (i+1 < args.length) ) {
		    width = Integer.parseInt(args[++i]);
		} else if ( args[i].equals("-h") && (i+1 < args.length) ) {
		    height = Integer.parseInt(args[++i]);
		} else {
		    usage();
		} 
	    }
	} catch (Exception ex) {
	    usage();
	}
	// Check args:
	if ((addr == null) || (port == -1))
	    usage();
	// Run it:
	Probe probe = null;
	try {
	    probe = new Probe(addr, port, refresh);
	} catch (Exception ex) {
	    ex.printStackTrace();
	    System.exit(1);
	}
	Frame toplevel = new Frame("mICP-Probe") ;
	toplevel.add ("Center", probe) ;
	toplevel.setSize(new Dimension(width, height));
	toplevel.show() ;
	new Thread(probe).start();
    }

}
