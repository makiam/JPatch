/*
 * $RCSfile: MismatchedSizeException.java,v $
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

/**
 * Indicates that an operation cannot be completed properly because
 * of a mismatch in the sizes of object attributes.
 */
public class MismatchedSizeException extends RuntimeException{


/**
 * Create the exception object with default values.
 */
  public MismatchedSizeException(){
  }

/**
 * Create the exception object that outputs a message.
 * @param str the message string to be output.
 */
  public MismatchedSizeException(String str){

    super(str);
  }

}

