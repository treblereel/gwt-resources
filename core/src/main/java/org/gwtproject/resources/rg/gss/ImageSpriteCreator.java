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

import static com.google.common.css.compiler.ast.CssFunctionNode.Function;
import static com.google.common.css.compiler.passes.PassUtil.ALTERNATE;
import static org.gwtproject.resources.client.ImageResource.ImageOptions;
import static org.gwtproject.resources.client.ImageResource.RepeatStyle;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.css.SourceCodeLocation;
import com.google.common.css.compiler.ast.*;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.gwtproject.resources.client.ImageResource;
import org.gwtproject.resources.ext.NotFoundException;
import org.gwtproject.resources.ext.ResourceContext;
import org.gwtproject.resources.ext.ResourceGeneratorUtil;
import org.gwtproject.resources.ext.UnableToCompleteException;
import org.gwtproject.resources.rg.gss.ast.CssDotPathNode;

/**
 * Visitor that detects sprite definitions and replace them by several css rules in order to create
 * the corresponding sprited image.
 *
 * <p>This visitor will replace the following gss: {@code .foo { padding: 5px; gwt-sprite:
 * imageResource; width: 150px; } } to the corresponding gss: {@code .foo { padding: 5px;
 * /* @alternate &#42;/ width: eval("imageResource.getWidth", "px"); /* @alternate &#42;/ height:
 * eval("imageResource.getHeight", "px"); /* @alternate &#42;/ overflow: hidden; /* @alternate
 * &#42;/ background: resourceUrl("imageResource") eval("imageResource.getLeft", "px")
 * eval("imageResource.getTop", "px") no-repeat; width: 150px; } }
 *
 * <p>This visitor will also check the presence of the {@link ImageOptions} annotation on the image
 * resource in order to support correctly horizontal or vertical repetition.
 */
public class ImageSpriteCreator extends DefaultTreeVisitor implements CssCompilerPass {
  private static final String SPRITE_PROPERTY_NAME = "gwt-sprite";
  private final MutatingVisitController visitController;
  private final ErrorManager errorManager;
  private final ResourceContext context;
  private final MethodByPathHelper methodByPathHelper;
  private final TypeElement imageResourceType;
  private final String resourceThisPrefix;
  private Elements elements;
  private Types types;

  public ImageSpriteCreator(
      MutatingVisitController visitController, ResourceContext context, ErrorManager errorManager) {
    this(visitController, context, errorManager, new MethodByPathHelperImpl());
  }

  @VisibleForTesting
  ImageSpriteCreator(
      MutatingVisitController visitController,
      ResourceContext context,
      ErrorManager errorManager,
      MethodByPathHelper methodByPathHelper) {

    this.visitController = visitController;
    this.errorManager = errorManager;
    this.context = context;

    elements = context.getGeneratorContext().getAptContext().elements;
    types = context.getGeneratorContext().getAptContext().types;

    this.methodByPathHelper = methodByPathHelper;
    this.imageResourceType = elements.getTypeElement(ImageResource.class.getCanonicalName());

    this.resourceThisPrefix = context.getImplementationSimpleSourceName() + ".this";
  }

  @Override
  public boolean enterDeclaration(CssDeclarationNode declaration) {
    String propertyName = declaration.getPropertyName().getPropertyName();

    if (SPRITE_PROPERTY_NAME.equals(propertyName)) {
      createSprite(declaration);
      return true;
    }

    return super.enterDeclaration(declaration);
  }

  private void createSprite(CssDeclarationNode declaration) {
    List<CssValueNode> valuesNodes = declaration.getPropertyValue().getChildren();

    if (valuesNodes.size() != 1) {
      errorManager.report(
          new GssError(
              SPRITE_PROPERTY_NAME + " must have exactly one value",
              declaration.getSourceCodeLocation()));
      return;
    }

    String imageResource = valuesNodes.get(0).getValue();

    ExecutableElement imageMethod;
    try {
      imageMethod =
          methodByPathHelper.getMethodByPath(
              context, getPathElement(imageResource), imageResourceType);
    } catch (NotFoundException | UnableToCompleteException e) {
      errorManager.report(
          new GssError(
              "Unable to find ImageResource method "
                  + imageResource
                  + " in "
                  + context.getClientBundleType()
                  + " : "
                  + e.getMessage(),
              declaration.getSourceCodeLocation()));
      return;
    }

    ImageOptions options = imageMethod.getAnnotation(ImageOptions.class);
    RepeatStyle repeatStyle = options != null ? options.repeatStyle() : RepeatStyle.None;

    ImmutableList.Builder<CssDeclarationNode> listBuilder = ImmutableList.builder();
    SourceCodeLocation sourceCodeLocation = declaration.getSourceCodeLocation();

    String repeatText;
    switch (repeatStyle) {
      case None:
        repeatText = " no-repeat";
        listBuilder.add(buildHeightDeclaration(imageResource, sourceCodeLocation));
        listBuilder.add(buildWidthDeclaration(imageResource, sourceCodeLocation));
        break;
      case Horizontal:
        repeatText = " repeat-x";
        listBuilder.add(buildHeightDeclaration(imageResource, sourceCodeLocation));
        break;
      case Vertical:
        repeatText = " repeat-y";
        listBuilder.add(buildWidthDeclaration(imageResource, sourceCodeLocation));
        break;
      case Both:
        repeatText = " repeat";
        break;
      default:
        errorManager.report(new GssError("Unknown repeatStyle " + repeatStyle, sourceCodeLocation));
        return;
    }

    listBuilder.add(buildOverflowDeclaration(sourceCodeLocation));
    listBuilder.add(buildBackgroundDeclaration(imageResource, repeatText, sourceCodeLocation));

    visitController.replaceCurrentBlockChildWith(listBuilder.build(), false);
  }

