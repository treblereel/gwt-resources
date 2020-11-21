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
package org.gwtproject.resources.rg.gss;

import static com.google.common.css.compiler.ast.CssPseudoClassNode.*;

import com.google.common.collect.ImmutableSet;
import com.google.common.css.compiler.ast.*;
import com.google.common.css.compiler.passes.CodeBuffer;
import com.google.common.css.compiler.passes.GssSourceMapGenerator;
import java.util.Iterator;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/** @author Dmitrii Tikhomirov Created by treblereel 12/5/18 */
public class GssCompactPrinter extends GssCodePrinter implements CssCompilerPass {
  private String compactedPrintedString;
  private static final Logger logger = Logger.getLogger(GssCompactPrinter.class.getName());
  private static final ImmutableSet<String> ARGUMENT_SEPARATORS = ImmutableSet.of(",", "=", " ");

  public GssCompactPrinter(
      CssNode subtree, @Nullable CodeBuffer buffer, @Nullable GssSourceMapGenerator generator) {
    super(subtree, buffer, generator);
    this.compactedPrintedString = null;
  }

  public GssCompactPrinter(CssNode subtree, @Nullable CodeBuffer buffer) {
    this(subtree, buffer, null);
  }

  public GssCompactPrinter(CssNode subtree) {
    this(subtree, null);
  }

  public GssCompactPrinter(
      CssTree tree, @Nullable CodeBuffer buffer, @Nullable GssSourceMapGenerator generator) {
    super(tree, buffer, generator);
    this.compactedPrintedString = null;
  }

  public GssCompactPrinter(CssTree tree, CodeBuffer buffer) {
    this(tree, buffer, null);
  }

  public GssCompactPrinter(CssTree tree, GssSourceMapGenerator generator) {
    this(tree, null, generator);
  }

  public GssCompactPrinter(CssTree tree) {
    this(tree, null, null);
  }

  public boolean enterDefinition(CssDefinitionNode node) {
    return false;
  }

  public boolean enterImportRule(CssImportRuleNode node) {
    this.buffer.append(node.getType().toString());
    Iterator var2 = node.getParameters().iterator();

    while (var2.hasNext()) {
      CssValueNode param = (CssValueNode) var2.next();
      this.buffer.append(' ');
      if (param instanceof CssStringNode) {
        this.buffer.append(param.toString());
      } else {
        this.buffer.append(param.getValue());
      }
    }

    return true;
  }

  public void leaveImportRule(CssImportRuleNode node) {
    this.buffer.append(';');
  }

  public boolean enterMediaRule(CssMediaRuleNode node) {
    this.buffer.append(node.getType().toString());
    if (node.getParameters().size() > 0) {
      this.buffer.append(' ');
    }

    return true;
  }

  private void appendMediaParameterWithParentheses(CssValueNode node) {
    this.buffer.append('(');
    this.buffer.append(node.getValue());
    this.buffer.append(')');
  }

  public void leaveMediaRule(CssMediaRuleNode node) {
    this.buffer.append('}');
  }

  public boolean enterPageRule(CssPageRuleNode node) {
    this.buffer.append(node.getType().toString());
    this.buffer.append(' ');
    Iterator var2 = node.getParameters().iterator();

    while (var2.hasNext()) {
      CssValueNode param = (CssValueNode) var2.next();
      this.buffer.append(param.getValue());
    }

    this.buffer.deleteLastCharIfCharIs(' ');
    return true;
  }

  public boolean enterPageSelector(CssPageSelectorNode node) {
    this.buffer.append(node.getType().toString());
    Iterator var2 = node.getParameters().iterator();

    while (var2.hasNext()) {
      CssValueNode param = (CssValueNode) var2.next();
      this.buffer.append(' ');
      this.buffer.append(param.getValue());
    }

    return true;
  }

  public boolean enterFontFace(CssFontFaceNode node) {
    this.buffer.append(node.getType().toString());
    return true;
  }

  public boolean enterSelector(CssSelectorNode selector) {
    String name = selector.getSelectorName();
    if (name != null) {
      this.buffer.append(name);
    }

    return true;
  }

