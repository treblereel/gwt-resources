/*
 * Copyright 2015 Google Inc.
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

import com.google.gwt.junit.client.GWTTestCase;
import org.gwtproject.resources.client.ClientBundle;
import org.gwtproject.resources.client.CssResource;
import org.gwtproject.resources.client.Resource;
import org.junit.Ignore;

/** Test for GssResource in RTL mode. */
@Ignore
public class GssResourceRTLTest extends GWTTestCase {

  interface Css extends CssResource {
    String a();
  }

  @Resource
  public interface Resources extends ClientBundle {
    @Source("rtl.css")
    Css css();
  }

  @Override
  public String getModuleName() {
    return "org.gwtproject.resources.GssResourceRTLTest";
  }

  public void test() {
    Resources r = new GssResourceRTLTest_ResourcesImpl();

    String css = r.css().getText();
    assertTrue(css.contains("direction:ltr"));
    assertTrue(css.contains("text-align:left"));
  }
}
