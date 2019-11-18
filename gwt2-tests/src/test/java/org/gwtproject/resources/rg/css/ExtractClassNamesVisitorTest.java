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

/** A test for {@link ExtractClassNamesVisitor}. Inject APTContext to fix this test */
public class ExtractClassNamesVisitorTest {
  /*extends CssTestCase {

  public void test() throws UnableToCompleteException {
    ExtractClassNamesVisitor v = new ExtractClassNamesVisitor(
        new TreeSet<>());

    try {
      test(TreeLogger.NULL, "extractClassNames", false, v);

    }catch (Exception e){
      e.printStackTrace();
      throw new Error(e);
    }
    Set<String> expected = new TreeSet<String>(Arrays.asList("selector1",
        "selector2", "selector3", "external1", "external2", "external3",
        "prefixed-selector", "selector_with_underscores",
        "-selector-with-hyphen"));
    Set<String> actual = new TreeSet<String>(v.getFoundClasses());

    assertEquals(expected, actual);
  }

  public void testImportedClasses() throws UnableToCompleteException {
    ExtractClassNamesVisitor v = new ExtractClassNamesVisitor(
        new TreeSet<>(Arrays.asList("blah-selector-", "prefixed-",
            "postfixed-")));

    test(TreeLogger.NULL,"extractClassNames", false, v);

    Set<String> expected = new TreeSet<String>(Arrays.asList("selector1",
        "selector2", "selector3", "external1", "external2", "external3",
        "selector_with_underscores", "-selector-with-hyphen"));
    Set<String> actual = new TreeSet<>(v.getFoundClasses());

    assertEquals(expected, actual);
  }*/
}
