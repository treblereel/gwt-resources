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

import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.VisitController;
import com.google.common.css.compiler.passes.CodeBuffer;
import com.google.common.css.compiler.passes.GssSourceMapGenerator;
import com.google.common.css.compiler.passes.NullGssSourceMapGenerator;
import javax.annotation.Nullable;

/** @author Dmitrii Tikhomirov Created by treblereel 12/5/18 */
public class GssCodePrinter extends GssUniformVisitor {
  protected final VisitController visitController;
  protected final CodeBuffer buffer;
  private final GssSourceMapGenerator generator;

  protected GssCodePrinter(VisitController visitController) {
    this(visitController, null);
  }

  protected GssCodePrinter(VisitController visitController, CodeBuffer buffer) {
    this(visitController, buffer, null);
  }

  protected GssCodePrinter(
      VisitController visitController,
      @Nullable CodeBuffer buffer,
      @Nullable GssSourceMapGenerator generator) {
    this.visitController = visitController;
    this.buffer = buffer != null ? buffer : new CodeBuffer();
    this.generator = (generator != null ? generator : new NullGssSourceMapGenerator());
  }

  protected GssCodePrinter(CssNode subtree, CodeBuffer buffer, GssSourceMapGenerator generator) {
    this(subtree.getVisitController(), buffer, generator);
  }

  protected GssCodePrinter(CssNode subtree, CodeBuffer buffer) {
    this(subtree.getVisitController(), buffer, null);
  }

  protected GssCodePrinter(CssNode subtree) {
    this(subtree.getVisitController(), null);
  }

  protected GssCodePrinter(CssTree tree, CodeBuffer buffer, GssSourceMapGenerator generator) {
    this(tree.getVisitController(), buffer, generator);
  }

  protected GssCodePrinter(CssTree tree, CodeBuffer buffer) {
    this(tree.getVisitController(), buffer, null);
  }

  protected GssCodePrinter(CssTree tree) {
    this(tree.getVisitController(), null);
  }

  public void enter(CssNode node) {
    this.generator.startSourceMapping(
        node, this.buffer.getNextLineIndex(), this.buffer.getNextCharIndex());
  }

  public void leave(CssNode node) {
    this.generator.endSourceMapping(
        node, this.buffer.getLastLineIndex(), this.buffer.getLastCharIndex());
  }

  protected void resetBuffer() {
    this.buffer.reset();
  }

  protected String getOutputBuffer() {
    return this.buffer.getOutput();
  }
}