  public void leaveSelector(CssSelectorNode selector) {
    this.buffer.append(',');
  }

  public boolean enterClassSelector(CssClassSelectorNode node) {
    this.appendRefiner(node);
    return true;
  }

  public boolean enterIdSelector(CssIdSelectorNode node) {
    this.appendRefiner(node);
    return true;
  }

  public boolean enterPseudoClass(CssPseudoClassNode node) {
    this.buffer.append(node.getPrefix());
    this.buffer.append(node.getRefinerName());
    switch (node.getFunctionType()) {
      case NTH:
        this.buffer.append(node.getArgument().replace(" ", ""));
        this.buffer.append(')');
        break;
      case LANG:
        this.buffer.append(node.getArgument());
        this.buffer.append(')');
    }

    return true;
  }

  public void leavePseudoClass(CssPseudoClassNode node) {
    if (node.getFunctionType() == FunctionType.NOT) {
      this.buffer.deleteLastCharIfCharIs(',');
      this.buffer.append(')');
    }
  }

  public boolean enterPseudoElement(CssPseudoElementNode node) {
    this.appendRefiner(node);
    return true;
  }

  public boolean enterAttributeSelector(CssAttributeSelectorNode node) {
    this.buffer.append(node.getPrefix());
    this.buffer.append(node.getAttributeName());
    this.buffer.append(node.getMatchSymbol());
    this.buffer.append(node.getValue());
    this.buffer.append(node.getSuffix());
    return true;
  }

  private void appendRefiner(CssRefinerNode node) {
    this.buffer.append(node.getPrefix());
    this.buffer.append(node.getRefinerName());
  }

  public boolean enterCombinator(CssCombinatorNode combinator) {
    if (combinator != null) {
      this.buffer.append(combinator.getCombinatorType().getCanonicalName());
    }

    return true;
  }

  public void leaveCombinator(CssCombinatorNode combinator) {
    this.buffer.deleteLastCharIfCharIs(',');
  }

  public void leaveSelectorBlock(CssSelectorListNode node) {
    this.buffer.deleteLastCharIfCharIs(',');
  }

  public boolean enterDeclarationBlock(CssDeclarationBlockNode block) {
    this.buffer.append('{');
    return true;
  }

  public void leaveDeclarationBlock(CssDeclarationBlockNode block) {
    this.buffer.deleteLastCharIfCharIs(';');
    this.buffer.append('}');
  }

  public boolean enterBlock(CssBlockNode block) {
    if (block.getParent() instanceof CssUnknownAtRuleNode
        || block.getParent() instanceof CssMediaRuleNode) {
      this.buffer.append('{');
    }

    return true;
  }

  public boolean enterDeclaration(CssDeclarationNode declaration) {
    if (declaration.hasStarHack()) {
      this.buffer.append('*');
    }

    this.buffer.append(declaration.getPropertyName().getValue());
    this.buffer.append(':');
    return true;
  }

  public void leaveDeclaration(CssDeclarationNode declaration) {
    this.buffer.deleteLastCharIfCharIs(' ');
    this.buffer.append(';');
  }

  public void leaveCompositeValueNode(CssCompositeValueNode node) {
    this.buffer.deleteLastCharIfCharIs(' ');
    if (node.getParent() instanceof CssPropertyValueNode) {
      this.buffer.append(' ');
    }
  }

  public boolean enterValueNode(CssValueNode node) {
    if (node instanceof CssPriorityNode) {
      this.buffer.deleteLastCharIfCharIs(' ');
    }

    this.appendValueNode(node);
    return true;
  }

  public void leaveValueNode(CssValueNode node) {
    if (node.getParent() instanceof CssPropertyValueNode) {
      this.buffer.append(' ');
    }
  }

  public boolean enterCompositeValueNodeOperator(CssCompositeValueNode parent) {
    this.buffer.deleteLastCharIfCharIs(' ');
    this.buffer.append(parent.getOperator().getOperatorName());
    return true;
  }

  public boolean enterFunctionNode(CssFunctionNode node) {
    this.buffer.append(node.getFunctionName());
    this.buffer.append('(');
    return true;
  }

