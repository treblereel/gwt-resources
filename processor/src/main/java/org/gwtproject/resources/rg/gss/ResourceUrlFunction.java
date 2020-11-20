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
package org.gwtproject.resources.rg.gss;

import com.google.auto.common.MoreTypes;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.css.SourceCodeLocation;
import com.google.common.css.compiler.ast.*;
import com.google.common.css.compiler.gssfunctions.GssFunctions;
import java.util.List;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.gwtproject.resources.client.DataResource;
import org.gwtproject.resources.client.ImageResource;
import org.gwtproject.resources.client.Resource;
import org.gwtproject.resources.ext.NotFoundException;
import org.gwtproject.resources.ext.ResourceContext;
import org.gwtproject.resources.ext.ResourceGeneratorUtil;
import org.gwtproject.resources.ext.UnableToCompleteException;
import org.gwtproject.resources.rg.gss.ast.CssDotPathNode;
import org.gwtproject.resources.rg.gss.ast.CssJavaExpressionNode;

/** Gss function that create the needed nodes in order to correctly get the url of a resource. */
public class ResourceUrlFunction implements GssFunction {

  private final ResourceContext context;
  private final MethodByPathHelper methodByPathHelper;
  private final TypeElement dataResourceType;
  private final TypeElement imageResourceType;

  private final Elements elements;
  private final Types types;

  public ResourceUrlFunction(ResourceContext context) {
    this(context, new MethodByPathHelperImpl());
  }

  @VisibleForTesting
  ResourceUrlFunction(ResourceContext context, MethodByPathHelper methodByPathHelper) {
    this.context = context;
    elements = context.getGeneratorContext().getAptContext().elements;
    types = context.getGeneratorContext().getAptContext().types;

    this.methodByPathHelper = methodByPathHelper;

    dataResourceType = elements.getTypeElement(DataResource.class.getCanonicalName());
    imageResourceType = elements.getTypeElement(ImageResource.class.getCanonicalName());
  }

  public static String getName() {
    return "resourceUrl";
  }

  @Override
  public Integer getNumExpectedArguments() {
    return 1;
  }

  @Override
  public List<CssValueNode> getCallResultNodes(
      List<CssValueNode> cssValueNodes, ErrorManager errorManager) throws GssFunctionException {
    CssValueNode functionToEval = cssValueNodes.get(0);
    String value = functionToEval.getValue();
    SourceCodeLocation location = functionToEval.getSourceCodeLocation();

    String javaExpression = buildJavaExpression(value, location, errorManager);

    CssFunctionNode urlNode = buildUrlNode(javaExpression, location);

    return ImmutableList.of(urlNode);
  }

  private String buildJavaExpression(
      String value, SourceCodeLocation location, ErrorManager errorManager)
      throws GssFunctionException {
    CssDotPathNode dotPathValue = new CssDotPathNode(value, "", "", location);

    assertMethodIsValidResource(location, dotPathValue.getPathElements(), errorManager);

    return context.getImplementationSimpleSourceName()
        + ".this."
        + dotPathValue.getValue()
        + ".getSafeUri().asString()";
  }

  private void assertMethodIsValidResource(
      SourceCodeLocation location, List<String> pathElements, ErrorManager errorManager)
      throws GssFunctionException {
    TypeElement methodType;

    try {
      methodType = methodByPathHelper.getReturnType(context, pathElements);
    } catch (NotFoundException | UnableToCompleteException e) {
      String message = e.getMessage() != null ? e.getMessage() : "Invalid path";
      errorManager.report(new GssError(message, location));
      throw new GssFunctionException(message, e);
    }

    boolean isClientBundle =
        methodType.getAnnotation(Resource.class) != null
            && types.isSubtype(methodType.asType(), methodType.asType());

    boolean is = types.isSubtype(methodType.asType(), dataResourceType.asType());
    boolean is2 = types.isSubtype(methodType.asType(), imageResourceType.asType());

    if (!isClientBundle) {
      if (!is && !is2) {
        String message =
            "Invalid method type for url substitution: "
                + methodType
                + ". "
                + "Only DataResource and ImageResource are supported.";
        errorManager.report(new GssError(message, location));
        throw new GssFunctionException(message);
      }
    }
  }

  private CssFunctionNode buildUrlNode(String javaExpression, SourceCodeLocation location) {
    CssFunctionNode urlNode = GssFunctions.createUrlNode("", location);
    CssJavaExpressionNode cssJavaExpressionNode = new CssJavaExpressionNode(javaExpression);
    CssFunctionArgumentsNode arguments =
        new CssFunctionArgumentsNode(ImmutableList.of(cssJavaExpressionNode));
    urlNode.setArguments(arguments);

    return urlNode;
  }

  @Override
  public String getCallResultString(List<String> strings) {
    return strings.get(0);
  }

  @VisibleForTesting
  interface MethodByPathHelper {
    TypeElement getReturnType(ResourceContext context, List<String> pathElements)
        throws NotFoundException, UnableToCompleteException;
  }

  private static class MethodByPathHelperImpl implements MethodByPathHelper {
    @Override
    public TypeElement getReturnType(ResourceContext context, List<String> pathElements)
        throws NotFoundException, UnableToCompleteException {
      return (TypeElement)
          MoreTypes.asElement(
              ResourceGeneratorUtil.getMethodByPath(
                      context.getClientBundleType(),
                      pathElements,
                      null,
                      context.getGeneratorContext().getAptContext().types,
                      context.getGeneratorContext().getAptContext().elements)
                  .getReturnType());
    }
  }
}
