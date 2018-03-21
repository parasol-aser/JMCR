// ServerEditorFactory.java
// $Id: ServerEditorFactory.java,v 1.2 2010/06/15 17:52:57 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.editors;

import java.util.Hashtable;
import java.util.Properties;

import org.w3c.jigadmin.RemoteResourceWrapper;
import org.w3c.jigadmin.PropertyManager;
import org.w3c.jigadmin.gui.ServerBrowser;

/**
 * The ServerEditor Factory
 * @version $Revision: 1.2 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class ServerEditorFactory {

    private static Hashtable editors = new Hashtable(5);

    /**
     * Get the identifier of the server.
     * @param name the server name
     * @param browser the browser of the server
     * @return a String
     */
    public static String getIdentifier(String name, ServerBrowser browser) {
	return name+(String.valueOf(browser.hashCode()));
    }

    /**
     * Get the ServerEditor of the server.
     * @param name the server name
     * @param browser the ServerBrowser
     * @param server the RemoteResourceWrapper of the server
     * @return The ServerEditor
     */
    public static 
	ServerEditorInterface getServerEditor(String name, 
					      ServerBrowser browser,
					      RemoteResourceWrapper server) 
    {
	PropertyManager pm = PropertyManager.getPropertyManager();
	String editorClass = pm.getEditorClass(server);

	if (editorClass == null)
	    return null;

	ServerEditorInterface editor = 
	    (ServerEditorInterface)editors.get(name+browser);

	if (editor == null) {
	    try {
		Class  c = Class.forName(editorClass);
		Object o = c.newInstance();
	    //Added by Jeff Huang
	    //TODO: FIXIT
		if (o instanceof ServerEditorInterface) {
		    editor = (ServerEditorInterface) o;
		    editor.initialize(name, 
				      server, 
				      pm.getEditorProperties(server));
		} else {
		    throw new RuntimeException(editorClass+" doesn't "+
				     "implements ServerEditorInterface.");
		}
	    } catch (Exception ex) {
		ex.printStackTrace();
		throw new RuntimeException("cannot create editor: "+
					   editorClass+" for \""+name);
	    }
	    editors.put(getIdentifier(name,browser), editor);
	} else {
	    editor.setServer(server);
	}
	return editor;
    }

    /**
     * Update the ServerEditor.
     * @param name the server name
     * @param browser the ServerBrowser
     * @param server the RemoteResourceWrapper of the server
     */
    public static void updateServerEditor(String name,
					  ServerBrowser browser,
					  RemoteResourceWrapper server) 
    {
	ServerEditorInterface editor = 
	    (ServerEditorInterface)editors.get(getIdentifier(name,browser));
	if (editor != null)
	    editor.setServer(server);
    }

}