  public void leaveFunctionNode(CssFunctionNode node) {
    this.buffer.deleteLastCharIfCharIs(' ');
    this.buffer.append(") ");
  }

  public boolean enterArgumentNode(CssValueNode node) {
    if (ARGUMENT_SEPARATORS.contains(node.toString())) {
      this.buffer.deleteLastCharIfCharIs(' ');
    }

    this.appendValueNode(node);
    return true;
  }

  public boolean enterConditionalBlock(CssConditionalBlockNode node) {
    this.visitController.stopVisit();
    Logger var10000 = logger;
    String var2 = String.valueOf("Conditional block should not be present: ");
    String var3 = node.toString();
    String var10001;
    if (node.getSourceCodeLocation() != null) {
      int var4 = node.getSourceCodeLocation().getLineNumber();
      var10001 = (new StringBuilder(12)).append("@").append(var4).toString();
    } else {
      var10001 = "";
    }

    String var5 = var10001;
    var10000.warning(
        (new StringBuilder(
                0
                    + String.valueOf(var2).length()
                    + String.valueOf(var3).length()
                    + String.valueOf(var5).length()))
            .append(var2)
            .append(var3)
            .append(var5)
            .toString());
    return true;
  }

  public boolean enterUnknownAtRule(CssUnknownAtRuleNode node) {
    this.buffer.append('@').append(node.getName().toString());
    if (node.getParameters().size() > 0) {
      this.buffer.append(' ');
    }

    return true;
  }

  public boolean enterMediaTypeListDelimiter(CssNodesListNode<? extends CssNode> node) {
    this.buffer.append(' ');
    return true;
  }

  public void leaveUnknownAtRule(CssUnknownAtRuleNode node) {
    if (node.getType().hasBlock()) {
      if (!(node.getBlock() instanceof CssDeclarationBlockNode)) {
        this.buffer.append('}');
      }
    } else {
      this.buffer.append(';');
    }
  }

  public boolean enterKeyframesRule(CssKeyframesNode node) {
    this.buffer.append('@').append(node.getName().toString());
    Iterator var2 = node.getParameters().iterator();

    while (var2.hasNext()) {
      CssValueNode param = (CssValueNode) var2.next();
      this.buffer.append(' ');
      this.buffer.append(param.getValue());
    }

    if (node.getType().hasBlock()) {
      this.buffer.append('{');
    }

    return true;
  }

  public void leaveKeyframesRule(CssKeyframesNode node) {
    if (node.getType().hasBlock()) {
      this.buffer.append('}');
    } else {
      this.buffer.append(';');
    }
  }

  public boolean enterKey(CssKeyNode node) {
    String value = node.getKeyValue();
    if (value != null) {
      this.buffer.append(value);
    }

    return true;
  }

  public void leaveKey(CssKeyNode key) {
    this.buffer.append(',');
  }

  public void leaveKeyBlock(CssKeyListNode block) {
    this.buffer.deleteLastCharIfCharIs(',');
  }

  public String getCompactPrintedString() {
    return this.compactedPrintedString;
  }

  public void runPass() {
    this.resetBuffer();
    this.visitController.startVisit(this);
    this.compactedPrintedString = this.getOutputBuffer();
  }

  protected void appendValueNode(CssValueNode node) {
    if (!(node instanceof CssCompositeValueNode)) {
      if (node instanceof CssBooleanExpressionNode
          && node.getParent() instanceof CssMediaRuleNode) {
        this.appendMediaParameterWithParentheses(node);
      } else if (node instanceof CssStringNode) {
        CssStringNode s = (CssStringNode) node;
        this.buffer.append(s.toString(CssStringNode.HTML_ESCAPER));
      } else if (node instanceof CssNumericNode) {
        CssNumericNode n = (CssNumericNode) node;
        this.buffer.append(n.getNumericPart());
        this.buffer.append(n.getUnit());
      } else {
        this.buffer.append(node.toString());
      }
    }
  }

  public static String printCompactly(CssNode n) {
    GssCompactPrinter p = new GssCompactPrinter(n);
    p.runPass();
    return p.getCompactPrintedString().trim();
  }

  public static String printCompactly(CssTree t) {
    return printCompactly(t.getRoot());
  }
}
