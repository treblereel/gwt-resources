package org.gwtproject.resources.rg.gss;

import com.google.common.css.compiler.ast.CssNode;
import com.google.common.css.compiler.ast.CssTree;
import com.google.common.css.compiler.ast.VisitController;
import com.google.common.css.compiler.passes.CodeBuffer;
import com.google.common.css.compiler.passes.GssSourceMapGenerator;
import com.google.common.css.compiler.passes.NullGssSourceMapGenerator;

import javax.annotation.Nullable;

/**
 * @author Dmitrii Tikhomirov
 * Created by treblereel 12/5/18
 */
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

    protected GssCodePrinter(VisitController visitController, @Nullable CodeBuffer buffer, @Nullable GssSourceMapGenerator generator) {
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

