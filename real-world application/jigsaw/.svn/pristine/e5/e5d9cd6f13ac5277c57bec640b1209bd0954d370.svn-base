// HTTPException.java
// $Id: HTTPException.java,v 1.1 2010/06/15 12:21:57 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.jigsaw.http ;

import org.w3c.tools.resources.ProtocolException;
import org.w3c.tools.resources.ReplyInterface;

/**
 * All entities should throw an HTTPException when encoutering some problems.
 * This kind of exception is the one that gets normally caught by clients,
 * and result in sending back HTTP error messages to the client.
 */

public class HTTPException extends ProtocolException {

  public HTTPException (String msg) {
    super (msg) ;
  }

  public HTTPException (String msg, Reply error) {
    super (msg, error) ;
  }

  public HTTPException (Reply error) {
    super (error) ;
  }

  public HTTPException(ProtocolException ex) {
    super(ex.getMessage(), ex.getReply());
  }
}

	
