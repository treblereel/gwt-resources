/*
 * Copyright 2008 Google Inc.
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
package org.gwtproject.resources.rg;

import static org.gwtproject.resources.client.DataResource.DoNotEmbed;
import static org.gwtproject.resources.client.DataResource.MimeType;

import java.net.URL;
import javax.lang.model.element.ExecutableElement;
import org.gwtproject.resources.client.impl.DataResourcePrototype;
import org.gwtproject.resources.ext.*;
import org.gwtproject.resources.rg.util.SourceWriter;
import org.gwtproject.resources.rg.util.StringSourceWriter;
import org.gwtproject.safehtml.shared.UriUtils;

/** Provides implementations of DataResource. */
public final class DataResourceGenerator extends AbstractResourceGenerator {
  @Override
  public String createAssignment(
      TreeLogger logger, ResourceContext context, ExecutableElement method)
      throws UnableToCompleteException {
    ResourceOracle resourceOracle = context.getGeneratorContext().getResourcesOracle();
    URL[] resources = resourceOracle.findResources(logger, method);

    if (resources.length != 1) {
      logger.log(TreeLogger.ERROR, "Exactly one resource must be specified", null);
      throw new UnableToCompleteException();
    }

    // Determine if a MIME Type has been specified
    MimeType mimeTypeAnnotation = method.getAnnotation(MimeType.class);
    String mimeType = mimeTypeAnnotation != null ? mimeTypeAnnotation.value() : null;

    // Determine if resource should not be embedded
    DoNotEmbed doNotEmbed = method.getAnnotation(DoNotEmbed.class);
    boolean forceExternal = (doNotEmbed != null);

    URL resource = resources[0];
    String outputUrlExpression = context.deploy(resource, mimeType, forceExternal);

    SourceWriter sw = new StringSourceWriter();
    // Convenience when examining the generated code.
    if (!AbstractResourceGenerator.STRIP_COMMENTS) {
      sw.println("// " + resource.toExternalForm());
    }
    sw.println("new " + DataResourcePrototype.class.getName() + "(");
    sw.indent();
    sw.println('"' + method.getSimpleName().toString() + "\",");
    sw.println(
        UriUtils.class.getCanonicalName() + ".fromTrustedString(" + outputUrlExpression + ")");
    sw.outdent();
    sw.print(")");

    return sw.toString();
  }
}
