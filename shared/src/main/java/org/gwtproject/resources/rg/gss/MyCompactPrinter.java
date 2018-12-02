package org.gwtproject.resources.rg.gss;

import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.VisitController;
import com.google.common.css.compiler.passes.CodeBuffer;
import com.google.common.css.compiler.passes.GssSourceMapGenerator;
import com.google.common.css.compiler.passes.NullGssSourceMapGenerator;
import com.google.common.css.compiler.passes.UniformVisitor;

import javax.annotation.Nullable;

/**
 * @author Dmitrii Tikhomirov
 * Created by treblereel 12/2/18
 */
public abstract class CompactPrinter extends UniformVisitor {

    protected final VisitController visitController;
    protected final CodeBuffer buffer;
    private final GssSourceMapGenerator generator;

    protected CodePrinter(VisitController visitController, @Nullable CodeBuffer buffer, @Nullable GssSourceMapGenerator generator) {
        this.visitController = visitController;
        this.buffer = buffer != null ? buffer : new CodeBuffer();
        this.generator = (GssSourceMapGenerator)(generator != null ? generator : new NullGssSourceMapGenerator());
    }

    protected CodePrinter(VisitController visitController, CodeBuffer buffer) {
        this((VisitController)visitController, buffer, (GssSourceMapGenerator)null);
    }

    protected CodePrinter(VisitController visitController) {
        this((VisitController)visitController, (CodeBuffer)null);
    }

    protected CodePrinter(CssNode subtree, CodeBuffer buffer, GssSourceMapGenerator generator) {
        this(subtree.getVisitController(), buffer, generator);
    }

    protected CodePrinter(CssNode subtree, CodeBuffer buffer) {
        this((VisitController)subtree.getVisitController(), buffer, (GssSourceMapGenerator)null);
    }

    protected CodePrinter(CssNode subtree) {
        this((VisitController)subtree.getVisitController(), (CodeBuffer)null);
    }

    protected CodePrinter(CssTree tree, CodeBuffer buffer, GssSourceMapGenerator generator) {
        this(tree.getVisitController(), buffer, generator);
    }

    protected CodePrinter(CssTree tree, CodeBuffer buffer) {
        this((VisitController)tree.getVisitController(), buffer, (GssSourceMapGenerator)null);
    }

    protected CodePrinter(CssTree tree) {
        this((VisitController)tree.getVisitController(), (CodeBuffer)null);
    }

    public void enter(CssNode node) {
        this.generator.startSourceMapping(node, this.buffer.getNextLineIndex(), this.buffer.getNextCharIndex());
    }

    public void leave(CssNode node) {
        this.generator.endSourceMapping(node, this.buffer.getLastLineIndex(), this.buffer.getLastCharIndex());
    }

    protected void resetBuffer() {
        this.buffer.reset();
    }

    protected String getOutputBuffer() {
        return this.buffer.getOutput();
    }
}
