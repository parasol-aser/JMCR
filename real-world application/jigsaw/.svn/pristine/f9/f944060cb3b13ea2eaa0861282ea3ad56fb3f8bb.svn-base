// Slider.java
// $Id: Slider.java,v 1.1 2010/06/15 12:20:38 smhuang Exp $
// Author: bmahe@sophia.inria.fr
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.widgets;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

/**
 * Slider : 
 * @author Benoit Mahe <bmahe@sophia.inria.fr>
 */

public class Slider extends Canvas {

    //inner classes

    class PointerClickListener extends MouseAdapter {

	Slider  slider  = null;

	public void mouseClicked(MouseEvent e) {
	    Object target = e.getComponent();
	    if (target == slider)
		slider.movePointerTo(e.getX(), e.getY());
	}

	public PointerClickListener(Slider s) {
	    this.slider  = s;
	}

    }

    class PointerMotionListener implements MouseMotionListener {

	Slider  slider  = null;

	public void mouseDragged(MouseEvent e) {
	    Object target = e.getComponent();
	    if (target == slider)
		slider.movePointerTo(e.getX(),e.getY());
	}

	public void mouseMoved(MouseEvent e) {
	}

	PointerMotionListener(Slider s) {
	    this.slider  = s;
	}

    }

    class Pointer {

	private int width       = 0;
	private int height      = 0;

	private int centerX = 0;

	Dimension size = null;

	int x = 0;
	int y = 0;

	public void fill3DPointer(Graphics g,
				  int x,
				  int y,
				  int width,
				  int height,
				  boolean raised)
	{
	    int xPoints[] = new int[3];
	    int yPoints[] = new int[3];
	    int nPoints   = 3;

	    Color c = g.getColor();
	    Color brighter = c.brighter();
	    Color darker = c.darker();

	    if (!raised) g.setColor(darker);

	    g.fillRect(x + 1, y + 1, width - 2, 2 * height / 3);
	    xPoints[0] = x;
	    yPoints[0] = y + 2 * height / 3;
	    xPoints[1] = x + width / 2;
	    yPoints[1] = y + height;
	    xPoints[2] = x + width;
	    yPoints[2] = y + 2 * height / 3;
	    g.fillPolygon(xPoints,yPoints,nPoints);
	    g.setColor(raised ? brighter : darker);
	    g.drawLine(x, y, x, y + (2 * height / 3) -1);
	    g.drawLine(x + 1, y, x + width - 2, y);
	    g.drawLine(xPoints[0],yPoints[0],xPoints[1],yPoints[1]);
	    g.setColor(raised ? darker : brighter);
	    g.drawLine(x + width - 1, y, x + width - 1, y + (2*height/3) - 2);
	    g.drawLine(xPoints[1],yPoints[1],xPoints[2] - 1,yPoints[2] - 1);
	}

	public void paint(Graphics g) {
	    fill3DPointer(g,
			  getLocation().x,
			  getLocation().y,
			  width,
			  height,
			  true);
	}

	public void setSize (int width, int height) {
	    this.width    = width;
	    this.height   = height;
	    size = new Dimension(width, height);
	}

	public int getCenterX() {
	    return centerX;
	}

	public void setLocation(int x, int y) {
	    pposition = new Point(x,y);
	    this.x = x-(width/2);
	    this.y = y-(height/2);
	    this.centerX = x;
	}

	public void setLocation(Point p) {
	    setLocation(p.x, p.y);
	}

	public Point getLocation() {
	    return new Point(x,y);
	}

	public Point getNewLocation(int x, int y) {
	    return new Point(getLocation().x + x,
			     getLocation().y + y);
	}

	Pointer(int width, int height, Point location) {
	    setSize(width,height);
	    setLocation(location);
	}

    }

    class Graduation {
	boolean isDouble;
	public int x1;
	public int y1;
	public int x2;
	public int y2;

	public double value;

	public void draw(Graphics g, boolean display) {
	    g.drawLine(x1,y1,x2,y2);
	    if (display) {
		String svalue = getStringValue();
		FontMetrics f = g.getFontMetrics();
		int height    = f.getHeight();
		int width     = f.stringWidth(svalue);
		g.drawString(svalue,x1-width/2,y2+height);
	    }
	}

	public String getStringValue() {
	    if (isDouble)
		return String.valueOf((float)value); //FIXME
	    else
		return String.valueOf((long)value);
	}

	public void showValue(Graphics g) {
	    String svalue = getStringValue();
	    FontMetrics f = g.getFontMetrics();
	    int height = f.getHeight();
	    int width  = f.stringWidth(svalue);
	    g.drawString(svalue,x1-width/2,y2+height);
	}

