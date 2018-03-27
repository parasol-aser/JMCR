/*
 * @(#)Enumeration.java	1.22 03/12/19
 *
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package instrumented.java15.util;

/**
 * An object that implements the Enumeration interface generates a
 * series of elements, one at a time. Successive calls to the
 * <code>nextElement</code> method return successive elements of the
 * series.
 * <p>
 * For example, to print all elements of a vector <i>v</i>:
 * <blockquote><pre>
 *     for (Enumeration e = v.elements() ; e.hasMoreElements() ;) {
 *         System.out.println(e.nextElement());<br>
 *     }
 * </pre></blockquote>
 * <p>
 * Methods are provided to enumerate through the elements of a
 * vector, the keys of a hashtable, and the values in a hashtable.
 * Enumerations are also used to specify the input streams to a
 * <code>SequenceInputStream</code>.
 * <p>
 * NOTE: The functionality of this interface is duplicated by the Iterator
 * interface.  In addition, Iterator adds an optional remove operation, and
 * has shorter method names.  New implementations should consider using
 * Iterator in preference to Enumeration.
 *
 * @see     benchmarks.instrumented.java15.util.Iterator
 * @see     java.io.SequenceInputStream
 * @see     benchmarks.instrumented.java15.util.Enumeration#nextElement()
 * @see     benchmarks.instrumented.java15.util.Hashtable
 * @see     benchmarks.instrumented.java15.util.Hashtable#elements()
 * @see     benchmarks.instrumented.java15.util.Hashtable#keys()
 * @see     benchmarks.instrumented.java15.util.Vector
 * @see     benchmarks.instrumented.java15.util.Vector#elements()
 *
 * @author  Lee Boynton
 * @version 1.22, 12/19/03
 * @since   JDK1.0
 */
public interface Enumeration<E> {
    /**
     * Tests if this enumeration contains more elements.
     *
     * @return  <code>true</code> if and only if this enumeration object
     *           contains at least one more element to provide;
     *          <code>false</code> otherwise.
     */
    boolean hasMoreElements();

    /**
     * Returns the next element of this enumeration if this enumeration
     * object has at least one more element to provide.
     *
     * @return     the next element of this enumeration.
     * @exception  NoSuchElementException  if no more elements exist.
     */
    E nextElement();
}
