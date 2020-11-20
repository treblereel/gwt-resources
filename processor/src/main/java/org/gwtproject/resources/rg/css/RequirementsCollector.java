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

import org.gwtproject.resources.ext.TreeLogger;
import org.gwtproject.resources.rg.css.ast.Context;
import org.gwtproject.resources.rg.css.ast.CssIf;
import org.gwtproject.resources.rg.css.ast.CssVisitor;

/** Analyzes a stylesheet to update the ClientBundleRequirements interface. */
public class RequirementsCollector extends CssVisitor {
  private final TreeLogger logger;

  public RequirementsCollector(TreeLogger logger) {
    this.logger = logger.branch(TreeLogger.DEBUG, "Scanning CSS for requirements");
  }

  @Override
  public void endVisit(CssIf x, Context ctx) {
    String propertyName = x.getPropertyName();
    if (propertyName != null) {
      logger.log(TreeLogger.Type.ALL, "RequirementsCollector " + propertyName);
    }
  }
}
