// FilterInterface.java
// $Id: FilterInterface.java,v 1.1 2010/06/15 12:20:21 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.resources ;

import java.io.OutputStream;

public interface FilterInterface {

  /**
   * The lookup stage filtering.
   * This filter is invoked during lookup, it can cancel the lookup operation
   * in progress, by either throwing an exception (in case of error), or
   * by returning <strong>true</strong>, in which case, the filter has
   * perform the whole lookup operation itself.
   * @param ls The current lookup state, describing the state of the lookup
   * operation in progress.
   * @param lr The current lookup result, describing the already computed
   * part of the lookup operation.
   * @return A boolean, <strong>true</strong> if lookup has been completed
   * by the filter, and nothing else need to be done to complete it. 
   * Otherwise lookup should continue normally, and the filter returns
   * <strong>false</strong>.
   * @exception ProtocolException If some error occurs, and the whole 
   * lookup operation cannot be continued normally.
   */

  public boolean lookup(LookupState ls, LookupResult lr)
    throws ProtocolException;

  /**
   * The filter's ingoing method is called before any request processing is
   * done by the target resource.
   * <p>This method can (if able) compute the whole request's reply, and 
   * return it. If processing should continue normally, then the filter
   * must return <strong>null</strong>.
   * <p>If a filter's <code>ingoingFilter</code> method gets called, 
   * then it is guaranteed that either its <code>outgoingFilter</code>
   * method or its <code>exceptionFilter</code> method gets called.
   * @param request The request being processed.
   * @param filters The whole array of filters to be applied before
   * actually continuing the process.
   * @param fidx The index in the above array of the filter being called.
   * @return A Reply instance, if the filter knows how to compute it, or
   * <strong>null</strong> if the request processing should continue
   * normally.
   * @exception ProtocolException If the filter fails.
   */

  public ReplyInterface ingoingFilter(RequestInterface request,
				      FilterInterface filters[],
				      int fidx)
    throws ProtocolException;

  /**
   * The filter's outgoing method is called once the target resource has
   * computed a reply.
   * <p>This method can return a Reply instance, in which case, the
   * processing should be aborted, and  the returned reply should be emited
   * back to the client. Otherwise, if the filter returns <strong>null
   * </strong> the processing continues normally.
   * @param request The request being processed.
   * @param reply The original reply, as emited by the target resource, 
   * and which has already been processed by the first filters.
   * @param filters The whole array of filters to be applied before
   * actually continuing the process.
   * @param fidx The index in the above array of the filter being called.
   * @exception ProtocolException If the filter fails.
   */

  public ReplyInterface outgoingFilter(RequestInterface request,
				       ReplyInterface reply,
				       FilterInterface filters[],
				       int fidx)
    throws ProtocolException;

  /**
   * @param request The request being processed.
   * @param ex The exception that occured during processing the request. 
   * and which has already been processed by the first filters.
   * @param filters The whole array of filters to be applied before
   * actually continuing the process.
   * @param fidx The index in the above array of the filter being called.
   */

  public ReplyInterface exceptionFilter(RequestInterface request,
					ProtocolException ex,
					FilterInterface filters[],
					int fidx);

  public OutputStream outputFilter(RequestInterface request,
                                     ReplyInterface reply,
                                     OutputStream output);

}
