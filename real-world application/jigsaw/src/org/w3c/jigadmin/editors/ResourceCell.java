// ResourceCell.java
// $Id: ResourceCell.java,v 1.1 2010/06/15 12:25:56 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.jigadmin.editors; 

/**
 * A wrapper of classname, used in dnd feature.
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class ResourceCell {

    static final public String INDEXER_S   = "indexer";
    static final public String CONTAINER_S = "container";
    static final public String RESOURCE_S  = "resource";
    static final public String FRAME_S     = "frame";
    static final public String FILTER_S    = "filter";
    static final public String META_S      = "meta";

    private String name = null;
    private String spec = null;

    /**
     * Return true if the Resource wrapped by this cell is an Indexer.
     * @return a boolean
     */
    public boolean isIndexer() {
	return spec.equals(INDEXER_S);
    }

    /**
     * Return true if the Resource wrapped by this cell is a container.
     * @return a boolean
     */
    public boolean isContainer() {
	return spec.equals(CONTAINER_S);
    }

    /**
     * Return true if the Resource wrapped by this cell is a resource.
     * @return a boolean
     */
    public boolean isResource() {
	return spec.equals(RESOURCE_S);
    }

    /**
     * Return true if the Resource wrapped by this cell is a frame.
     * @return a boolean
     */
    public boolean isFrame() {
	return spec.equals(FRAME_S);
    }

    /**
     * Return true if the Resource wrapped by this cell is a filter.
     * @return a boolean
     */
    public boolean isFilter() {
	return spec.equals(FILTER_S);
    }

    /**
     * Return true if the Resource wrapped by this cell is a MetaDataFrame.
     * @return a boolean
     */
    public boolean isMetaDataFrame() {
	return spec.equals(META_S);
    }

    /**
     * Constructor.
     * @param name the resource classname
     * @param spec INDEXER_S, CONTAINER_S, RESOURCE_S, FRAME_S or FILTER_S
     */
    public ResourceCell(String name, String spec) {
	this.name = name;
	this.spec = spec;
    }

    /**
     * Return the classname
     * @return a String.
     */
    public String toString() {
	return name;
    }
}
