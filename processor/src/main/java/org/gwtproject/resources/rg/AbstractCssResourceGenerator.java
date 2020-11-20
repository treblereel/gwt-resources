/*
 *
 * Copyright © ${year} ${name}
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/*
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
package org.gwtproject.resources.rg;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import org.gwtproject.dom.client.StyleInjector;
import org.gwtproject.resources.ext.AbstractResourceGenerator;
import org.gwtproject.resources.ext.ResourceContext;
import org.gwtproject.resources.ext.TreeLogger;
import org.gwtproject.resources.ext.UnableToCompleteException;
import org.gwtproject.resources.rg.util.SourceWriter;

/** @author Dmitrii Tikhomirov Created by treblereel 11/6/18 */
public abstract class AbstractCssResourceGenerator extends AbstractResourceGenerator {

  protected boolean isReturnTypeString(TypeMirror classReturnType) {
    return (classReturnType != null
        && !classReturnType.getKind().isPrimitive()
        && classReturnType.toString().equals("java.lang.String"));
  }

  protected void writeEnsureInjected(SourceWriter sw) {
    sw.println("private boolean injected;");
    sw.println("public boolean ensureInjected() {");
    sw.indent();
    sw.println("if (!injected) {");
    sw.indentln("injected = true;");
    sw.indentln(StyleInjector.class.getName() + ".inject(getText());");
    sw.indentln("return true;");
    sw.println("}");
    sw.println("return false;");
    sw.outdent();
    sw.println("}");
  }

  protected void writeGetName(ExecutableElement method, SourceWriter sw) {
    sw.println("public String getName() {");
    sw.indentln("return \"" + method.getSimpleName().toString() + "\";");
    sw.println("}");
  }

  protected void writeGetText(
      TreeLogger logger, ResourceContext context, ExecutableElement method, SourceWriter sw)
      throws UnableToCompleteException {
    String cssExpression = getCssExpression(logger, context, method);

    sw.println("public String getText() {");
    sw.indentln("return " + cssExpression + ";");
    sw.println("}");
  }

  /**
   * Returns the java expression that contains the compiled CSS.
   *
   * @throws UnableToCompleteException
   */
  protected abstract String getCssExpression(
      TreeLogger logger, ResourceContext context, ExecutableElement method)
      throws UnableToCompleteException;

  protected void writeSimpleGetter(
      ExecutableElement methodToImplement, String toReturn, SourceWriter sw) {
    sw.print("public");
    sw.print(" ");
    sw.print(methodToImplement.getReturnType().toString());
    sw.print(" ");
    sw.print(methodToImplement.toString());
    sw.println(" {");
    sw.indentln("return " + toReturn + ";");
    sw.println("}");
  }
}
