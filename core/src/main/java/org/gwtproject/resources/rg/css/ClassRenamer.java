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

import java.util.*;
import java.util.regex.Matcher;
import javax.lang.model.element.ExecutableElement;
import org.gwtproject.resources.client.CssResource;
import org.gwtproject.resources.client.CssResource.ClassName;
import org.gwtproject.resources.ext.TreeLogger;
import org.gwtproject.resources.rg.css.ast.*;

/** Renames class selectors to their obfuscated names. */
public class ClassRenamer extends CssVisitor {

  /**
   * A tag to indicate that an externally-defined CSS class has no JMethod that is used to access
   * it.
   */
  private static final Replacement UNREFERENCED_EXTERNAL = new Replacement(null, null);
  /** Records replacements that have actually been performed. */
  private final Map<ExecutableElement, String> actualReplacements = new IdentityHashMap<>();

  private final Map<String, Map<ExecutableElement, String>> classReplacementsWithPrefix;
  private final Set<String> cssDefs = new HashSet<String>();
  private final Set<String> externalClasses;
  private final TreeLogger logger;
  private final Set<ExecutableElement> missingClasses;
  private final boolean strict;
  private final Set<String> unknownClasses = new HashSet<String>();

  public ClassRenamer(
      TreeLogger logger,
      Map<String, Map<ExecutableElement, String>> classReplacementsWithPrefix,
      boolean strict,
      Set<String> externalClasses) {
    this.logger = logger.branch(TreeLogger.DEBUG, "Replacing CSS class names");
    this.classReplacementsWithPrefix = classReplacementsWithPrefix;
    this.strict = strict;
    this.externalClasses = externalClasses;

    // Require a definition for all classes in the default namespace
    assert classReplacementsWithPrefix.containsKey("");
    missingClasses = new HashSet<>(classReplacementsWithPrefix.get("").keySet());
  }

  @Override
  public void endVisit(CssDef x, Context ctx) {
    cssDefs.add(x.getKey());
  }

  @Override
  public void endVisit(CssSelector x, Context ctx) {

    final Map<String, Replacement> potentialReplacements;
    potentialReplacements = computeReplacements(classReplacementsWithPrefix, externalClasses);

    String sel = x.getSelector();
    int originalLength = sel.length();

    Matcher ma = CssSelector.CLASS_SELECTOR_PATTERN.matcher(sel);
    StringBuilder sb = new StringBuilder(originalLength);
    int start = 0;

    while (ma.find()) {
      String sourceClassName = ma.group(1);
      Replacement entry = potentialReplacements.get(sourceClassName);

      if (entry == null) {
        unknownClasses.add(sourceClassName);
        continue;

      } else if (entry == UNREFERENCED_EXTERNAL) {
        // An @external without an accessor method. This is OK.
        continue;
      }

      ExecutableElement method = entry.getMethod();
      String obfuscatedClassName = entry.getObfuscatedClassName();

      // Consume the interstitial portion of the original selector
      sb.append(sel.subSequence(start, ma.start(1)));
      sb.append(obfuscatedClassName);
      start = ma.end(1);

      actualReplacements.put(method, obfuscatedClassName);
      missingClasses.remove(method);
    }

    if (start != 0) {
      // Consume the remainder and update the selector
      sb.append(sel.subSequence(start, originalLength));
      x.setSelector(sb.toString());
    }
  }

