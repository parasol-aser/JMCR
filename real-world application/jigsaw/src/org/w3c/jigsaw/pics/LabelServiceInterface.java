// LabelServiceInterface.java
// $Id: LabelServiceInterface.java,v 1.1 2010/06/15 12:25:28 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.pics ;

import java.net.URL;

public interface LabelServiceInterface {

    /**
     * Get the specific labels for a given URL.
     * @param url The URL whose label is to be fetched.
     * @return An object conorminig to the LabelInterface, or 
     *    <strong>null</strong> if none was found.
     */

    public LabelInterface getSpecificLabel (URL u) ;

    /**
     * Get the most specific generic label for the given URL.
     * @param url The URL whose label is to be retreived.
     * @return An object conforming to the LabelInterface, or 
     *    <strong>null</strong> if none was found.
     */

    public LabelInterface getGenericLabel (URL u) ;

    /**
     * Get the tree labels for the given URL.
     * @param url The URL whose tree labels are to be retreieved.
     * @return An array of objects conforming to the LabelInterface, or
     *    <strong>null</strong> if none was found.
     */

    public LabelInterface[] getTreeLabels (URL u) ;

    /**
     * Get the generic tree labels for the given URL.
     * @param url The URL whose labels is to be retreieved.
     * @return An array of object conforming to the LabelInterface, or 
     *    <strong>null<?strong> if none was found.
     */

    public LabelInterface[] getGenericTreeLabels (URL u) ;

    
    /**
     * Dump this service description into the provided buffer.
     * This method is called by the protocol handler, whenever it needs to send
     * back the service description. 
     * @param buffer The buffer in whichi to dump the service description.
     * @param format The format in which this service is to be dumped (which
     *     can be any of ... FIXME)
     */

    public void dump (StringBuffer buffer, int format) ;

}