	public int dx(int x) {
	    int dx = x1-x;
	    return ((dx < 0) ? -dx : dx);
	}

	public Graduation(int x1, int y1, int x2, int y2, double value) {
	    this.x1    = x1;
	    this.y1    = y1;
	    this.x2    = x2;
	    this.y2    = y2;
	    this.value = value;
	    isDouble = true;
	}

	public Graduation(int x1, int y1, int x2, int y2, long value) {
	    this.x1    = x1;
	    this.y1    = y1;
	    this.x2    = x2;
	    this.y2    = y2;
	    this.value = value;
	    isDouble = false;
	}

    }

    //SLIDER itself

    int marginx       = 10;
    int marginy       = 5;
    int defaultWidth  = 180;
    int defaultHeight = 53;
    int width         = 0;
    int height        = 0;

    int minheight     = 53;
    int minwidth      = 150;

    int rect_margin_x = 20;
    int rect_margin_y = 10;

    int pointerWidth  = 12;
    int pointerHeight = 20;

    int pointerX = 0;
    int pointerY = 0;

    int guideHeight = 2;
    int graduationHeight = 5;

    double min  = 0;
    double max  = 0;
    double step = 0;

    int minpixelstep = 2;

    boolean border     = false;
    boolean manageLong = false;

    Color color = Color.gray;

    Dimension size = null;

    Point pposition = null;

    Graduation graduations [] = null;

    Graduation currentGraduation = null;

    protected Pointer pointer = null;

    protected void updateCurrentGraduation() {
	if (pposition.x > (width - marginx - rect_margin_x)) {
	    currentGraduation = graduations[graduations.length - 1];
	} else if (pposition.x < (marginx + rect_margin_x)) {
	    currentGraduation = graduations[0];
	} else {
	    int dx = -1;
	    int mindx = getGraduationLength()+50;
	    int i = 0;
	    while (i < graduations.length) {
		dx = (graduations[i].dx(pposition.x));
		if (dx < mindx) {
		    mindx = dx;
		    i++;
		} else break;
	    }
	    currentGraduation = graduations[i-1];
	}
    }

    protected int getGoodX(int x) {
	if (x > (width - marginx - rect_margin_x)) {
	    if (graduations[graduations.length - 1].x1 != 
		pointer.getCenterX()) {
		currentGraduation = graduations[graduations.length - 1];
		return graduations[graduations.length - 1].x1;
	    }
	    else return -1;
	} else  if (x < (marginx + rect_margin_x)) {
	    if (graduations[0].x1 != pointer.getCenterX()) {
		currentGraduation = graduations[0];
		return graduations[0].x1;      
	    }
	    else return -1;
	} else {
	    // find the nearest graduation.
	    int dx = -1;
	    int mindx = getGraduationLength()+50;
	    int i = 0;
	    while (i < graduations.length) {
		dx = (graduations[i].dx(x));
		if (dx < mindx) {
		    mindx = dx;
		    i++;
		} else break;
	    }
	    if (graduations[i-1].x1 != pointer.getCenterX()) {
		currentGraduation = graduations[i-1];
		return graduations[i-1].x1;
	    }
	    return -1; //FIXME
	}
    }

    /**
     * Get the current value pointed by the slider.
     * @return the value.
     */
    public double getValue() {
	if (currentGraduation != null) {
	    return currentGraduation.value;
	}
	else return min;
    }

    protected void updatePointerPosition(double value) {
	if (value <= graduations[0].value){
	    pointer.setLocation(graduations[0].x1, pposition.y);
	    return;
	}
	int maxidx = graduations.length -1;
	if (value == graduations[maxidx].value) {
	    pointer.setLocation(graduations[maxidx].x1,	pposition.y);
	    return;
	}
	if (value > graduations[graduations.length -1].value)
	    setMax(value + 10*step);
	for (int i = 1; i < graduations.length -2; i++) {
	    if (value - graduations[i].value < step) {
		pointer.setLocation(graduations[i].x1, pposition.y);
		return ;
	    }
	}
    }

    /**
     * Set the value pointed by the slider.
     * if the value is too high, resize the slider.
     * @param The value to point.
     */
    public void setValue(double value) {
	updatePointerPosition(value);
	updateCurrentGraduation();
	if (getGraphics() != null)
	    paint(getGraphics());
    }

    public void setValue(long value) {
	setValue((double)value);
    }

    protected void movePointerTo(int x, int y) {
	int gx = getGoodX(x);
	if (gx != -1) {
	    pointer.setLocation(gx,pposition.y);
	    if (getGraphics() != null)
		paint(getGraphics());
	}
    }

