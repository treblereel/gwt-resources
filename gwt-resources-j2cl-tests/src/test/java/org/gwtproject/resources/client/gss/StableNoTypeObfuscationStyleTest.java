/*
 * Copyright 2014 Google Inc.
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

package org.gwtproject.resources.client.gss;

import static org.gwtproject.resources.client.gss.TestResources.ClassNameAnnotation;

import org.junit.Ignore;

import static junit.framework.TestCase.*;

/**
 * Tests style classes renaming with the configuration property <code>CssResource.style</code> set
 * to stable-notype.
 */
@Ignore
public class StableNoTypeObfuscationStyleTest {

  public void testClassesRenaming() {
    TestResources testResources = new TestResourcesImpl();
    ClassNameAnnotation classNameAnnotation = testResources.classNameAnnotation();

    String renamedClass = classNameAnnotation.renamedClass();
    String nonRenamedClass = classNameAnnotation.nonRenamedClass();

    assertEquals("renamed-class", renamedClass);
    assertEquals("nonRenamedClass", nonRenamedClass);
  }
}
