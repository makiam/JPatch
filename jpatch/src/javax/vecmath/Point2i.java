/*
 * $RCSfile: Point2i.java,v $
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision: 1.4 $
 * $Date: 2007/02/09 17:22:37 $
 * $State: Exp $
 */

package javax.vecmath;

import java.lang.Math;

/**
 * A 2-element point represented by signed integer x,y
 * coordinates.
 *
 * @since vecmath 1.4
 */
public class Point2i extends Tuple2i implements java.io.Serializable {

    static final long serialVersionUID = 9208072376494084954L;

    /**
     * Constructs and initializes a Point2i from the specified
     * x and y coordinates.
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public Point2i(int x, int y) {
	super(x, y);
    }


    /**
     * Constructs and initializes a Point2i from the array of length 2.
     * @param t the array of length 2 containing x and y in order.
     */
    public Point2i(int[] t) {
	super(t);
    }


    /**
     * Constructs and initializes a Point2i from the specified Tuple2i.
     * @param t1 the Tuple2i containing the initialization x and y
     * data.
     */
    public Point2i(Tuple2i t1) {
	super(t1);
    }


    /**
     * Constructs and initializes a Point2i to (0,0).
     */
    public Point2i() {
	super();
    }

}
