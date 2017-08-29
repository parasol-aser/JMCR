// CountOutputStream.java
// $Id: CountOutputStream.java,v 1.1 2010/06/15 12:25:36 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1996.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.util;

import java.io.OutputStream;

/**
 * This class can be used to count number of bytes emitted to a stream.
 * The stream will actually throw the data away. It's main function is
 * to count the number of bytes emitted to a stream before actually emitting
 * the bytes (that's not really efficient, but works enough).
 */

public class CountOutputStream extends OutputStream {
    protected int count = 0;

    /**
     * Get the current number of bytes emitted to that stream.
     * @return The current count value.
     */

    public int getCount() {
	return count;
    }

    /**
     * Close that count stream.
     */

    public void close() {
	return;
    }

    /**
     * Flush that count stream.
     */

    public void flush() {
	return;
    }

    /**
     * Write an array of bytes to that stream.
     */

    public void write(byte b[]) {
	count += b.length;
    }

    /**
     * Write part of an array of bytes to that stream.
     */

    public void write(byte b[], int off, int len) {
	count += len;
    }

    /**
     * Write a single byte to that stream.
     */

    public void write(int b) {
	count++;
    }

    /**
     * Create a new instance of that class.
     */

    public CountOutputStream() {
	this.count = 0;
    }

}
