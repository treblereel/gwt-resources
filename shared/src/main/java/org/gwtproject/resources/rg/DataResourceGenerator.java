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

import org.gwtproject.resources.client.impl.DataResourcePrototype;
import org.gwtproject.resources.ext.*;
import org.gwtproject.resources.rg.util.SourceWriter;
import org.gwtproject.resources.rg.util.StringSourceWriter;
import org.gwtproject.safehtml.shared.UriUtils;

import javax.lang.model.element.ExecutableElement;

import static org.gwtproject.resources.client.DataResource.DoNotEmbed;
import static org.gwtproject.resources.client.DataResource.MimeType;

/**
 * Provides implementations of DataResource.
 */
public final class DataResourceGenerator extends AbstractResourceGenerator {
    @Override
    public String createAssignment(TreeLogger logger, ResourceContext context, AptContext aptContext, ExecutableElement method)
            throws UnableToCompleteException {

        Resource resource = ResourceGeneratorUtil.getResource(logger, method, aptContext);


        // Determine if a MIME Type has been specified
        MimeType mimeTypeAnnotation = method.getAnnotation(MimeType.class);
        String mimeType = mimeTypeAnnotation != null ? mimeTypeAnnotation.value() : null;

        // Determine if resource should not be embedded
        DoNotEmbed doNotEmbed = method.getAnnotation(DoNotEmbed.class);
        boolean forceExternal = (doNotEmbed != null);

        String outputUrlExpression = context.deploy(resource.getUrl(), mimeType, forceExternal);

        SourceWriter sw = new StringSourceWriter();
        // Convenience when examining the generated code.
        if (!AbstractResourceGenerator.STRIP_COMMENTS) {
            sw.println("// " + resource.getUrl().getFile());
        }
        sw.println("new " + DataResourcePrototype.class.getName() + "(");
        sw.indent();
        sw.println('"' + method.getSimpleName().toString() + "\",");
        sw.println(UriUtils.class.getCanonicalName() + ".fromTrustedString(" + outputUrlExpression + ")");
        sw.outdent();
        sw.print(")");

        return sw.toString();
    }
}
