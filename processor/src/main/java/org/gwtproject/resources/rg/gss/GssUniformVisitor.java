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

import com.google.common.css.compiler.ast.*;

/** @author Dmitrii Tikhomirov Created by treblereel 12/5/18 */
public class GssUniformVisitor implements CssTreeVisitor {
  public GssUniformVisitor() {}

  public boolean enterConditionalRule(CssConditionalRuleNode node) {
    this.enter(node);
    return true;
  }

  public void enter(CssNode node) {}

  public void leaveConditionalRule(CssConditionalRuleNode node) {
    this.leave(node);
  }

  public void leave(CssNode node) {}

  public boolean enterImportRule(CssImportRuleNode node) {
    this.enter(node);
    return true;
  }

  public void leaveImportRule(CssImportRuleNode node) {
    this.leave(node);
  }

  public boolean enterMediaRule(CssMediaRuleNode node) {
    this.enter(node);
    return true;
  }

  public void leaveMediaRule(CssMediaRuleNode node) {
    this.leave(node);
  }

  public boolean enterPageRule(CssPageRuleNode node) {
    this.enter(node);
    return true;
  }

  public void leavePageRule(CssPageRuleNode node) {
    this.leave(node);
  }

  public boolean enterPageSelector(CssPageSelectorNode node) {
    this.enter(node);
    return true;
  }

  public void leavePageSelector(CssPageSelectorNode node) {
    this.leave(node);
  }

  public boolean enterFontFace(CssFontFaceNode node) {
    this.enter(node);
    return true;
  }

  public void leaveFontFace(CssFontFaceNode node) {
    this.leave(node);
  }

  public boolean enterDefinition(CssDefinitionNode node) {
    this.enter(node);
    return true;
  }

  public void leaveDefinition(CssDefinitionNode node) {
    this.leave(node);
  }

  public boolean enterUnknownAtRule(CssUnknownAtRuleNode node) {
    this.enter(node);
    return true;
  }

  public void leaveUnknownAtRule(CssUnknownAtRuleNode node) {
    this.leave(node);
  }

  public boolean enterMediaTypeListDelimiter(CssNodesListNode<? extends CssNode> node) {
    return true;
  }

  public void leaveMediaTypeListDelimiter(CssNodesListNode<? extends CssNode> node) {}

  public boolean enterComponent(CssComponentNode node) {
    this.enter(node);
    return true;
  }

  public void leaveComponent(CssComponentNode node) {
    this.leave(node);
  }

  public boolean enterKeyframesRule(CssKeyframesNode node) {
    this.enter(node);
    return true;
  }

  public void leaveKeyframesRule(CssKeyframesNode node) {
    this.leave(node);
  }

  public boolean enterMixinDefinition(CssMixinDefinitionNode node) {
    this.enter(node);
    return true;
  }

  public void leaveMixinDefinition(CssMixinDefinitionNode node) {
    this.leave(node);
  }

  public boolean enterMixin(CssMixinNode node) {
    this.enter(node);
    return true;
  }

  public void leaveMixin(CssMixinNode node) {
    this.leave(node);
  }

  public boolean enterTree(CssRootNode root) {
    this.enter(root);
    return true;
  }

  public void leaveTree(CssRootNode root) {
    this.leave(root);
  }

  public boolean enterImportBlock(CssImportBlockNode block) {
    this.enter(block);
    return true;
  }

  public void leaveImportBlock(CssImportBlockNode block) {
    this.leave(block);
  }

  public boolean enterBlock(CssBlockNode block) {
    this.enter(block);
    return true;
  }

  public void leaveBlock(CssBlockNode block) {
    this.leave(block);
  }

  public boolean enterConditionalBlock(CssConditionalBlockNode block) {
    this.enter(block);
    return true;
  }

  public void leaveConditionalBlock(CssConditionalBlockNode block) {
    this.leave(block);
  }

  public boolean enterDeclarationBlock(CssDeclarationBlockNode block) {
    this.enter(block);
    return true;
  }

  public void leaveDeclarationBlock(CssDeclarationBlockNode block) {
    this.leave(block);
  }

  public boolean enterRuleset(CssRulesetNode ruleset) {
    this.enter(ruleset);
    return true;
  }

  public void leaveRuleset(CssRulesetNode ruleset) {
    this.leave(ruleset);
  }

  public boolean enterSelectorBlock(CssSelectorListNode block) {
    this.enter(block);
    return true;
  }

