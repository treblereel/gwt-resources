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
package org.gwtproject.resources.rg.gss;

import com.google.common.collect.ImmutableList;
import com.google.common.css.SourceCodeLocation;
import com.google.common.css.compiler.ast.CssValueNode;
import com.google.common.css.compiler.ast.ErrorManager;
import com.google.common.css.compiler.ast.GssFunction;
import java.util.List;
import org.gwtproject.resources.rg.gss.ast.CssJavaExpressionNode;

/**
 * GSS function that creates a {@link CssJavaExpressionNode} in order to evaluate a Java expression
 * at runtime.
 */
public class EvalFunction implements GssFunction {

  public static String getName() {
    return "eval";
  }

  @Override
  public List<CssValueNode> getCallResultNodes(List<CssValueNode> args, ErrorManager errorManager) {
    CssValueNode functionToEval = args.get(0);

    SourceCodeLocation sourceCodeLocation = extractSourceCodeLocation(functionToEval);

    CssJavaExpressionNode result =
        new CssJavaExpressionNode(functionToEval.getValue(), sourceCodeLocation);

    return ImmutableList.of(result);
  }

  @Override
  public String getCallResultString(List<String> args) {
    return args.get(0);
  }

  @Override
  public Integer getNumExpectedArguments() {
    return 1;
  }

  private SourceCodeLocation extractSourceCodeLocation(CssValueNode functionToEval) {
    return functionToEval.getParent().getParent().getSourceCodeLocation();
  }
}