  private CssDeclarationNode buildBackgroundDeclaration(
      String imageResource, String repeatText, SourceCodeLocation location) {
    // build the url function
    CssFunctionNode urlFunction = new CssFunctionNode(Function.byName("url"), location);
    CssDotPathNode imageUrl =
        new CssDotPathNode(
            resourceThisPrefix, imageResource + ".getSafeUri" + ".asString", null, null, location);
    CssFunctionArgumentsNode urlFunctionArguments = new CssFunctionArgumentsNode();
    urlFunctionArguments.addChildToBack(imageUrl);
    urlFunction.setArguments(urlFunctionArguments);

    // build left offset
    CssDotPathNode left =
        new CssDotPathNode(resourceThisPrefix, imageResource + ".getLeft", "-", "px", location);

    // build top offset
    CssDotPathNode top =
        new CssDotPathNode(resourceThisPrefix, imageResource + ".getTop", "-", "px", location);

    // build repeat
    CssLiteralNode repeat = new CssLiteralNode(repeatText, location);

    CssPropertyNode propertyNode = new CssPropertyNode("background", location);
    CssPropertyValueNode propertyValueNode =
        new CssPropertyValueNode(ImmutableList.of(urlFunction, left, top, repeat));
    propertyValueNode.setSourceCodeLocation(location);

    return createDeclarationNode(propertyNode, propertyValueNode, location, true);
  }

  private CssDeclarationNode buildHeightDeclaration(
      String imageResource, SourceCodeLocation location) {
    CssPropertyNode propertyNode = new CssPropertyNode("height", location);
    CssValueNode valueNode =
        new CssDotPathNode(resourceThisPrefix, imageResource + ".getHeight", null, "px", location);

    CssPropertyValueNode propertyValueNode = new CssPropertyValueNode(ImmutableList.of(valueNode));

    return createDeclarationNode(propertyNode, propertyValueNode, location, true);
  }

  private CssDeclarationNode createDeclarationNode(
      CssPropertyNode propertyNode,
      CssPropertyValueNode propertyValueNode,
      SourceCodeLocation location,
      boolean useAlternate) {
    CssDeclarationNode replaceNode = new CssDeclarationNode(propertyNode, propertyValueNode);
    replaceNode.setSourceCodeLocation(location);

    if (useAlternate) {
      replaceNode.setComments(ImmutableList.of(new CssCommentNode(ALTERNATE, location)));
    }

    return replaceNode;
  }

  private CssDeclarationNode buildOverflowDeclaration(SourceCodeLocation location) {
    CssPropertyNode propertyNode = new CssPropertyNode("overflow", location);
    CssValueNode valueNode = new CssLiteralNode("hidden", location);

    CssPropertyValueNode propertyValueNode = new CssPropertyValueNode(ImmutableList.of(valueNode));

    return createDeclarationNode(propertyNode, propertyValueNode, location, true);
  }

  private CssDeclarationNode buildWidthDeclaration(
      String imageResource, SourceCodeLocation location) {
    CssPropertyNode propertyNode = new CssPropertyNode("width", location);
    CssValueNode valueNode =
        new CssDotPathNode(resourceThisPrefix, imageResource + ".getWidth", null, "px", location);
    CssPropertyValueNode propertyValueNode = new CssPropertyValueNode(ImmutableList.of(valueNode));

    return createDeclarationNode(propertyNode, propertyValueNode, location, true);
  }

  private List<String> getPathElement(String imageResourcePath) {
    return Lists.newArrayList(imageResourcePath.split("\\."));
  }

  @Override
  public void runPass() {
    visitController.startVisit(this);
  }

  @VisibleForTesting
  interface MethodByPathHelper {
    ExecutableElement getMethodByPath(
        ResourceContext context, List<String> pathElements, TypeElement expectedReturnType)
        throws NotFoundException, UnableToCompleteException;
  }

  private static class MethodByPathHelperImpl implements MethodByPathHelper {
    @Override
    public ExecutableElement getMethodByPath(
        ResourceContext context, List<String> pathElements, TypeElement expectedReturnType)
        throws NotFoundException, UnableToCompleteException {
      return ResourceGeneratorUtil.getMethodByPath(
          context.getClientBundleType(),
          pathElements,
          expectedReturnType,
          context.getGeneratorContext().getAptContext().types,
          context.getGeneratorContext().getAptContext().elements);
    }
  }
}
