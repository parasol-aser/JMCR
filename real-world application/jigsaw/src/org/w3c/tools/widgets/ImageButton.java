// ImageButton.java
// $Id: ImageButton.java,v 1.1 2010/06/15 12:20:35 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.widgets ;

import java.awt.AWTEventMulticaster;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ImageButton extends Canvas {

    /**
     * This MouseListener is used to do all the paint operations
     * and to generate ActionEvents when a click occured 
     */

    private class ImageButtonListener extends MouseAdapter {

	public void mousePressed(MouseEvent me) {
	    // paint down
	    paintShadow(false);
	}

	public void mouseReleased(MouseEvent me) {
	    // paint up
	    paintShadow(true);
	}

        public void mouseClicked(MouseEvent me) {
	    // fire a new ActionEvent
	    fireActionEvent();
	}
    }

    protected boolean switchable = false;
    protected Image img1 = null;
    protected Image img2 = null;
    protected Image currentImg = null;
    private int width = -1;
    private int height = -1;
    private String command;

    transient ActionListener actionListener;  

    int max (int a, int b) {
      return ( (a < b) ? b : a);
    }

    /**
     * Gets the size of the Image to calculate the minimum size of the 
     * Button
     */

    protected void initSize() {
      if (switchable) {
	width = max (img1.getWidth(this), img2.getWidth(this));
	height = max (img1.getHeight(this) , img2.getHeight(this));
      } else {
	width = currentImg.getWidth(this);
	height = currentImg.getHeight(this);
      }
    }

    public void switchImage() {
      if (switchable) {
	if (currentImg == img1)
	  currentImg = img2;
	else 
	  currentImg = img1;
	}
      paintShadow(true);
    }

    /**
     * paint the ImageButton in its initial shape
     * @param g A Graphics
     */

    public void paint(Graphics g) {
	paintShadow(true);
    }

    /**
     * paints the ImageButton using double buffering
     * @param raised A boolean which shows the state of the button
     */

    protected void paintShadow(boolean raised) {
	Graphics g = getGraphics();
	Shape s = g.getClip();
	Image dbi;
	Graphics dbg;
	Color bg = getBackground();
	Dimension d = getSize();
	int dx;
	int dy;

	dbi = ImageCache.getImage(this, d.width, d.height);
	dbg = dbi.getGraphics();
	dbg.setClip(s);
	dbg.setColor(bg);
	dx = d.width - width;
	dy = d.height - height;
	dbg.clearRect(0, 0, d.width, d.height);
	dbg.fill3DRect(1, 1, d.width-2, d.height-2, raised);
	dbg.drawImage(currentImg, dx/2, dy/2, this); 
	g.drawImage(dbi, 0, 0, this);
    }

    /**
     * called when more informations about the image are available.
     * When the size is available, the ImageButton notifies its container
     * that the size may have changed.
     * @see java.awt.image.ImageObserver
     */

    public boolean imageUpdate(Image img, int flaginfo,
			       int x, int y, int width, int height) {
// 	if ((flaginfo & (HEIGHT|WIDTH)) != 0) {
// 	    this.width = width;
// 	    this.height = height;
// 	    Container parent = getParent();
// 	    if(parent != null) 
// 		parent.doLayout();
// 	}
        initSize();
	Container parent = getParent();
	if(parent != null) 
	  parent.doLayout();
	return super.imageUpdate(img, flaginfo, x, y, width, height);
    }

    /**
     * Returns the minimum size of the ImageButton
     */

    public Dimension getMinimumSize() {
	return new Dimension(width+8, height+8);
    }

    /**
     * Returns the preferred size of the ImageButton
     */

    public Dimension getPreferredSize() {
	return new Dimension(width+8, height+8);
    }

    /**
     * Sets the action command String used when an ActionEvent is fired
     * @param command The command String
     */

    public void setActionCommand(String command) {
        this.command = command;
    }

    /**
     * Returns the action command String
     */

    public String getActionCommand() {
        return command; 
    }

    /**
     * Adds an action listener to this ImageButton
     * @param al The ActionListener
     */

    public synchronized void addActionListener(ActionListener al) {
	actionListener = AWTEventMulticaster.add(actionListener, al);
    }   

    /**
     * Removes an action listener to this ImageButton
     * @param al The ActionListener
     */

    public synchronized void removeActionListener(ActionListener al) {
        actionListener = AWTEventMulticaster.remove(actionListener, al);
    }

    /**
     * fire a new ActionEvent and process it, if some listeners are listening
     */

    protected void fireActionEvent() {
	if(actionListener != null) {
	    ActionEvent ae = new ActionEvent(this,
					     ActionEvent.ACTION_PERFORMED,
					     command);
	    actionListener.actionPerformed(ae);
	}
    }

    /**
     * Construct an ImageButton with the specified action command
     * @param img1 The image of this ImageButton
     * @param img2 The image of this ImageButton
     * @param command The action command String
     */

    public ImageButton(Image img1, Image img2, String command) {
        this.switchable = true;
	this.img1 = img1;
	this.img2 = img2;	
	this.currentImg = img1;
	this.command = command;
 	addMouseListener(new ImageButtonListener());
	prepareImage(img1, this);
	prepareImage(img2, this);
	initSize();
    }

    /**
     * Construct an ImageButton with the specified action command
     * @param img1 The image of this ImageButton
     * @param realesed The image of this ImageButton
     */

    public ImageButton(Image img1, Image img2) {
        this(img1,img2,"");
    }
    /**
     * Construct an ImageButton with the specified action command
     * @param img The image of this ImageButton
     * @param command The action command String
     */

    public ImageButton(Image img, String command) {
        this.switchable = false;
	this.currentImg = img;
	this.command = command;
 	addMouseListener(new ImageButtonListener());
	prepareImage(currentImg, this);
	initSize();
    }

    /**
     * Construct an ImageButton with no action command
     * @param img The image of this ImageButton
     */

    public ImageButton(Image img) {
	this(img, "");
    }
}
