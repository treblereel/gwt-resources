/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.gwtproject.resources.rg.css;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.regex.Matcher;
import javax.lang.model.element.Element;
import org.gwtproject.resources.rg.css.ast.*;

/** Collect all CSS class names in a stylesheet. */
public class ExtractClassNamesVisitor extends CssVisitor {
  /** Extract all CSS class names in the provided stylesheet. */
  public static Set<String> exec(CssStylesheet sheet) {
    return exec(sheet, new Element[0]);
  }

  /**
   * Extract all CSS class names in the provided stylesheet, modulo those imported from another
   * context.
   */
  public static Set<String> exec(CssStylesheet sheet, Element... imports) {
    throw new NullPointerException("");
    /*
    SortedSet<String> ignoredPrefixes = new TreeSet<String>();
    for (Element clazz : imports) {
      String prefix = CssResourceGenerator.getImportPrefix(clazz);
      ignoredPrefixes.add(prefix);
    }

    ExtractClassNamesVisitor v = new ExtractClassNamesVisitor(ignoredPrefixes);
    v.accept(sheet);
    return v.found;*/
  }

  private final Set<String> found = new HashSet<String>();
  private final SortedSet<String> ignoredPrefixes;

  /** Package-protected for testing. */
  ExtractClassNamesVisitor(SortedSet<String> ignoredPrefixes) {
    this.ignoredPrefixes = ignoredPrefixes;
  }

  @Override
  public void endVisit(CssExternalSelectors x, Context ctx) {
    addAll(x.getClasses());
  }

  @Override
  public void endVisit(CssSelector x, Context ctx) {
    Matcher m = CssSelector.CLASS_SELECTOR_PATTERN.matcher(x.getSelector());
    while (m.find()) {
      add(m.group(1));
    }
  }

  /** Package-protected for testing. */
  Set<String> getFoundClasses() {
    return found;
  }

  private void add(String selector) {
    SortedSet<String> headSet = ignoredPrefixes.headSet(selector);
    if (headSet.isEmpty() || !selector.startsWith(headSet.last())) {
      found.add(selector);
    }
  }

  private void addAll(Iterable<String> selectors) {
    for (String selector : selectors) {
      add(selector);
    }
  }
}
