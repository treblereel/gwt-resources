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
package org.gwtproject.resources.rg.gss.ast;

import com.google.common.base.Strings;
import com.google.common.css.SourceCodeLocation;
import com.google.common.css.compiler.ast.CssValueNode;
import java.util.Arrays;
import java.util.List;
import org.gwtproject.resources.rg.Generator;

/** Represents a sequence of no-arg method invocations. */
public class CssDotPathNode extends CssValueNode {

  public static String resolveExpression(
      String instance, String path, String prefix, String suffix) {
    String expression = path.replace(".", "().") + "()";

    if (!Strings.isNullOrEmpty(instance)) {
      expression = instance + "." + expression;
    }

    if (!Strings.isNullOrEmpty(prefix)) {
      expression = "\"" + Generator.escape(prefix) + "\" + " + expression;
    }

    if (!Strings.isNullOrEmpty(suffix)) {
      expression += " + \"" + Generator.escape(suffix) + "\"";
    }

    return expression;
  }

  private String suffix;
  private String prefix;
  private String path;
  private String instance;

  public CssDotPathNode(
      String dotPath, String prefix, String suffix, SourceCodeLocation sourceCodeLocation) {
    this(null, dotPath, prefix, suffix, sourceCodeLocation);
  }

  public CssDotPathNode(
      String instance,
      String dotPath,
      String prefix,
      String suffix,
      SourceCodeLocation sourceCodeLocation) {
    super(resolveExpression(instance, dotPath, prefix, suffix), sourceCodeLocation);

    this.prefix = prefix;
    this.suffix = suffix;
    this.path = dotPath;
    this.instance = instance;
  }

  @Override
  public CssValueNode deepCopy() {
    return new CssDotPathNode(instance, path, prefix, suffix, getSourceCodeLocation());
  }

  public String getPath() {
    return path;
  }

  public String getSuffix() {
    return suffix;
  }

  public String getPrefix() {
    return prefix;
  }

  public String getInstance() {
    return instance;
  }

  public List<String> getPathElements() {
    return Arrays.asList(path.split("\\."));
  }
}
