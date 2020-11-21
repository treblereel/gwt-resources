/*
 * Copyright 2011 Google Inc.
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
package org.gwtproject.resources.client;

import static junit.framework.TestCase.*;

/** Tests for ExternalTextResource assembly and use. */
public class ExternalTextResourceTest {

  private static final String HELLO = "Hello World!";
  private static final String NEEDS_ESCAPE = "\"'\\";

  public void testExternal() {

    final ExternalTextResources r = new ExternalTextResourceTest_ExternalTextResourcesImpl();
    assertEquals("helloWorldExternal", r.getResource("helloWorldExternal").getName());
    assertEquals("needsEscapeExternal", r.getResource("needsEscapeExternal").getName());
  }

  @Resource
  interface ExternalTextResources extends ClientBundleWithLookup {
    @Source("hello.txt")
    ExternalTextResource helloWorldExternal();

    @Source("shouldBeEscaped.txt")
    ExternalTextResource needsEscapeExternal();
  }

  class MyResourceCallback implements ResourceCallback<TextResource> {
    String result;

    /**
     * Invoked if the asynchronous operation failed.
     *
     * @param e an exception describing the failure
     */
    @Override
    public void onError(ResourceException e) {
      fail(e.toString());
    }

    /**
     * Invoked if the asynchronous operation was successfully completed.
     *
     * @param resource the resource on which the operation was performed
     */
    @Override
    public void onSuccess(TextResource resource) {
      result = resource.getText();
    }
  }
}
