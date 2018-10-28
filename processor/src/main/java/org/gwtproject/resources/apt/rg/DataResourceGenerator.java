package org.gwtproject.resources.apt.rg;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.gwtproject.resources.apt.ClientBundleGeneratorContext;
import org.gwtproject.resources.apt.exceptions.UnableToCompleteException;
import org.gwtproject.resources.apt.resource.Resource;
import org.gwtproject.resources.client.DataResource;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import java.io.IOException;

import static org.gwtproject.resources.client.DataResource.DoNotEmbed;
import static org.gwtproject.resources.client.DataResource.MimeType;

/**
 * @author Dmitrii Tikhomirov <chani@me.com>
 * Created by treblereel on 10/27/18.
 */
public class DataResourceGenerator extends InlineResourceGenerator {
    private final ClassName dataResourceClassName = ClassName.get("org.gwtproject.resources.client", "DataResource");

    public DataResourceGenerator(ClientBundleGeneratorContext context, Element clazz, TypeSpec.Builder builder) {
        super(context, clazz, builder);
    }

    @Override
    void process() throws UnableToCompleteException {
        for (Element method : scan(DataResource.class)) {

            // Determine if a MIME Type has been specified
            MimeType mimeTypeAnnotation = method.getAnnotation(MimeType.class);
            String mimeType = mimeTypeAnnotation != null ? mimeTypeAnnotation.value() : null;

            // Determine if resource should not be embedded
            DoNotEmbed doNotEmbed = method.getAnnotation(DoNotEmbed.class);
            boolean forceExternal = (doNotEmbed != null);

            Resource resource = getResource(method);

            try {
                mimeType = (mimeType != null) ? mimeType : resource.getUrl().openConnection().getContentType();
            } catch (IOException e) {
                context.messager.printMessage(Diagnostic.Kind.ERROR, "Unable to determine mime type of resource " + resource.getUrl());
                throw new UnableToCompleteException(e.getMessage());
            }
            String outputUrlExpression = deploy(resource, mimeType, forceExternal);

            addMethodBody(method, dataResourceClassName, generateResourceMethodImpl(method, outputUrlExpression));
            addMethodInitializer(method);
            addGetResourceMethod(method);
        }
    }

    private MethodSpec.Builder generateResourceMethodImpl(Element method, String outputUrlExpression) throws UnableToCompleteException {
        MethodSpec.Builder initializer = MethodSpec.methodBuilder(method.getSimpleName().toString() + "Initializer")
                .addModifiers(Modifier.PRIVATE)
                .returns(void.class)
                .addStatement(" $L = new org.gwtproject.resources.client.impl.DataResourcePrototype($S,org.gwtproject.safehtml.shared.UriUtils.fromTrustedString($L))",
                        method.getSimpleName().toString(),
                        method.getSimpleName().toString(),
                        outputUrlExpression);
        return initializer;
    }
}