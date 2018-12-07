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
package org.gwtproject.resources.client;

import org.gwtproject.resources.client.impl.CustomImageResourcePrototype;
import org.gwtproject.resources.ext.*;
import org.gwtproject.resources.rg.util.SourceWriter;
import org.gwtproject.resources.rg.util.StringSourceWriter;
import org.gwtproject.safehtml.shared.UriUtils;

import javax.lang.model.element.ExecutableElement;
import java.net.URL;

/**
 * Generator for {@link CustomImageResource}.
 */
public class CustomImageResourceGenerator extends AbstractResourceGenerator {

    @Override
    public String createAssignment(TreeLogger logger, ResourceContext context, ExecutableElement method)
            throws UnableToCompleteException {

        //TODO
/*        URL[] resources = ResourceGeneratorUtil.findResources(logger, context, method);

        if (resources.length != 1) {
            logger.log(TreeLogger.ERROR, "Exactly one resource must be specified", null);
            throw new UnableToCompleteException();
        }*/

        URL resource = ResourceGeneratorUtil.findResource(logger, method).getUrl();

        SourceWriter sw = new StringSourceWriter();
        sw.println("new " + CustomImageResourcePrototype.class.getName() + "(");
        sw.indent();
        sw.println('"' + method.getSimpleName().toString() + "\",");
        // We don't care about it actually working, so just use the resource URL
        sw.println(UriUtils.class.getName() + ".fromTrustedString(\"" + resource.toExternalForm() + "\")");
        sw.outdent();
        sw.print(")");

        return sw.toString();
    }
}
