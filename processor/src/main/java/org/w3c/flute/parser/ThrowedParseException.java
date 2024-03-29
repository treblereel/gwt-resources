/*
 * (c) COPYRIGHT 1999 World Wide Web Consortium
 * (Massachusetts Institute of Technology, Institut National de Recherche
 *  en Informatique et en Automatique, Keio University).
 * All Rights Reserved. http://www.w3.org/Consortium/Legal/
 *
 * $Id: ThrowedParseException.java,v 1.1 1999/06/09 15:21:33 plehegar Exp $
 */
package org.w3c.flute.parser;

/**
 * @version $Revision: 1.1 $
 * @author Philippe Le Hegaret
 */
class ThrowedParseException extends RuntimeException {
  ParseException e;

  /** Creates a new ThrowedParseException */
  ThrowedParseException(ParseException e) {
    this.e = e;
  }
}
