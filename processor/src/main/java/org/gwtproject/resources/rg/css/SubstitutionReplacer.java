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

import static org.gwtproject.resources.rg.css.ast.CssProperty.*;

import com.google.auto.common.MoreTypes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.gwtproject.resources.client.ClientBundle;
import org.gwtproject.resources.client.DataResource;
import org.gwtproject.resources.client.ImageResource;
import org.gwtproject.resources.client.Resource;
import org.gwtproject.resources.ext.*;
import org.gwtproject.resources.rg.css.ast.*;

/** Substitute symbolic replacements into string values. */
public class SubstitutionReplacer extends CssVisitor {
  private final ResourceContext context;
  private final TreeLogger logger;
  private final Map<String, CssDef> substitutions;
  private TypeElement clientBundleType;
  private TypeElement dataResourceType;
  private TypeElement imageResourceType;
  private Elements elements;
  private Types types;

  public SubstitutionReplacer(
      TreeLogger logger, ResourceContext context, Map<String, CssDef> substitutions) {
    this.context = context;
    this.logger = logger;
    this.substitutions = substitutions;

    elements = context.getGeneratorContext().getAptContext().elements;
    types = context.getGeneratorContext().getAptContext().types;

    dataResourceType = elements.getTypeElement(DataResource.class.getCanonicalName());
    imageResourceType = elements.getTypeElement(ImageResource.class.getCanonicalName());
    clientBundleType = elements.getTypeElement(ClientBundle.class.getCanonicalName());
  }

  @Override
  public void endVisit(CssProperty x, Context ctx) {
    if (x.getValues() == null) {
      // Nothing to do
      return;
    }
    x.setValue(substituteDefs(x.getValues()));
  }

  private ListValue substituteDefs(ListValue listValue) {

    List<Value> result = new ArrayList<>(listValue.getValues().size());
    for (Value val : listValue.getValues()) {
      if (val.isFunctionValue() != null) {
        // Recursively perform substitution on a function's values
        FunctionValue fnVal = val.isFunctionValue();
        ListValue newVals = substituteDefs(fnVal.getValues());
        result.add(new FunctionValue(fnVal.getName(), newVals));
        continue;
      }

      IdentValue maybeIdent = val.isIdentValue();
      if (maybeIdent == null) {
        // Not an ident, append as-is to result
        result.add(val);
        continue;
      }

      String identStr = maybeIdent.getIdent();
      CssDef def = substitutions.get(identStr);

      if (def == null) {
        // No substitution found, append as-is to result
        result.add(val);
        continue;
      } else if (def instanceof CssUrl) {
        assert def.getValues().size() == 1;
        assert def.getValues().get(0).isDotPathValue() != null;
        DotPathValue functionName = def.getValues().get(0).isDotPathValue();

        TypeElement methodType = null;
        ExecutableElement method = null;

        try {
          method =
              ResourceGeneratorUtil.getMethodByPath(
                  context.getClientBundleType(), functionName.getParts(), null, types, elements);

          methodType = (TypeElement) MoreTypes.asElement(method.getReturnType());
        } catch (NotFoundException e) {
          logger.log(TreeLogger.ERROR, e.getMessage());
          throw new CssCompilerException("Cannot find data method");
        } catch (UnableToCompleteException e) {
          e.printStackTrace();
        }

        boolean is = types.isSubtype(methodType.asType(), dataResourceType.asType());
        boolean is2 = types.isSubtype(methodType.asType(), imageResourceType.asType());

        boolean isClientBundle =
            methodType.getAnnotation(Resource.class) != null
                && types.isSubtype(methodType.asType(), clientBundleType.asType());
        if (!isClientBundle) {
          if (!is && !is2) {
            String message =
                "Invalid method '"
                    + method
                    + "' type for url substitution: "
                    + methodType
                    + ". "
                    + "Only DataResource and ImageResource are supported.";
            logger.log(TreeLogger.ERROR, message);
            throw new CssCompilerException(message);
          }
        }

        StringBuilder expression = new StringBuilder();
        expression.append("\"url('\" + ");
        expression.append(context.getImplementationSimpleSourceName());
        expression.append(".this.");
        expression.append(functionName.getExpression());
        expression.append(".getSafeUri().asString()");
        expression.append(" + \"')\"");
        result.add(new ExpressionValue(expression.toString()));
      } else {
        for (Value defValue : def.getValues()) {
          result.add(defValue);
        }
      }
    }
    return new ListValue(result);
  }
}
