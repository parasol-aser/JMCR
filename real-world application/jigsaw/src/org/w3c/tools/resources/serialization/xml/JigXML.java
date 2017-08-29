// JXML.java
// $Id: JigXML.java,v 1.1 2010/06/15 12:28:58 smhuang Exp $
// (c) COPYRIGHT MIT, INRIA and Keio, 1999.
// Please first read the full copyright statement in file COPYRIGHT.html
package org.w3c.tools.resources.serialization.xml;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public interface JigXML {

    public final String version = "1.0";
    public static final String iversion = version.intern();

    public final String dtd = "http://jigsaw.w3.org/JigXML/JigXML.dtd";
    public static final String idtd = dtd.intern();

    public final String ns = "http://jigsaw.w3.org/JigXML/JigXML1.0";
    public static final String ins = ns.intern();

    public final String JXML_TAG = "jigxml";
    public static final String iJXML_TAG = JXML_TAG.intern();
    
    public final String RESOURCE_TAG = "resource";
    public static final String iRESOURCE_TAG = RESOURCE_TAG.intern();

    public final String DESCR_TAG = "description";
    public static final String iDESCR_TAG = DESCR_TAG.intern();

    public final String CHILDREN_TAG = "children";
    public static final String iCHILDREN_TAG = CHILDREN_TAG.intern();

    public final String CHILD_TAG = "child";
    public static final String iCHILD_TAG = CHILD_TAG.intern();

    public final String ATTRIBUTE_TAG = "attribute";
    public static final String iATTRIBUTE_TAG = ATTRIBUTE_TAG.intern();

    public final String ARRAY_TAG = "array";
    public static final String iARRAY_TAG = ARRAY_TAG.intern();

    public final String RESARRAY_TAG = "resourcearray";
    public static final String iRESARRAY_TAG = RESARRAY_TAG.intern();

    public final String INHERIT_TAG = "inherit";
    public static final String iINHERIT_TAG = INHERIT_TAG.intern();

    public final String IMPLEMENTS_TAG = "implements";
    public static final String iIMPLEMENTS_TAG = IMPLEMENTS_TAG.intern();

    public final String VALUE_TAG = "value";
    public static final String iVALUE_TAG = VALUE_TAG.intern();

    public final String LENGTH_ATTR = "length";
    public static final String iLENGTH_ATTR = LENGTH_ATTR.intern();

    public final String CLASS_ATTR = "class";
    public static final String iCLASS_ATTR = CLASS_ATTR.intern();

    public final String NAME_ATTR = "name";
    public static final String iNAME_ATTR = NAME_ATTR.intern();

    public final String FLAG_ATTR = "flag";
    public static final String iFLAG_ATTR = FLAG_ATTR.intern();

    public final String NULL = "@@NULL@@";
    public static final String iNULL = NULL.intern();
}
