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
 * $Id: LangConditionImpl.java,v 1.1 2000/04/19 21:57:47 plehegar Exp $
 */
package org.w3c.flute.parser.selectors;

import org.w3c.css.sac.Condition;
import org.w3c.css.sac.LangCondition;

/**
 * @version $Revision: 1.1 $
 * @author Philippe Le Hegaret
 */
public class LangConditionImpl implements LangCondition {

  String lang;

  /** Creates a new LangConditionImpl */
  public LangConditionImpl(String lang) {
    this.lang = lang;
  }

  /** An integer indicating the type of <code>Condition</code>. */
  public short getConditionType() {
    return Condition.SAC_LANG_CONDITION;
  }

  /** Returns the language */
  public String getLang() {
    return lang;
  }
}
