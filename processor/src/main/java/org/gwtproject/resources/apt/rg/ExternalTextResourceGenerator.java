package org.gwtproject.resources.apt.rg;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.gwtproject.resources.apt.ClientBundleGeneratorContext;
import org.gwtproject.resources.apt.exceptions.UnableToCompleteException;
import org.gwtproject.resources.apt.resource.impl.FakeResource;
import org.gwtproject.resources.client.ExternalTextResource;
import org.json.JSONArray;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Dmitrii Tikhomirov
 * Created by treblereel on 10/18/18.
 */
public final class ExternalTextResourceGenerator extends StaticResourceGenerator {
    private final ClassName externalTextResourceClassName = ClassName.get("org.gwtproject.resources.client", "ExternalTextResource");
    private final ClassName externalTextResourcePrototypeClassName = ClassName.get("org.gwtproject.resources.client.impl", "ExternalTextResourcePrototype");
    private final ClassName textResourceClassName = ClassName.get("org.gwtproject.resources.client", "TextResource");
    static final String JSONP_CALLBACK_PREFIX = "__gwt_jsonp__.P";
    private boolean shouldUseJsonp = false;
    private String md5Sum;

    private Set<Element> externalTextResources = new LinkedHashSet<>();

    public ExternalTextResourceGenerator(ClientBundleGeneratorContext context, Element clazz, TypeSpec.Builder builder) {
        super(context, clazz, builder);
    }

    @Override
    void process() throws UnableToCompleteException {
        for (Element method : scan(ExternalTextResource.class)) {
            externalTextResources.add(method);
        }
        addExternalTextResourcesToClass();
        for (Element method : externalTextResources) {
            addMethodBody(method);
            addMethodInitializer(method);
            addGetResourceMethod(method);
        }


    }

    int counter = 0;

    private void addMethodBody(Element method) throws UnableToCompleteException {
        externalTextResources.add(method);
        clazzBuilder.addField(FieldSpec.builder(externalTextResourceClassName, method.getSimpleName().toString(), Modifier.PRIVATE, Modifier.STATIC).build());
        clazzBuilder.addMethod(addExternalTextResourceMethodImpl(method, counter++).build());
    }

    private MethodSpec.Builder addExternalTextResourceMethodImpl(Element method, int step) {
        MethodSpec.Builder initializer = MethodSpec.methodBuilder(method.getSimpleName().toString() + "Initializer")
                .addModifiers(Modifier.PRIVATE)
                .returns(void.class);
        if (shouldUseJsonp) {
            theStatementIfShouldUseJsonp(initializer, method, step);
        } else {
            theStatement(initializer, method, step);
        }
        return initializer;
    }

    private void theStatementIfShouldUseJsonp(MethodSpec.Builder initializer, Element method, int step) {
        initializer.addStatement(method.getSimpleName().toString() + " = new " + externalTextResourcePrototypeClassName + "($S,$L,$L,$L,$S)",
                method.getSimpleName().toString(),
                "org.gwtproject.safehtml.shared.UriUtils.fromTrustedString(externalTextUrl)",
                "externalTextCache",
                step, md5Sum);
    }

    private void theStatement(MethodSpec.Builder initializer, Element method, int step) {
        initializer.addStatement(method.getSimpleName().toString() + " = new " + externalTextResourcePrototypeClassName + "($S,$L,$L,$L)",
                method.getSimpleName().toString(),
                "org.gwtproject.safehtml.shared.UriUtils.fromTrustedString(externalTextUrl)",
                "externalTextCache",
                step);
    }


    private void addExternalTextResourcesToClass() throws UnableToCompleteException {
        if (externalTextResources.size() > 0) {
            JSONArray array = new JSONArray();
            for (Element externalTextResource : externalTextResources) {
                array.put(readTextInputStream(getResource(externalTextResource)));
            }
            String result = array.toString();
            md5Sum = getMD5Signature(result);
            StringBuffer wrappedData = new StringBuffer();
            if (shouldUseJsonp) {
                wrappedData.append(JSONP_CALLBACK_PREFIX);
                wrappedData.append(md5Sum);
                wrappedData.append(".onSuccess(\n");
                wrappedData.append(result);
                wrappedData.append(")");
            } else {
                wrappedData.append(result);
            }
            String filename = md5Sum + ".txt";
            String outputUrlExpression = deploy(new FakeResource(filename, wrappedData.toString().getBytes()), "text/plain", true);
            addExternalTextResourceInitializers(outputUrlExpression);
        }
    }

    private void addExternalTextResourceInitializers(String filename) {
        clazzBuilder.addField(FieldSpec.builder(String.class, "externalTextUrl", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("$L", filename).build());

        clazzBuilder.addField(FieldSpec.builder(ArrayTypeName.of(textResourceClassName), "externalTextCache", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer(" new $T[$L]", textResourceClassName, externalTextResources.size()).build());
    }
}