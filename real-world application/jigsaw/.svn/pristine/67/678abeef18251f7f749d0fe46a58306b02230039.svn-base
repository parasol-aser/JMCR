// PropertyFeeder.java
// $Id: PropertyFeeder.java,v 1.1 2010/06/15 12:22:46 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1997.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadm.editors ;

import org.w3c.jigadm.RemoteResourceWrapper;

import java.util.Hashtable;
import java.util.Properties;
import java.util.StringTokenizer;

public class PropertyFeeder implements EditorFeeder {

    public static final String FEEDER_DATA_P = "feeder.data";

    String[] s = null;

    public String[] getDefaultItems() {
	return s;
    }

    protected String[] getStringArray(Properties p, String name) {
	String v = (String) p.get(name);
	if ( v == null )
	    return new String[0];
	// Parse the property value:
	StringTokenizer st    = new StringTokenizer(v, "|");
	int             len   = st.countTokens();
	String          ret[] = new String[len];
	for (int i = 0 ; i < ret.length ; i++) {
	    ret[i] = st.nextToken();
	}
	return ret;
    }

    public void initialize (RemoteResourceWrapper rrw, Properties p) {
	s = getStringArray(p, FEEDER_DATA_P);
    }
}