  public void leaveSelectorBlock(CssSelectorListNode block) {
    this.leave(block);
  }

  public boolean enterDeclaration(CssDeclarationNode declaration) {
    this.enter(declaration);
    return true;
  }

  public void leaveDeclaration(CssDeclarationNode declaration) {
    this.leave(declaration);
  }

  public boolean enterSelector(CssSelectorNode selector) {
    this.enter(selector);
    return true;
  }

  public void leaveSelector(CssSelectorNode selector) {
    this.leave(selector);
  }

  public boolean enterClassSelector(CssClassSelectorNode classSelector) {
    this.enter(classSelector);
    return true;
  }

  public void leaveClassSelector(CssClassSelectorNode classSelector) {
    this.leave(classSelector);
  }

  public boolean enterIdSelector(CssIdSelectorNode idSelector) {
    this.enter(idSelector);
    return true;
  }

  public void leaveIdSelector(CssIdSelectorNode idSelector) {
    this.leave(idSelector);
  }

  public boolean enterPseudoClass(CssPseudoClassNode pseudoClass) {
    this.enter(pseudoClass);
    return true;
  }

  public void leavePseudoClass(CssPseudoClassNode pseudoClass) {
    this.leave(pseudoClass);
  }

  public boolean enterPseudoElement(CssPseudoElementNode pseudoElement) {
    this.enter(pseudoElement);
    return true;
  }

  public void leavePseudoElement(CssPseudoElementNode pseudoElement) {
    this.leave(pseudoElement);
  }

  public boolean enterAttributeSelector(CssAttributeSelectorNode attributeSelector) {
    this.enter(attributeSelector);
    return true;
  }

  public void leaveAttributeSelector(CssAttributeSelectorNode attributeSelector) {
    this.leave(attributeSelector);
  }

  public boolean enterPropertyValue(CssPropertyValueNode propertyValue) {
    this.enter(propertyValue);
    return true;
  }

  public void leavePropertyValue(CssPropertyValueNode propertyValue) {
    this.leave(propertyValue);
  }

  public boolean enterCompositeValueNode(CssCompositeValueNode value) {
    this.enter(value);
    return true;
  }

  public void leaveCompositeValueNode(CssCompositeValueNode value) {
    this.leave(value);
  }

  public boolean enterCompositeValueNodeOperator(CssCompositeValueNode parent) {
    return true;
  }

  public void leaveCompositeValueNodeOperator(CssCompositeValueNode parent) {}

  public boolean enterFunctionNode(CssFunctionNode value) {
    this.enter(value);
    return true;
  }

  public void leaveFunctionNode(CssFunctionNode value) {
    this.leave(value);
  }

  public boolean enterArgumentNode(CssValueNode value) {
    this.enter(value);
    return true;
  }

  public void leaveArgumentNode(CssValueNode value) {
    this.leave(value);
  }

  public boolean enterCombinator(CssCombinatorNode combinator) {
    this.enter(combinator);
    return true;
  }

  public void leaveCombinator(CssCombinatorNode combinator) {
    this.leave(combinator);
  }

  public boolean enterKey(CssKeyNode key) {
    this.enter(key);
    return true;
  }

  public void leaveKey(CssKeyNode key) {
    this.leave(key);
  }

  public boolean enterKeyBlock(CssKeyListNode block) {
    this.enter(block);
    return true;
  }

  public void leaveKeyBlock(CssKeyListNode block) {
    this.leave(block);
  }

  public boolean enterKeyframeRuleset(CssKeyframeRulesetNode key) {
    this.enter(key);
    return true;
  }

  public void leaveKeyframeRuleset(CssKeyframeRulesetNode key) {
    this.leave(key);
  }

  public boolean enterValueNode(CssValueNode n) {
    this.enter(n);
    return true;
  }

  public void leaveValueNode(CssValueNode n) {
    this.leave(n);
  }

  public boolean enterForLoop(CssForLoopRuleNode node) {
    this.enter(node);
    return true;
  }

  public void leaveForLoop(CssForLoopRuleNode node) {
    this.leave(node);
  }

  public void visit(CssNode node) {}

  public boolean enterProvideNode(CssProvideNode node) {
    this.enter(node);
    return true;
  }

  public void leaveProvideNode(CssProvideNode node) {
    this.leave(node);
  }

  public boolean enterRequireNode(CssRequireNode node) {
    this.enter(node);
    return true;
  }

  public void leaveRequireNode(CssRequireNode node) {
    this.leave(node);
  }
}
