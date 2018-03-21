// LabelBureauInterface.java
// $Id: LabelBureauInterface.java,v 1.1 2010/06/15 12:25:29 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.pics ;

/**
 * Interface for the label bureau.
 * This interface that the PICS protocol handler relies on to get the labels
 * for any URL.
 */

public interface LabelBureauInterface {
    /**
     * Tag for the minimal label format.
     */
    public static final int FMT_MINIMAL = 1 ;
    /**
     * Tag for the short label format.
     */
    public static final int FMT_SHORT  = 2 ;
    /**
     * Tag for the full label format.
     */
    public static final int FMT_FULL   = 3 ;
    /**
     * Tag for the signed label format.
     */
    public static final int FMT_SIGNED = 4 ;

    /**
     * Get this bureau identifier.
     * A bureau should have a uniq String identifier, which is used by the PICS
     * filter to create it (through the LabelBureauFactory), dump it and 
     * restore it.
     */

    public String getIdentifier () ;

    /**
     * Get a label service handler, given its identifier.
     * A service identifier is expected to be its URL, as defined in the PICS
     * specification.
     * @param identifier The service URL identifier.
     * @return An object conforming to the LabelServiceInterface, or 
     *    <strong>null</strong> if none was found.
     */

    public LabelServiceInterface getLabelService (String identifier) ;

}


