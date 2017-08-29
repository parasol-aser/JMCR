// ImageCache.java
// $Id: ImageCache.java,v 1.1 2010/06/15 12:20:38 smhuang Exp $
// Author: Jean-Michel.Leon@sophia.inria.fr
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.widgets;

import java.awt.Component;
import java.awt.Image;

import java.util.Hashtable;

/**
 * A Basic Image Cache class.
 */
public class ImageCache {
    private static Hashtable images = new Hashtable();

   /**
    * Gets an Image of the requested size.
    *
    * Checks if an Image already exists in the cache for the current Thread and
    * if this image is large enough. Else, creates a new Image and store it in
    * the cache.
    */
    static public Image getImage(Component c, int w, int h) {
	Image img = (Image)images.get(Thread.currentThread());
	if((img == null) || (img.getWidth(c) < w) || (img.getHeight(c) < h)) {
	    img = c.createImage(w, h);
	    images.put(Thread.currentThread(), img);
	}
	return img;
    }
}