    protected void movePointerTo(Point p) {
	int gx = getGoodX(p.x);
	if (gx != -1) {
	    pointer.setLocation(gx,pposition.y);
	    if (getGraphics() != null)
		paint(getGraphics());
	}
    }

    /**
     * paint the slider.
     */
    public void paint (Graphics g) {
	Dimension d = getSize();
	updateSize(d);
	Shape s = g.getClip();
	Image dbi    = null;
	Graphics dbg = null;
	dbi = ImageCache.getImage(this, d.width, d.height);
	dbg = dbi.getGraphics();
	dbg.setClip(s);
	dbg.setColor(color);
	dbg.clearRect(0,0, d.width, d.height);
	dbg.fillRect(marginx,
		     marginy,
		     width-(2*marginx),
		     height-(2*marginy));
	if (border) {
	    dbg.setColor(color.darker());
	    dbg.drawRect(marginx,
			 marginy,
			 width-(2*marginx),
			 height-(2*marginy));
	}
	dbg.setColor(Color.white);
	dbg.fill3DRect(marginx + rect_margin_x,
		       marginy + rect_margin_y - guideHeight/2,
		       getGraduationLength(),
		       guideHeight,
		       false);
	paintGraduation(dbg);
	dbg.setColor(color.darker());
	pointer.paint(dbg);
	g.drawImage(dbi, 0, 0, this);
    }

    protected int getGraduationLength() {
	// use to recover arround error.
	int length = width - 2 * (marginx + rect_margin_x);
	int nbpart = (int)((max - min) / step);
	int pixelstep = length / nbpart;
	return pixelstep * nbpart;
    }

    protected void updateGraduationPosition() {
	int length = width - 2 * (marginx + rect_margin_x);
	int nbpart = (int) ((max - min) / step);
	int pixelstep = length / nbpart;
	int y1 = marginy  + 5 * rect_margin_y / 3;
	int y2 = y1+ graduationHeight;
	int minpixel = marginx + rect_margin_x;
	int maxpixel = length + minpixel;
	int gradx = minpixel;
	graduations = new Graduation[nbpart+1];
	int i=0;
	while (i < graduations.length){
	    if (manageLong) {
		graduations[i] = 
		    new Graduation(gradx,y1,gradx,y2,(long)(min+i*step));
	    } else {
		graduations[i] = new Graduation(gradx,y1,gradx,y2,min+i*step);
	    }
	    gradx += pixelstep;
	    i++;
	} 
    }

    protected void paintGraduation(Graphics g) {
	g.setColor(Color.black);
	Font font = new Font(g.getFont().getName(),
			     g.getFont().getStyle(),
			     g.getFont().getSize() - 1);
	g.setFont(font);
	for (int i=0 ; i < graduations.length; i++)
	    graduations[i].draw(g, (i == 0) || (i == graduations.length-1));
	g.setColor(Color.white);
	currentGraduation.showValue(g);
    }

    /**
     * update the slider.
     */
    public void update (Graphics g){
	paint(g);
    }

    protected void setDefaultSize(double min, double max, double step) {
	int length = minwidth - 2 * (marginx + rect_margin_x);
	int nbpart = (int)((max - min) / step);
	int pixelstep = length / nbpart;
 	if (pixelstep < minpixelstep) {
 	    minwidth = (minpixelstep * nbpart) + 2 * (marginx + rect_margin_x);
 	    defaultWidth = minwidth;
 	}
	updateSize(defaultWidth, defaultHeight);
	pposition = new Point(pposition.x,  marginy + rect_margin_y);
	pointer.setLocation(pposition);
	currentGraduation = graduations[0];
    }

    /**
     * Resizes this component so that it has width "width" and height "height".
     * @param d - The dimension specifying the new size of this slider. 
     */
    public void updateSize(Dimension d) {
	updateSize(d.width, d.height);
    }

    private Dimension oldsize = new Dimension(0,0);

    /**
     * Resizes this component so that it has width "width" 
     * and height "height". 
     * @param width - The new width of this slider
     * @param height - The new height of this slider
     */
    public void updateSize (int width, int height) {
	if ((oldsize.width != width) || (oldsize.height != height)) {
	    double value = getValue();
	    if (height < minheight)
		height = minheight;
	    if (width < minwidth)
		width = minwidth;
	    super.setSize(width, height);
	    oldsize = getSize();
	    this.width    = width;
	    this.height   = height;
	    rect_margin_y = (height - (2* marginy)) / 3;
	    updateGraduationPosition();
	    pposition.y = marginy + rect_margin_y;
	    updatePointerPosition(value);
	    updateCurrentGraduation();
	}
    }

