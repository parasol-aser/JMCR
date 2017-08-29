// ServerEditorInterface.java
// $Id: ServerEditorInterface.java,v 1.1 2010/06/15 12:25:56 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.editors;

import org.w3c.jigadmin.RemoteResourceWrapper;

/**
 * Interface for server editors.
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public interface ServerEditorInterface extends EditorInterface {

    /**
     * Load or reload the server configuration.
     * @param server the new server wrapper
     */
    public void setServer(RemoteResourceWrapper server);

}
