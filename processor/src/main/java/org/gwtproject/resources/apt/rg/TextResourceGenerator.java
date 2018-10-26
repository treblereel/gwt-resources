package org.gwtproject.resources.apt.rg;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.gwtproject.resources.apt.ClientBundleGeneratorContext;
import org.gwtproject.resources.apt.exceptions.UnableToCompleteException;
import org.gwtproject.resources.client.TextResource;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

/**
 * @author Dmitrii Tikhomirov
 * Created by treblereel on 10/18/18.
 */
public final class TextResourceGenerator extends AbstractResourceGenerator {
    private final ClassName textResourceClassName = ClassName.get("org.gwtproject.resources.client", "TextResource");

    /**
     * Java compiler has a limit of 2^16 bytes for encoding string constants in a
     * class file. Since the max size of a character is 4 bytes, we'll limit the
     * number of characters to (2e^14 - 1) to fit within one record.
     */
    private static final int MAX_STRING_CHUNK = 16383;

    public TextResourceGenerator(ClientBundleGeneratorContext context, Element clazz, TypeSpec.Builder builder) {
        super(context, clazz, builder);
    }

    @Override
    void process() throws UnableToCompleteException {
        for (Element method : scan(TextResource.class)) {
            addMethodBody(method);
            addMethodInitializer(method);
            addGetResourceMethod(method);
        }
    }

    private void addMethodBody(Element method) throws UnableToCompleteException {
        clazzBuilder.addField(FieldSpec.builder(textResourceClassName, method.getSimpleName().toString(), Modifier.PRIVATE, Modifier.STATIC).build());

        MethodSpec.Builder initializer = generateTextResourceMethodImpl(method);
        initializer.endControlFlow()
                .beginControlFlow("public String getName()")
                .addStatement("return $S", method.getSimpleName())
                .endControlFlow()
                .endControlFlow()
                .addCode(";");
        clazzBuilder.addMethod(initializer.build());
    }

    private MethodSpec.Builder generateTextResourceMethodImpl(Element method) throws UnableToCompleteException {
        MethodSpec.Builder initializer = MethodSpec.methodBuilder(method.getSimpleName().toString() + "Initializer")
                .addModifiers(Modifier.PRIVATE)
                .returns(void.class)
                .beginControlFlow(method.getSimpleName().toString() + " = new " + context.typeUtils.asElement(getReturnTypeMirror(method)).getSimpleName() + "()")
                .beginControlFlow("public String getText()");
        generateContent(initializer, method);
        return initializer;
    }

    private void generateContent(MethodSpec.Builder builder, Element method) throws UnableToCompleteException {
        StringBuffer result = readTextInputStream(getResource(method));
        if (result.length() > MAX_STRING_CHUNK) {
            writeLongString(builder, result.toString());
        } else {
            builder.addStatement("return $S", escape(result.toString()));
        }
    }

    private void writeLongString(MethodSpec.Builder builder, String toWrite) {
        builder.addStatement("$T builder = new $T();", StringBuilder.class, StringBuilder.class);
        int offset = 0;
        int length = toWrite.length();
        while (offset < length - 1) {
            int subLength = Math.min(MAX_STRING_CHUNK, length - offset);
            builder.addStatement("builder.append($S)", escape(toWrite.substring(offset, offset + subLength)));
            offset += subLength;
        }
        builder.addStatement("return builder.toString()");
    }
}
