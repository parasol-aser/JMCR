// FileField.java
// $Id: FileField.java,v 1.1 2010/06/15 12:27:21 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.forms ;

import java.io.File;

public class FileField extends StringField {

    /**
     * Get this field's value in its native type.
     * @return An instance of File, or <strong>null</strong>.
     */

    public Object getValue() {
	return new File(value) ;
    }

    /**
     * Get this field's value as a File instance.
     * @return An instance of FIle, or <strong>null</strong>.
     */

    public File getFileValue() {
	return new File(value) ;
    }

    /**
     * Set this field's value using the native type.
     * @param value The new File value for the field.
     * @param update Should we update the editor's view ?
     * @exception IllegalFieldValueException If the value isn't accepted.
     */

    public void setValue(Object object, boolean notify, boolean update) 
	throws IllegalFieldValueException
    {
	if ( ! (object instanceof File) )
	    throw new IllegalFieldValueException (object) ;
	setValue((File) object, notify, update) ;
    }

    /**
     * Set this field's value.
     * @param file The new File value for the field.
     * @param update Update the editor's view ?
     * @exception IllegalFieldValueException If the value isn't accepted.
     */

    public void setValue(File value, boolean notify, boolean update) 
	throws IllegalFieldValueException
    {
	super.setValue(value.getAbsolutePath(), notify, update) ;
    }
	
    public FileField(FormManager manager
		     , String name, String title
		     , File value) {
	super(manager, name, title, ((value != null) 
				     ? value.getAbsolutePath()
				     : null)) ;
    }

    public FileField(FormManager manager, String name, String title) {
	this(manager, name, title, null) ;
    }
		     
}
