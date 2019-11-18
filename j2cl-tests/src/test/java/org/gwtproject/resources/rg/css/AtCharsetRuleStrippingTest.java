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

import java.util.List;
import junit.framework.TestCase;
import org.gwtproject.resources.ext.TreeLogger;
import org.gwtproject.resources.ext.UnableToCompleteException;
import org.gwtproject.resources.rg.css.ast.CssNode;
import org.gwtproject.resources.rg.css.ast.CssStylesheet;
import org.gwtproject.resources.rg.util.DefaultTextOutput;
import org.gwtproject.resources.rg.util.TextOutput;

/** Tests how CssResource handles stylesheets with unknown at-rules. */
public class AtCharsetRuleStrippingTest extends TestCase {
  private static final String SIMPLE = "@simple;";

  public void test() throws UnableToCompleteException {
    CssStylesheet sheet =
        GenerateCssAst.exec(
            TreeLogger.NULL,
            getClass()
                .getClassLoader()
                .getResource("org/gwtproject/resources/rg/css/atCharsetRule.css"));

    List<CssNode> nodes = sheet.getNodes();
    assertEquals(1, nodes.size());

    TextOutput out = new DefaultTextOutput(true);
    CssGenerationVisitor v = new CssGenerationVisitor(out);
    v.accept(sheet);

    assertEquals(SIMPLE, out.toString());
  }
}
