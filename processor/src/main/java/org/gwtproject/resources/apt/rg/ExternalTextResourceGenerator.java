package org.gwtproject.resources.apt.rg;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.gwtproject.resources.apt.exceptions.UnableToCompleteException;
import org.gwtproject.resources.client.ExternalTextResource;
import org.json.JSONArray;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Dmitrii Tikhomirov
 * Created by treblereel on 10/18/18.
 */
public final class ExternalTextResourceGenerator extends AbstractResourceGenerator {
    private final ClassName externalTextResourceClassName = ClassName.get("org.gwtproject.resources.client", "ExternalTextResource");
    private final ClassName externalTextResourcePrototypeClassName = ClassName.get("org.gwtproject.resources.client", "ExternalTextResourcePrototype");
    private final ClassName textResourceClassName = ClassName.get("org.gwtproject.resources.client", "TextResource");
    private final ClassName domGlobalClassName = ClassName.get("elemental2.dom", "DomGlobal");

    private Set<Element> externalTextResources = new LinkedHashSet<>();

    private File gwtCacheDir;

    public ExternalTextResourceGenerator(ProcessingEnvironment processingEnv, Element clazz, TypeSpec.Builder builder) {
        super(processingEnv, clazz, builder);
    }

    @Override
    void process() throws UnableToCompleteException {
        gwtCacheLocation();

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

    private File gwtCacheLocation() throws UnableToCompleteException {
        FileObject fileObject = null;
        Path path = null;
        try {
            fileObject = processingEnv.getFiler().createResource(StandardLocation.SOURCE_OUTPUT, "", "dummy" + System.currentTimeMillis());
            path = Paths.get(fileObject.toUri())
                    .getParent()  // {PROJECT_ROOT}/target/generated-sources/annotations
                    .getParent()  // {PROJECT_ROOT}/target/generated-sources
                    .getParent()  // {PROJECT_ROOT}/target
                    .getParent(); // {PROJECT_ROOT}
        } catch (IOException e) {
            throw new UnableToCompleteException("unable to locate gwt cache folder ");
        }
        String cacheLocation = System.getProperty("gwt.clientbundle.gwt-cache.location");
        if(cacheLocation != null){
            return this.gwtCacheDir = new File(path.toUri().getPath() + cacheLocation);
        }
        return this.gwtCacheDir = new File(path.toUri().getPath() + GWT_CACHE_LOCATION);
    }

    private void addExternalTextResourcesToClass() throws UnableToCompleteException {
        if (externalTextResources.size() > 0) {
            JSONArray array = new JSONArray();
            for (Element externalTextResource : externalTextResources) {
                array.put(readInputStream(getResource(externalTextResource)));
            }
            String result = array.toString();
            String filename = getMD5Signature(result) + ".cache.txt";
            addExternalTextResourceInitializers(filename);
            addExternalTextResourceToWebApp(result, filename);
        }
    }

    private void addExternalTextResourceInitializers(String filename) {
        clazzBuilder.addField(FieldSpec.builder(String.class, "externalTextUrl", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("$T.window.location.getOrigin() + $S", domGlobalClassName, "/gwt-cache/" + filename).build());
        clazzBuilder.addField(FieldSpec.builder(ArrayTypeName.of(textResourceClassName), "externalTextCache", Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer(" new $T[$L]", textResourceClassName, externalTextResources.size()).build());
    }

    private void addExternalTextResourceToWebApp(String result, String filename) throws UnableToCompleteException {
        if (!gwtCacheDir.exists()) {
            gwtCacheDir.mkdir();
        }
        try (FileWriter fw = new FileWriter(new File(gwtCacheDir, filename))) {
            fw.append(result);
        } catch (IOException ioe) {
            throw new UnableToCompleteException(ioe.getMessage());
        }
    }
}