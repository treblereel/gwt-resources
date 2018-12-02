package org.gwtproject.resources.rg;

import org.gwtproject.resources.ext.*;
import org.gwtproject.resources.rg.util.SourceWriter;
import org.gwtproject.resources.rg.util.StringSourceWriter;

import javax.lang.model.element.ExecutableElement;

/**
 * @author Dmitrii Tikhomirov
 * Created by treblereel 11/13/18
 */
public final class TextResourceGenerator extends AbstractResourceGenerator {
    /**
     * Java compiler has a limit of 2^16 bytes for encoding string constants in a
     * class file. Since the max size of a character is 4 bytes, we'll limit the
     * number of characters to (2^14 - 1) to fit within one record.
     */
    private static final int MAX_STRING_CHUNK = 16383;

    @Override
    public String createAssignment(TreeLogger logger, ResourceContext context, AptContext aptContext,
                                   ExecutableElement method) throws UnableToCompleteException {

        Resource resource = ResourceGeneratorUtil.getResource(logger, method, aptContext);
        SourceWriter sw = new StringSourceWriter();
        // Write the expression to create the subtype.
        sw.println("new " + method.getReturnType() + "() {");
        sw.indent();

        if (!AbstractResourceGenerator.STRIP_COMMENTS) {
            // Convenience when examining the generated code.
            sw.println("// " + resource.getUrl().getFile());
        }

        sw.println("public String getText() {");
        sw.indent();

        //String toWrite = Util.readURLAsString(resource);

        //String toWrite = Util.readURLAsString(resource);
        String toWrite = ResourceGeneratorUtil.readInputStreamAsText(resource);

        if (toWrite.length() > MAX_STRING_CHUNK) {
            writeLongString(sw, toWrite);
        } else {
            sw.println("return \"" + Generator.escape(toWrite) + "\";");
        }
        sw.outdent();
        sw.println("}");

        sw.println("public String getName() {");
        sw.indent();
        sw.println("return \"" + method.getSimpleName() + "\";");
        sw.outdent();
        sw.println("}");

        sw.outdent();
        sw.println("}");

        return sw.toString();
    }

    /**
     * A single constant that is too long will crash the compiler with an out of
     * memory error. Break up the constant and generate code that appends using a
     * buffer.
     */
    private void writeLongString(SourceWriter sw, String toWrite) {
        sw.println("StringBuilder builder = new StringBuilder();");
        int offset = 0;
        int length = toWrite.length();
        while (offset < length - 1) {
            int subLength = Math.min(MAX_STRING_CHUNK, length - offset);
            sw.print("builder.append(\"");
            sw.print(Generator.escape(toWrite.substring(offset, offset + subLength)));
            sw.println("\");");
            offset += subLength;
        }
        sw.println("return builder.toString();");
    }
}