  /**
   * Flatten class name lookups to speed selector rewriting.
   *
   * @param classReplacementsWithPrefix a map of local prefixes to the obfuscated names of imported
   *     methods. If a CssResource makes use of the {@link CssResource.Import} annotation, the keys
   *     of this map will correspond to the {@link CssResource.ImportedWithPrefix} value defined on
   *     the imported CssResource. The zero-length string key holds the obfuscated names for the
   *     CssResource that is being generated.
   * @return A flattened version of the classReplacementWithPrefix map, where the keys are the
   *     source class name (with prefix included), and values have the obfuscated class name and
   *     associated JMethod.
   */
  private Map<String, Replacement> computeReplacements(
      Map<String, Map<ExecutableElement, String>> classReplacementsWithPrefix,
      Set<String> externalClasses) {

    Map<String, Replacement> toReturn = new HashMap<>();

    for (String externalClass : externalClasses) {
      toReturn.put(externalClass, UNREFERENCED_EXTERNAL);
    }

    for (Map.Entry<String, Map<ExecutableElement, String>> outerEntry :
        classReplacementsWithPrefix.entrySet()) {
      String prefix = outerEntry.getKey();

      for (Map.Entry<ExecutableElement, String> entry : outerEntry.getValue().entrySet()) {
        ExecutableElement method = entry.getKey();

        String sourceClassName = method.getSimpleName().toString();
        String obfuscatedClassName = entry.getValue();

        if (cssDefs.contains(sourceClassName)) {
          continue;
        }

        ClassName className = method.getAnnotation(ClassName.class);
        if (className != null) {
          sourceClassName = className.value();
        }

        sourceClassName = prefix + sourceClassName;

        if (externalClasses.contains(sourceClassName)) {
          /*
           * It simplifies the sanity-checking logic to treat external classes
           * as though they were simply obfuscated to exactly the value the user
           * wants.
           */
          obfuscatedClassName = sourceClassName;
        }

        toReturn.put(sourceClassName, new Replacement(method, obfuscatedClassName));
      }
    }
    return Collections.unmodifiableMap(toReturn);
  }

  @Override
  public void endVisit(CssStylesheet x, Context ctx) {
    boolean stop = false;

    // Skip names corresponding to @def entries. They too can be declared as
    // String accessors.
    List<ExecutableElement> toRemove = new ArrayList<>();
    for (ExecutableElement method : missingClasses) {
      if (cssDefs.contains(method.getSimpleName().toString())) {
        toRemove.add(method);
      }
    }
    for (ExecutableElement method : toRemove) {
      missingClasses.remove(method);
    }

    if (!missingClasses.isEmpty()) {
      stop = true;
      TreeLogger errorLogger =
          logger.branch(
              TreeLogger.INFO,
              "The following obfuscated style classes were missing from " + "the source CSS file:");
      for (ExecutableElement m : missingClasses) {
        String name = m.getSimpleName().toString();
        ClassName className = m.getAnnotation(ClassName.class);
        if (className != null) {
          name = className.value();
        }
        errorLogger.log(TreeLogger.ERROR, name + ": Fix by adding ." + name + "{}");
      }
    }

    if (strict && !unknownClasses.isEmpty()) {
      stop = true;
      TreeLogger errorLogger =
          logger.branch(
              TreeLogger.ERROR,
              "The following unobfuscated classes were present in a strict CssResource:");
      for (String s : unknownClasses) {
        errorLogger.log(TreeLogger.ERROR, s);
      }
      if (errorLogger.isLoggable(TreeLogger.INFO)) {
        errorLogger.log(
            TreeLogger.INFO,
            "Fix by adding String accessor "
                + "method(s) to the CssResource interface for obfuscated classes, "
                + "or using an @external declaration for unobfuscated classes.");
      }
    }

    if (stop) {
      throw new CssCompilerException("Missing a CSS replacement");
    }
  }

  /** Reports the replacements that were actually performed by this visitor. */
  public Map<ExecutableElement, String> getReplacements() {
    return actualReplacements;
  }

  /*
   * TODO: Replace with Pair<A, B>.
   */
  private static class Replacement {

    private ExecutableElement method;
    private String obfuscatedClassName;

    public Replacement(ExecutableElement method, String obfuscatedClassName) {
      this.method = method;
      this.obfuscatedClassName = obfuscatedClassName;
    }

    public ExecutableElement getMethod() {
      return method;
    }

    public String getObfuscatedClassName() {
      return obfuscatedClassName;
    }

    /** For debugging use only. */
    @Override
    public String toString() {
      if (this == UNREFERENCED_EXTERNAL) {
        return "Unreferenced external class name";
      } else {
        return method.getSimpleName().toString() + "=" + obfuscatedClassName;
      }
    }
  }
}
