// Form.java
// $Id: Form.java,v 1.1 2010/06/15 12:27:22 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.forms ;

public class Form {
    /**
     * The handler of the form.
     */
    protected FormHandlerInterface handler = null ;

    /**
     * Register a new form field.
     * @param name The name of the field to be defined.
     * @param title Title of the field to be defined.
     * @param field The field editor.
     */

    public void addField (String name, String title, FormField field) {
    }

    public Form(FormHandlerInterface handler) {
	this.handler = handler ;
    }

}
