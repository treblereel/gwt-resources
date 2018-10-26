package org.gwtproject.resources.apt.rg;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.gwtproject.resources.apt.ClientBundleGeneratorContext;
import org.gwtproject.resources.apt.exceptions.UnableToCompleteException;
import org.gwtproject.resources.client.ExternalTextResource;
import org.json.JSONArray;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Dmitrii Tikhomirov
 * Created by treblereel on 10/18/18.
 */
public final class ExternalTextResourceGenerator extends FileResourceGenerator {
    private final ClassName externalTextResourceClassName = ClassName.get("org.gwtproject.resources.client", "ExternalTextResource");
    private final ClassName externalTextResourcePrototypeClassName = ClassName.get("org.gwtproject.resources.client", "ExternalTextResourcePrototype");
    private final ClassName textResourceClassName = ClassName.get("org.gwtproject.resources.client", "TextResource");

    private Set<Element> externalTextResources = new LinkedHashSet<>();

    public ExternalTextResourceGenerator(ClientBundleGeneratorContext context, Element clazz, TypeSpec.Builder builder) {
        super(context, clazz, builder);
    }

    @Override
    void process() throws UnableToCompleteException {
        for (Element method : scan(ExternalTextResource.class)) {
            addMethodBody(method);
            addMethodInitializer(method);
            addGetResourceMethod(method);
        }

        addExternalTextResourcesToClass();
    }


    private void addMethodBody(Element method) throws UnableToCompleteException {
        externalTextResources.add(method);
        clazzBuilder.addField(FieldSpec.builder(externalTextResourceClassName, method.getSimpleName().toString(), Modifier.PRIVATE, Modifier.STATIC).build());
        clazzBuilder.addMethod(addExternalTextResourceMethodImpl(method, externalTextResources.size() - 1).build());
    }

    private MethodSpec.Builder addExternalTextResourceMethodImpl(Element method, int step) {
        MethodSpec.Builder initializer = MethodSpec.methodBuilder(method.getSimpleName().toString() + "Initializer")
                .addModifiers(Modifier.PRIVATE)
                .returns(void.class)
                .addStatement(method.getSimpleName().toString() + " = new " + externalTextResourcePrototypeClassName + "($S,$L,$L,$L)",
                        method.getSimpleName().toString(),
                        "org.gwtproject.safehtml.shared.UriUtils.fromTrustedString(externalTextUrl)",
                        "externalTextCache",
                        step);
        return initializer;
    }

    private void addExternalTextResourcesToClass() throws UnableToCompleteException {
        if (externalTextResources.size() > 0) {
            JSONArray array = new JSONArray();
            for (Element externalTextResource : externalTextResources) {
                array.put(readTextInputStream(getResource(externalTextResource)));
            }
            String result = array.toString();
            String filename = getMD5Signature(result) + ".cache.txt";
            addExternalTextResourceInitializers(filename);
            writeFileToDisk(result, filename);
        }
    }

    private void addExternalTextResourceInitializers(String filename) {

        clazzBuilder.addField(FieldSpec.builder(String.class, "externalTextUrl", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("$S", context.gwtCacheUrl + filename).build());

        clazzBuilder.addField(FieldSpec.builder(ArrayTypeName.of(textResourceClassName), "externalTextCache", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer(" new $T[$L]", textResourceClassName, externalTextResources.size()).build());
    }
}