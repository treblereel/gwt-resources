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
package org.gwtproject.resources.context;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.gwtproject.resources.client.ClientBundle;
import org.gwtproject.resources.client.CssResource;
import org.gwtproject.resources.client.DataResource;
import org.gwtproject.resources.client.ExternalTextResource;
import org.gwtproject.resources.client.ImageResource;
import org.gwtproject.resources.client.TextResource;
import org.gwtproject.resources.ext.ResourceGenerator;
import org.gwtproject.resources.ext.ResourceGeneratorType;
import org.gwtproject.resources.rg.BundleResourceGenerator;
import org.gwtproject.resources.rg.CssResourceGenerator;
import org.gwtproject.resources.rg.DataResourceGenerator;
import org.gwtproject.resources.rg.ExternalTextResourceGenerator;
import org.gwtproject.resources.rg.ImageResourceGenerator;
import org.gwtproject.resources.rg.TextResourceGenerator;

/** @author Dmitrii Tikhomirov <chani.liet@gmail.com> Created by treblereel on 10/26/18. */
public class AptContext {
  public final Messager messager;
  public final Filer filer;
  public final Elements elements;
  public final Types types;
  public final RoundEnvironment roundEnvironment;
  public final ProcessingEnvironment processingEnv;

  public final Map<Element, Class<? extends ResourceGenerator>> generators = new HashMap<>();

  public AptContext(
      final ProcessingEnvironment processingEnv, final RoundEnvironment roundEnvironment) {
    this.filer = processingEnv.getFiler();
    this.messager = processingEnv.getMessager();
    this.elements = processingEnv.getElementUtils();
    this.types = processingEnv.getTypeUtils();
    this.roundEnvironment = roundEnvironment;

    this.processingEnv = processingEnv;
    initGenerators();
  }

  private void initGenerators() {
    preBuildGenerators();
    userDefinedGenerators();
  }

  private void preBuildGenerators() {
    generators.put(
        elements.getTypeElement(ClientBundle.class.getCanonicalName()),
        BundleResourceGenerator.class);
    generators.put(
        elements.getTypeElement(CssResource.class.getCanonicalName()), CssResourceGenerator.class);
    generators.put(
        elements.getTypeElement(DataResource.class.getCanonicalName()),
        DataResourceGenerator.class);
    generators.put(
        elements.getTypeElement(ExternalTextResource.class.getCanonicalName()),
        ExternalTextResourceGenerator.class);
    generators.put(
        elements.getTypeElement(ImageResource.class.getCanonicalName()),
        ImageResourceGenerator.class);
    generators.put(
        elements.getTypeElement(TextResource.class.getCanonicalName()),
        TextResourceGenerator.class);
  }

  private void userDefinedGenerators() {
    roundEnvironment
        .getElementsAnnotatedWith(ResourceGeneratorType.class)
        .forEach(
            e -> {
              ResourceGeneratorType resourceGeneratorType =
                  e.getAnnotation(ResourceGeneratorType.class);
              String resourceGeneratorName = resourceGeneratorType.value();
              try {
                generators.put(
                    e, (Class<? extends ResourceGenerator>) Class.forName(resourceGeneratorName));
              } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
                throw new Error(e1);
              }
            });
  }
}
