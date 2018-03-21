// Utilities.java
// $Id: Utilities.java,v 1.1 2010/06/15 12:20:37 smhuang Exp $
// (c) COPYRIGHT MIT and INRIA, 1998.
// Please first read the full copyright statement in file COPYRIGHT.html

package org.w3c.tools.widgets;

import java.awt.Font;
import java.awt.Insets;
import java.awt.Dimension;

/**
 * @version $Revision: 1.1 $
 * @author  Benoît Mahé (bmahe@w3.org)
 */
public class Utilities {

    public final static Insets insets0;
    public final static Insets insets2;
    public final static Insets insets4;
    public final static Insets insets5;
    public final static Insets insets10;
    public final static Insets insets20;

    public final static Font smallFont;
    public final static Font defaultFont;
    public final static Font boldFont;
    public final static Font mediumFont;
    public final static Font mediumBoldFont;
    public final static Font bigFont;
    public final static Font bigBoldFont;
    public final static Font reallyBigFont;
    public final static Font reallyBigBoldFont;

    public final static Dimension dim0_0;
    public final static Dimension dim1_1;
    public final static Dimension dim2_2;
    public final static Dimension dim3_3;
    public final static Dimension dim4_4;
    public final static Dimension dim5_5;
    public final static Dimension dim10_10;
    public final static Dimension dim20_20;

    static {
	insets0  = new Insets(0, 0, 0, 0);
	insets2  = new Insets(2, 2, 2, 2);
	insets4  = new Insets(4, 4, 4, 4);
	insets5  = new Insets(5, 5, 5, 5);
	insets10 = new Insets(10, 10, 10, 10);
	insets20 = new Insets(20, 20, 20, 20);

	smallFont         = new Font("Dialog", Font.PLAIN, 10);
	defaultFont       = new Font("Dialog", Font.PLAIN, 12);
	boldFont          = new Font("Dialog", Font.BOLD, 12);
	mediumFont        = new Font("Dialog", Font.PLAIN, 15);
	mediumBoldFont    = new Font("Dialog", Font.BOLD, 15);
	bigFont           = new Font("Dialog", Font.PLAIN, 18);
	bigBoldFont       = new Font("Dialog", Font.BOLD, 18);
	reallyBigFont     = new Font("Dialog", Font.PLAIN, 24);
	reallyBigBoldFont = new Font("Dialog", Font.BOLD, 24);

	dim0_0   = new Dimension(0, 0);
	dim1_1   = new Dimension(1, 1);
	dim2_2   = new Dimension(2, 2);
	dim3_3   = new Dimension(3, 3);
	dim4_4   = new Dimension(4, 4);
	dim5_5   = new Dimension(5, 5);
	dim10_10 = new Dimension(10, 10);
	dim20_20 = new Dimension(20, 20);
    }

}
