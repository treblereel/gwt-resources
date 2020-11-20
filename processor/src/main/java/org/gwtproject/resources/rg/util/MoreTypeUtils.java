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
package org.gwtproject.resources.rg.util;

import com.google.auto.common.MoreElements;
import com.google.auto.common.Visibility;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/** @author Dmitrii Tikhomirov Created by treblereel 11/11/18 */
public class MoreTypeUtils {

  public static boolean isAbstract(ExecutableElement method) {
    for (Modifier modifier : method.getModifiers()) {
      if (modifier.equals(Modifier.ABSTRACT)) {
        return true;
      }
    }
    return false;
  }

  public static TypeElement getTypeElementFromClass(Class clazz, Elements elements) {
    return elements.getTypeElement(clazz.getCanonicalName());
  }

  public static String getQualifiedSourceName(TypeElement clazz) {
    if (isClassOrInterface(clazz)) {
      return MoreElements.getPackage(clazz).getQualifiedName() + "." + getEnclosingClassName(clazz);
    } else {
      throw new Error("Unable to determine QualifiedSourceName " + clazz);
    }
  }

  public static boolean isClassOrInterface(TypeElement clazz) {
    return clazz.getKind().isClass() || clazz.getKind().isInterface();
  }

  protected static String getEnclosingClassName(Element clazz) {
    return clazz.toString().replace(MoreElements.getPackage(clazz).getQualifiedName() + ".", "");
  }

  /*    public static Set<ExecutableElement> getOverridableMethods(TypeElement type, Elements elements) {
      SetMultimap<String, ExecutableElement> methodMap = LinkedHashMultimap.create();
      getLocalAndInheritedMethods(elements.getPackageOf(type), type, methodMap);
      Set<ExecutableElement> overridden = new LinkedHashSet<ExecutableElement>();
      for (String methodName : methodMap.keySet()) {
          List<ExecutableElement> methodList = ImmutableList.copyOf(methodMap.get(methodName));
          for (int i = 0; i < methodList.size(); i++) {
              ExecutableElement methodI = methodList.get(i);
              for (int j = i + 1; j < methodList.size(); j++) {
                  ExecutableElement methodJ = methodList.get(j);
                  if (elements.overrides(methodJ, methodI, type)) {
                      overridden.add(methodI);
                  }
              }
          }
      }
      return overridden;
  }*/

  /*    public static void getLocalAndInheritedMethods(PackageElement pkg, TypeElement type, SetMultimap<String, ExecutableElement> methods) {
      for (TypeMirror superInterface : type.getInterfaces()) {
          getLocalAndInheritedMethods(pkg, MoreTypes.asTypeElement(superInterface), methods);
      }
      if (type.getSuperclass().getKind() != TypeKind.NONE) {
          // Visit the superclass after superinterfaces so we will always see the implementation of a
          // method after any interfaces that declared it.
          getLocalAndInheritedMethods(pkg, MoreTypes.asTypeElement(type.getSuperclass()), methods);
      }
      for (ExecutableElement method : ElementFilter.methodsIn(type.getEnclosedElements())) {
          if (!method.getModifiers().contains(Modifier.STATIC)
                  && methodVisibleFromPackage(method, pkg)) {
              methods.put(method.getSimpleName().toString(), method);
          }
      }
  }*/

  public static boolean methodVisibleFromPackage(ExecutableElement method, PackageElement pkg) {
    // We use Visibility.ofElement rather than .effectiveVisibilityOfElement because it doesn't
    // really matter whether the containing class is visible. If you inherit a public method
    // then you have a public method, regardless of whether you inherit it from a public class.
    Visibility visibility = Visibility.ofElement(method);
    switch (visibility) {
      case PRIVATE:
        return false;
      case DEFAULT:
        return MoreElements.getPackage(method).equals(pkg);
      default:
        return true;
    }
  }

  public boolean isSameType(Element e1, Element e2, Types types) {
    return types.isSameType(e1.asType(), e2.asType());
  }
}
