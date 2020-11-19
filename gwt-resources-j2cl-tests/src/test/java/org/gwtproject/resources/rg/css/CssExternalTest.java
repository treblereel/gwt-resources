/*
 * Copyright 2010 Google Inc.
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

import java.util.ArrayList;
import java.util.Arrays;
import org.gwtproject.resources.ext.TreeLogger;
import org.gwtproject.resources.ext.UnableToCompleteException;
import org.gwtproject.resources.rg.CssTestCase;
import org.gwtproject.resources.rg.css.ast.CssStylesheet;

/** Tests {@link ExternalClassesCollector}. */
public class CssExternalTest extends CssTestCase {

  public void testExternal() throws UnableToCompleteException {
    CssStylesheet sheet =
        GenerateCssAst.exec(
            TreeLogger.NULL,
            getClass()
                .getClassLoader()
                .getResource("org/gwtproject/resources/rg/css/external.css"));
    assertNotNull(sheet);

    ExternalClassesCollector v = new ExternalClassesCollector();
    v.accept(sheet);

    assertEquals(
        Arrays.asList("a", "b", "c", "glob-a", "glob-b", "no*effect"),
        new ArrayList<String>(v.getClasses()));
  }

  /** Make sure the short-circuit logic for <code>{@literal @external} *</code> works correctly. */
  public void testExternalStar() throws UnableToCompleteException {
    CssStylesheet sheet =
        GenerateCssAst.exec(
            TreeLogger.NULL,
            getClass()
                .getClassLoader()
                .getResource("org/gwtproject/resources/rg/css/external_star.css"),
            getClass()
                .getClassLoader()
                .getResource("org/gwtproject/resources/rg/css/external.css"));
    assertNotNull(sheet);

    ExternalClassesCollector v = new ExternalClassesCollector();
    v.accept(sheet);

    assertEquals(
        Arrays.asList("a", "c", "d", "glob-a", "glob-b", "no-effect"),
        new ArrayList<String>(v.getClasses()));
  }
}
