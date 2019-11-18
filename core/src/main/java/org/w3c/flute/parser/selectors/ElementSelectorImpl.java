/*
 * Copyright (c) 2000 World Wide Web Consortium,
 * (Massachusetts Institute of Technology, Institut National de
 * Recherche en Informatique et en Automatique, Keio University). All
 * Rights Reserved. This program is distributed under the W3C's Software
 * Intellectual Property License. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE.
 * See W3C License http://www.w3.org/Consortium/Legal/ for more details.
 *
 * $Id: ElementSelectorImpl.java,v 1.1 2000/04/19 21:57:47 plehegar Exp $
 */
package org.w3c.flute.parser.selectors;

import org.w3c.css.sac.ElementSelector;
import org.w3c.css.sac.Selector;

/**
 * @version $Revision: 1.1 $
 * @author Philippe Le Hegaret
 */
public class ElementSelectorImpl implements ElementSelector {

  String localName;

  /** Creates a new ElementSelectorImpl */
  public ElementSelectorImpl(String localName) {
    this.localName = localName;
  }

  /** An integer indicating the type of <code>Selector</code> */
  public short getSelectorType() {
    return Selector.SAC_ELEMENT_NODE_SELECTOR;
  }

  /**
   * Returns the <a href="http://www.w3.org/TR/REC-xml-names/#dt-NSName">namespace URI</a> of this
   * element selector.
   *
   * <p><code>NULL</code> if this element selector can match any namespace.
   */
  public String getNamespaceURI() {
    return null;
  }

  /**
   * Returns the <a href="http://www.w3.org/TR/REC-xml-names/#NT-LocalPart">local part</a> of the <a
   * href="http://www.w3.org/TR/REC-xml-names/#ns-qualnames">qualified name</a> of this element.
   *
   * <p><code>NULL</code> if this element selector can match any element.
   * </ul>
   */
  public String getLocalName() {
    return localName;
  }
}
