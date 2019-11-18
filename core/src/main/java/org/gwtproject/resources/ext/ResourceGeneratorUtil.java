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
package org.gwtproject.resources.ext;

import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.google.common.base.Joiner;
import java.net.URL;
import java.util.*;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/** @author Dmitrii Tikhomirov Created by treblereel 11/11/18 */
public class ResourceGeneratorUtil {

  /**
   * Returns the base filename of a resource. The behavior is similar to the unix command <code>
   * basename</code>.
   *
   * @param resource the URL of the resource
   * @return the final name segment of the resource
   */
  public static String baseName(URL resource) {
    String path = resource.getPath();
    return path.substring(path.lastIndexOf('/') + 1);
  }

  public static DefaultExtensions findDefaultExtensionsInClassHierarcy(TypeElement resourceType) {
    DefaultExtensions defaultExtensions = resourceType.getAnnotation(DefaultExtensions.class);
    if (defaultExtensions != null) {
      return defaultExtensions;
    }
    Set<TypeMirror> sets = new HashSet<>();
    getAllParents(resourceType, sets);
    for (TypeMirror e : sets) {
      DefaultExtensions a = MoreTypes.asElement(e).getAnnotation(DefaultExtensions.class);
      if (a != null) return a;
    }
    return null;
  }

  /**
   * Finds a method by following a dotted path interpreted as a series of no-arg method invocations
   * from an instance of a given root type.
   *
   * @param rootType the type from which the search begins
   * @param pathElements a sequence of no-arg method names
   * @param expectedReturnType the expected return type of the method to locate, or <code>null
   *     </code> if no constraint on the return type is necessary
   * @return the requested JMethod
   * @throws NotFoundException if the requested method could not be found TODO detailed errors
   */
  public static ExecutableElement getMethodByPath(
      TypeElement rootType,
      List<String> pathElements,
      Element expectedReturnType,
      Types types,
      Elements elements)
      throws NotFoundException, UnableToCompleteException {
    if (pathElements.isEmpty()) {
      throw new NotFoundException("No path specified");
    }
    TypeElement currentType = rootType;

    for (String pathElement : pathElements) {
      if (!isClassOrInterface(currentType)) {
        throw new NotFoundException(
            "Cannot resolve member " + pathElement + " on type " + currentType);
      }

      for (ExecutableElement method :
          MoreElements.getLocalAndInheritedMethods(rootType, types, elements)) {
        if (method.getSimpleName().toString().equals(pathElement)) {
          return method;
        }
      }
    }
    StringJoiner sj = new StringJoiner(" ");
    pathElements.forEach(sj::add);
    throw new UnableToCompleteException(
        "Cannot resolve member " + sj.toString() + " on type " + currentType);
  }

  public static boolean isClassOrInterface(TypeElement clazz) {
    return clazz.getKind().isClass() || clazz.getKind().isInterface();
  }

  /**
   * Given a user-defined type name, determine the type name for the generated class based on
   * accumulated requirements.
   */
  public static String generateSimpleSourceName(TreeLogger logger, TypeElement element) {
    List<String> hierarchy = new ArrayList<>();
    Element enclosingElement = element;
    while (!enclosingElement.getKind().equals(ElementKind.PACKAGE)) {
      hierarchy.add(enclosingElement.getSimpleName().toString());
      enclosingElement = enclosingElement.getEnclosingElement();
    }
    Collections.reverse(hierarchy);
    return Joiner.on("_").join(hierarchy) + "Impl";
  }

  public static Set<TypeMirror> getAllParents(TypeElement candidate) {
    Set<TypeMirror> set = new HashSet<>();
    getAllParents(candidate, set);
    return set;
  }

  private static void getAllParents(TypeElement candidate, Set<TypeMirror> set) {
    candidate
        .getInterfaces()
        .forEach(
            e -> {
              set.add(e);
              getAllParents((TypeElement) MoreTypes.asElement(e), set);
            });
  }
}
