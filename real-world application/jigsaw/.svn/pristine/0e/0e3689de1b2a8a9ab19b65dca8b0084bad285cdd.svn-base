// SpaceEntryImpl.java
// $Id: SpaceEntryImpl.java,v 1.1 2010/06/15 12:20:14 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources ;

public class SpaceEntryImpl implements SpaceEntry {

    Integer key = null;

    /**
     * Get the Key. This key must be unique and unchanged
     * during the all life.
     * @return an int.
     */
    public Integer getEntryKey() {
	return key;
    }

    public String toString() {
	return String.valueOf(key);
    }

    public SpaceEntryImpl(ContainerResource cont) {
	this.key = cont.getKey();
    }

}
