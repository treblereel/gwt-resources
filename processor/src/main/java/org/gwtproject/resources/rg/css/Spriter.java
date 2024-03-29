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

import static org.gwtproject.resources.client.ImageResource.ImageOptions;
import static org.gwtproject.resources.client.ImageResource.RepeatStyle;
import static org.gwtproject.resources.rg.css.ast.CssProperty.*;

import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.gwtproject.resources.client.ImageResource;
import org.gwtproject.resources.ext.*;
import org.gwtproject.resources.rg.css.ast.*;

/**
 * Replaces CssSprite nodes with CssRule nodes that will display the sprited image. The real trick
 * with spriting the images is to reuse the ImageResource processing framework by requiring the
 * sprite to be defined in terms of an ImageResource.
 */
public class Spriter extends CssModVisitor {
  private final ResourceContext context;
  private final TreeLogger logger;
  private Elements elements;
  private Types types;

  public Spriter(TreeLogger logger, ResourceContext context) {
    elements = context.getGeneratorContext().getAptContext().elements;
    types = context.getGeneratorContext().getAptContext().types;

    this.logger = logger.branch(TreeLogger.DEBUG, "Creating image sprite classes");
    this.context = context;
  }

  @Override
  public void endVisit(CssSprite x, Context ctx) {
    TypeElement bundleType = context.getClientBundleType();
    DotPathValue functionName = x.getResourceFunction();
    elements = context.getGeneratorContext().getAptContext().elements;
    types = context.getGeneratorContext().getAptContext().types;

    if (functionName == null) {
      logger.log(
          TreeLogger.ERROR,
          "The @sprite rule "
              + x.getSelectors()
              + " must specify the "
              + CssSprite.IMAGE_PROPERTY_NAME
              + " property");
      throw new CssCompilerException("No image property specified");
    }

    TypeElement imageResourceType =
        context
            .getGeneratorContext()
            .getAptContext()
            .elements
            .getTypeElement(ImageResource.class.getCanonicalName());
    assert imageResourceType != null;

    // Find the image accessor method
    ExecutableElement imageMethod;
    try {
      imageMethod =
          ResourceGeneratorUtil.getMethodByPath(
              context.getClientBundleType(),
              functionName.getParts(),
              imageResourceType,
              types,
              elements);
    } catch (NotFoundException | UnableToCompleteException e) {
      logger.log(
          TreeLogger.ERROR,
          "Unable to find ImageResource method " + functionName + " in " + bundleType + " : " + e);
      throw new CssCompilerException("Cannot find image function");
    }

    ImageOptions options = imageMethod.getAnnotation(ImageOptions.class);
    RepeatStyle repeatStyle;
    if (options != null) {
      repeatStyle = options.repeatStyle();
    } else {
      repeatStyle = RepeatStyle.None;
    }

    String instance =
        "("
            + context.getImplementationSimpleSourceName()
            + ".this."
            + functionName.getExpression()
            + ")";

    CssRule replacement = new CssRule();
    replacement.getSelectors().addAll(x.getSelectors());
    List<CssProperty> properties = replacement.getProperties();

    if (repeatStyle == RepeatStyle.None || repeatStyle == RepeatStyle.Horizontal) {
      properties.add(
          new CssProperty(
              "height", new ExpressionValue(instance + ".getHeight() + \"px\""), false));
    }

    if (repeatStyle == RepeatStyle.None || repeatStyle == RepeatStyle.Vertical) {
      properties.add(
          new CssProperty("width", new ExpressionValue(instance + ".getWidth() + \"px\""), false));
    }
    properties.add(new CssProperty("overflow", new IdentValue("hidden"), false));

    String repeatText;
    switch (repeatStyle) {
      case None:
        repeatText = " no-repeat";
        break;
      case Horizontal:
        repeatText = " repeat-x";
        break;
      case Vertical:
        repeatText = " repeat-y";
        break;
      case Both:
        repeatText = " repeat";
        break;
      default:
        throw new RuntimeException("Unknown repeatStyle " + repeatStyle);
    }

    String backgroundExpression =
        "\"url(\\\"\" + "
            + instance
            + ".getSafeUri().asString() + \"\\\") -\" + "
            + instance
            + ".getLeft() + \"px -\" + "
            + instance
            + ".getTop() + \"px "
            + repeatText
            + "\"";
    properties.add(new CssProperty("background", new ExpressionValue(backgroundExpression), false));

    // Retain any user-specified properties
    properties.addAll(x.getProperties());

    ctx.replaceMe(replacement);
  }
}