    /**
     * Gets the mininimum size of this component.
     * @return A dimension object indicating this slider's minimum size.
     */
    public Dimension getMinimumSize() {
	return new Dimension(minwidth,minheight);
    }

    /**
     * Set the slider's color.
     * @param color - the slider's color.
     */
    public void setColor(Color color) {
	this.color = color;
    }

    /**
     * Set the minimum bound of the slider.
     * Use initialize or SetBounds if you want to set more than one value.
     * @param min - the min bound
     * @see #setMax 
     * @see #setBounds 
     * @see #setStep 
     * @see #initialize
     */
    public void setMin(double min) {
	this.min = min;
	update();
    }

    /**
     * Set the maximum bound of the slider.
     * Use initialize or SetBounds if you want to set more than one value.
     * @param max - the max bound
     * @see #setMin 
     * @see #setBounds 
     * @see #setStep 
     * @see #initialize
     */
    public void setMax(double max) {
	this.max = max;
	update();
    }

    /**
     * Set the bounds of the slider.
     * @param min - the min bound
     * @param max - the max bound
     * @see #setMin 
     * @see #setMax
     * @see #setStep 
     * @see #initialize
     */
    public void setBounds(double min, double max) {
	this.min = min;
	this.max = max;
	update();    
    }

    /**
     * Set the step of the slider.
     * Use initialize or SetBounds if you want to set more than one value.
     * @param step - the step between two position
     * @see #setMin 
     * @see #setMax
     * @see #setBounds 
     * @see #initialize
     */
    public void setStep(double step) {
	this.step = step;
	update();
    }

    /**
     * Initialize the slider's bounds and Step
     * @param min - the min bound
     * @param max - the max bound
     * @param step - the step between two position
     * @see #setMin 
     * @see #setMax
     * @see #setBounds 
     * @see #initialize
     */
    public void initialize(double min, double max, double step) {
	this.manageLong = false;
	this.min = min;
	this.max = max;
	this.step = step;
	update();
    }

    public void initialize(double min, double max, 
			   double step, boolean border) 
    {
	initialize(min,max,step);
	this.border = border;
    }

    public void initialize(long min, long max, long step) {
	initialize((double)min, (double) max, (double) step);
	this.manageLong = true;
	update();
    }

    public void initialize(long min, long max, long step, boolean border) {
	initialize(min,max,step);
	this.border = border;
    }

    protected void update() {
	setDefaultSize(min,max,step);
	updateGraduationPosition();
	if (graduations != null)
	    currentGraduation = graduations[0];
	setVisible(true);
	Component parent = getParent();
	if (parent != null)
	    parent.validate();
    }

    /**
     * Constructs an invisible Slider.
     * use initialize to define it.
     * @param minPixelStep - the min step (in pixels) between two positions.
     * @param border - if true draw a border arround the slider.
     */
    public Slider(int minPixelStep, boolean border) {
	this();
	this.minpixelstep = minPixelStep;
	this.border = border;
    }

    /**
     * Constructs an invisible Slider.
     * use initialize to define it.
     */
    public Slider() {
	pposition = new Point(marginx + rect_margin_x, 
			      marginy + rect_margin_y);
	pointer = new Pointer(pointerWidth,pointerHeight, pposition);
	PointerClickListener clickListener = 
	    new PointerClickListener(this);
	PointerMotionListener motionListener = 
	    new PointerMotionListener(this);
	addMouseListener( clickListener );
	addMouseMotionListener( motionListener );
	setVisible(false);
    }

    // TEST
    public static void main(String argv[]) {
	Frame f = new Frame ("Slider");
	f.setLayout( new GridLayout(6,1));
	Slider slider = null;

	slider = new Slider ();
	slider.initialize(0.0, 1.0, 0.1);
	slider.setColor(Color.lightGray);
	f.add (slider);

	slider = new Slider ();
	slider.initialize(0.0, 0.1, 0.01);
	slider.setColor(Color.lightGray);
	f.add (slider);

	slider = new Slider ();
	slider.initialize(0.0, 0.01, 0.0005);
	slider.setColor(Color.lightGray);
	f.add (slider);

	slider = new Slider ();
	slider.initialize(0, 100, 10);
	slider.setColor(Color.lightGray);
	f.add (slider);

	slider = new Slider ();
	slider.initialize(0, 30000, 5000);
	slider.setColor(Color.lightGray);
	f.add (slider);

	slider = new Slider ();
	slider.initialize(0, 30000, 1000);
	slider.setColor(Color.lightGray);
	f.add (slider);

	if (argv.length > 1) {
	    int width = Integer.parseInt(argv[0]);
	    int height = Integer.parseInt(argv[1]);
	    slider.setSize(width, height);
	}
	f.pack();
	f.show ();
    }

}
