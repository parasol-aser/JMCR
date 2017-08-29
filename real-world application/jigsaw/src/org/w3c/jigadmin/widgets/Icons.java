// Icons.java
// $Id: Icons.java,v 1.1 2010/06/15 12:28:33 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigadmin.widgets;

import javax.swing.ImageIcon;

import org.w3c.jigadmin.PropertyManager;

/**
 * Manages all JigAdmin icons.
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class Icons {

    public static ImageIcon saveIcon      = null;
    public static ImageIcon stopIcon      = null;
    public static ImageIcon helpIcon      = null;
    public static ImageIcon deleteIcon    = null;
    public static ImageIcon addIcon       = null;
    public static ImageIcon reindexIcon   = null;
    public static ImageIcon infoIcon      = null;
    public static ImageIcon dirIcon       = null;
    public static ImageIcon resIcon       = null;
    public static ImageIcon frameIcon     = null;
    public static ImageIcon filterIcon    = null;
    public static ImageIcon metaDataIcon  = null;
    public static ImageIcon serverIcon    = null;
    public static ImageIcon arrowUpIcon   = null;
    public static ImageIcon arrowDownIcon = null;
    public static ImageIcon leftIcon      = null;
    public static ImageIcon rightIcon     = null;
    public static ImageIcon editIcon      = null;
    public static ImageIcon closeIcon     = null;
    public static ImageIcon copyRIcon     = null;
    public static ImageIcon copyLIcon     = null;
    public static ImageIcon teamIcon      = null;
    public static ImageIcon w3chIcon      = null;
    public static ImageIcon w3cmIcon      = null;
    public static ImageIcon jigsawIcon    = null;

    static {
	PropertyManager pm = PropertyManager.getPropertyManager();

	saveIcon      = new ImageIcon(pm.getIconLocation("save"));
	stopIcon      = new ImageIcon(pm.getIconLocation("stop"));
	helpIcon      = new ImageIcon(pm.getIconLocation("help"));
	deleteIcon    = new ImageIcon(pm.getIconLocation("delete"));
	addIcon       = new ImageIcon(pm.getIconLocation("add"));
	reindexIcon   = new ImageIcon(pm.getIconLocation("reindex"));
	infoIcon      = new ImageIcon(pm.getIconLocation("reference"));
	dirIcon       = new ImageIcon(pm.getIconLocation("dir"));
	resIcon       = new ImageIcon(pm.getIconLocation("file"));
	frameIcon     = new ImageIcon(pm.getIconLocation("frame"));
	filterIcon    = new ImageIcon(pm.getIconLocation("filter"));
	metaDataIcon  = new ImageIcon(pm.getIconLocation("meta"));
	serverIcon    = new ImageIcon(pm.getIconLocation("server"));
	arrowUpIcon   = new ImageIcon(pm.getIconLocation("pup"));
        arrowDownIcon = new ImageIcon(pm.getIconLocation("pdown"));
	leftIcon      = new ImageIcon(pm.getIconLocation("shadowleft"));
	rightIcon     = new ImageIcon(pm.getIconLocation("shadowright"));
	editIcon      = new ImageIcon(pm.getIconLocation("edit"));
	closeIcon     = new ImageIcon(pm.getIconLocation("close"));
	copyLIcon     = new ImageIcon(pm.getIconLocation("copy2left"));
	copyRIcon     = new ImageIcon(pm.getIconLocation("copy2right"));
	teamIcon      = new ImageIcon(pm.getIconLocation("team"));
	w3chIcon      = new ImageIcon(pm.getIconLocation("w3c_home"));
	w3cmIcon      = new ImageIcon(pm.getIconLocation("w3c_main"));
	jigsawIcon    = new ImageIcon(pm.getIconLocation("jigsaw"));
    }

    private Icons() {
	//no instance needed
    }

}
