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
package org.gwtproject.resources.rg;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import org.gwtproject.resources.client.TextResource;
import org.gwtproject.resources.client.impl.ExternalTextResourcePrototype;
import org.gwtproject.resources.ext.*;
import org.gwtproject.resources.rg.util.SourceWriter;
import org.gwtproject.resources.rg.util.StringSourceWriter;
import org.gwtproject.resources.rg.util.Util;
import org.gwtproject.safehtml.shared.UriUtils;

/**
 * Adds {@link ExternalTextResourcePrototype} objects to the bundle.
 *
 * @author Dmitrii Tikhomirov Created by treblereel 11/11/18
 */
public final class ExternalTextResourceGenerator extends AbstractResourceGenerator {
  /**
   * The name of a deferred binding property that determines whether or not this generator will use
   * JSONP to fetch the files.
   */
  // This string must stay in sync with the values in JsonpRequest.java
  private StringBuffer data;

  private boolean first;
  private String urlExpression;
  private Map<String, Integer> hashes;
  private Map<String, Integer> offsets;
  private int currentIndex;
  private String externalTextUrlIdent;
  private String externalTextCacheIdent;

  @Override
  public String createAssignment(
      TreeLogger logger, ResourceContext context, ExecutableElement method) {
    String name = method.getSimpleName().toString();

    SourceWriter sw = new StringSourceWriter();
    sw.println("new " + ExternalTextResourcePrototype.class.getName() + "(");
    sw.indent();
    sw.println('"' + name + "\",");
    // These are field names
    sw.println(UriUtils.class.getName() + ".fromTrustedString(" + externalTextUrlIdent + "),");
    sw.println(externalTextCacheIdent + ", ");
    sw.println(offsets.get(method.getSimpleName().toString()).toString());
    sw.outdent();
    sw.print(")");

    return sw.toString();
  }

  @Override
  public void createFields(TreeLogger logger, ResourceContext context, ClientBundleFields fields)
      throws UnableToCompleteException {
    Elements elements = context.getGeneratorContext().getAptContext().elements;
    data.append(']');
    urlExpression =
        context.deploy(
            Util.getQualifiedSourceName(context.getClientBundleType(), elements).replace('.', '_')
                + "_jsonbundle.txt",
            "text/plain",
            Util.getBytes(data.toString()),
            true);

    TypeElement stringType = elements.getTypeElement(String.class.getCanonicalName());
    assert stringType != null;

    externalTextUrlIdent = fields.define(stringType, "externalTextUrl", urlExpression, true, true);

    TypeElement textResourceType = elements.getTypeElement(TextResource.class.getCanonicalName());
    assert textResourceType != null;

    externalTextCacheIdent =
        fields.define(
            TextResource.class.getCanonicalName() + "[]",
            "externalTextCache",
            "new " + TextResource.class.getName() + "[" + currentIndex + "]",
            true,
            true);
  }

  @Override
  public void init(TreeLogger logger, ResourceContext context) {

    data = new StringBuffer("[\n");
    first = true;
    urlExpression = null;
    hashes = new HashMap<>();
    offsets = new HashMap<>();
    currentIndex = 0;
  }

  @Override
  public void prepare(TreeLogger logger, ResourceContext resourceContext, ExecutableElement method)
      throws UnableToCompleteException {
    ResourceOracle resourceOracle = resourceContext.getGeneratorContext().getResourcesOracle();

    URL[] urls = resourceOracle.findResources(logger, method);

    if (urls.length != 1) {
      logger.log(TreeLogger.ERROR, "Exactly one resource must be specified", null);
      throw new UnableToCompleteException();
    }

    URL resource = urls[0];

    String toWrite = Util.readURLAsString(resource);
    // This de-duplicates strings in the bundle.
    if (!hashes.containsKey(toWrite)) {
      hashes.put(toWrite, currentIndex++);

      if (!first) {
        data.append(",\n");
      } else {
        first = false;
      }

      data.append('"');
      data.append(Generator.escape(toWrite));
      data.append('"');
    }

    // Store the (possibly n:1) mapping of resource function to bundle index.
    offsets.put(method.getSimpleName().toString(), hashes.get(toWrite));
  }
}
