// HttpWarning.java
// $Id: HttpWarning.java,v 1.1 2010/06/15 12:19:43 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.www.http;

public class HttpWarning extends BasicValue {
    /**
     * Warning status - Response is stale.
     */
    public static final int STALE                    = 110;
    /**
     * Warning status - Revalidation failed.
     */
    public static final int REVALIDATION_FAILED      = 111;
    /**
     * Warning status - Disconnected opertaion.
     */
    public static final int DISCONNECTED_OPERATION   = 112;
    /**
     * Warning status - Heuristic expiration.
     */
    public static final int HEURISTIC_EXPIRATION     = 113;
    /**
     * Warning status - Miscellaneous warning
     */
    public static final int MISCELLANEOUS            = 199;
    /**
     * Warning status - Transformation applied.
     */
    public static final int TRANSFORMATION_APPLIED   = 214;
    /**
     * Warning status - Miscellaneous warning
     */
    public static final int PERSISTENT_MISCELLANEOUS = 199;

    protected HttpWarningList list = null;

    protected int    status = -1;
    protected String agent  = null;
    protected String text   = null;

    /**
     * parse.
     * @exception HttpParserException if parsing failed.
     */
    protected void parse()
	throws HttpParserException
    {
	ParseState ps = new ParseState(roff, rlen);
	ParseState it = new ParseState();
	// Get the status code:
	if ( HttpParser.nextItem(raw, ps) < 0 )
	    error("Invalid warning, no status code.");
	it.ioff   = ps.start;
	it.bufend = ps.end;
	this.status = HttpParser.parseInt(raw, it);
	// Get the agent emiting the warning
	ps.prepare();
	if ( HttpParser.nextItem(raw, ps) < 0 )
	    error("Invalid warning, no agent field.");
	this.agent = new String(raw, 0, ps.start, ps.end-ps.start);
	// Get the quoted message
	ps.prepare();
	if ( HttpParser.nextItem(raw, ps) < 0 )
	    error("Invalid warning, no text message.");
	it.ioff   = ps.start;
	it.bufend = ps.end;
	HttpParser.unquote(raw, it);
	this.text = new String(raw, 0, it.start, it.end-it.start);
    }

    protected void updateByteValue() {
	HttpBuffer buf = new HttpBuffer() ;
	buf.appendInt(status);
	buf.append(' ');
	buf.append(agent);
	buf.append(' ');
	buf.appendQuoted(text);
	raw  = buf.getByteCopy();
	roff = 0;
	rlen = raw.length;
    }

    protected void invalidateByteValue() {
	super.invalidateByteValue();
	if ( list != null )
	    list.invalidateByteValue();
    }

    public Object getValue() {
	validate();
	return this;
    }

    /**
     * Get this warning status code.
     * @return An integer giving the warning status code.
     */

    public int getStatus() {
	validate();
	return status;
    }

    /**
     * Set this warning status code.
     * @param status The status code for this warning.
     */

    public void setStatus(int status) {
	if ( this.status != status )
	    invalidateByteValue();
	this.status = status;
    }

    /**
     * Get this warning agent.
     * @return A String encoding the agent that generated the warning.
     */

    public String getAgent() {
	validate();
	return agent;
    }

    /**
     * Set the agent that is generating the warning.
     * @param agent The String describing the agent emitting the warning.
     */

    public void setAgent(String agent) {
	if ((agent != null) && ! agent.equals(this.agent) )
	    invalidateByteValue();
	this.agent = agent;
    }

    /**
     * Get the warning text message.
     * @return A String encoding the text message.
     */

    public String getText() {
	validate();
	return text;
    }

    /**
     * Set the text warning message.
     * @param text The new text of the warning message.
     */

    public void setText(String text) {
	if ((text != null) && ! text.equals(this.text) )
	    invalidateByteValue();
	this.text = text;
    }

    HttpWarning(HttpWarningList list, byte raw[], int roff, int rlen) {
	this.list = list;
	this.raw  = raw;
	this.roff = roff;
	this.rlen = rlen;
	this.isValid = false;
    }

    HttpWarning(boolean isValid, int status, String agent, String text) {
	this.isValid = isValid;
	setStatus(status);
	setAgent(agent);
	setText(text);
    }

    public HttpWarning() {
	this.isValid = false;
    }
}


