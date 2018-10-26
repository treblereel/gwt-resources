package org.gwtproject.resources.apt.rg;

import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeSpec;
import org.gwtproject.resources.apt.ClientBundleGeneratorContext;
import org.gwtproject.resources.apt.exceptions.UnableToCompleteException;
import org.gwtproject.resources.client.ClientBundleWithLookup;
import org.gwtproject.resources.client.ResourcePrototype;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;
import java.util.StringJoiner;

/**
 * @author Dmitrii Tikhomirov <chani@me.com>
 * Created by treblereel on 10/18/18.
 */
public class ClientBundleClassBuilder extends AbstractResourceGenerator {
    private final RoundEnvironment roundEnvironment;
    private final Set<Class> generators;

    private final String _INSTANCE0 = "_instance0";

    public ClientBundleClassBuilder(ClientBundleGeneratorContext context, RoundEnvironment roundEnvironment, Set<Class> generators, Element clazz) throws UnableToCompleteException {
        super(context, clazz, null);
        if (!clazz.getKind().isInterface()) {
            throw new UnableToCompleteException(clazz + " must be an interface");
        }
        this.roundEnvironment = roundEnvironment;
        this.generators = generators;
    }

    public void process() throws UnableToCompleteException {
        generateClassBody();
        runGenerator();
        maybeAddResourceLookup();
        write();
    }

    private void maybeAddResourceLookup() {
        TypeElement elem = context.elementUtils.getTypeElement(ClientBundleWithLookup.class.getCanonicalName());
        if (context.typeUtils.isSubtype(clazz.asType(), elem.asType())) {
            addResourceMapField();
            addGetResourcesMethod();
            addGetResourceByNameMethod();
        }
    }

    private void addGetResourceByNameMethod() {
        MethodSpec.Builder builder = MethodSpec.methodBuilder("getResource").addModifiers(Modifier.PUBLIC)
                .addParameter(ClassName.get(String.class), "name")
                .returns(ClassName.get(ResourcePrototype.class))
                .beginControlFlow("if (resourceMap == null)")
                .addStatement("resourceMap = new $T()", ParameterizedTypeName.get(ClassName.get(HashMap.class), ClassName.get(String.class), ClassName.get(ResourcePrototype.class)));

        scan().forEach(method -> builder.addStatement("resourceMap.put($S, $L)", method.getSimpleName(), method.toString()));
        builder.endControlFlow();
        builder.addStatement("return resourceMap.get(name)");

        clazzBuilder.addMethod(builder.build());
    }

    private void addGetResourcesMethod() {
        StringJoiner joiner = new StringJoiner(",");
        scan().forEach(method -> joiner.add(method.toString()));
        clazzBuilder.addMethod(MethodSpec.methodBuilder("getResources")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return new $T { " + joiner.toString() + " }", ArrayTypeName.get(ResourcePrototype[].class))
                .returns(ResourcePrototype[].class).build());
    }

    private void addResourceMapField() {
        clazzBuilder.addField(FieldSpec.builder(ParameterizedTypeName.get(ClassName.get(HashMap.class), ClassName.get(String.class), ClassName.get(ResourcePrototype.class)), "resourceMap", Modifier.PRIVATE, Modifier.STATIC).build());
    }

    private void write() throws UnableToCompleteException {
        JavaFile clientBundleFile = JavaFile.builder(getPackageName(), clazzBuilder.build()).build();
        try {
            clientBundleFile.writeTo(context.filer);
        } catch (IOException e) {
            context.messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            throw new UnableToCompleteException(e.getMessage());
        }
    }

    private void runGenerator() throws UnableToCompleteException {
        for (Class generator : generators) {
            try {
                ((AbstractResourceGenerator) generator.getConstructor(ClientBundleGeneratorContext.class, Element.class, TypeSpec.Builder.class).newInstance(context, clazz, clazzBuilder)).process();
            } catch (UnableToCompleteException | InstantiationException | IllegalAccessException | InvocationTargetException| NoSuchMethodException e) {
                throw new UnableToCompleteException(e.getMessage());
            }
        }
    }

    private void generateClassBody() {
        clazzBuilder = TypeSpec.classBuilder(generateClassName());
        setInterface();
        addClassInstanceInitializer();


    }

    private void addClassInstanceInitializer() {
        clazzBuilder.addField(FieldSpec.builder(ClassName.get(getPackageName(), generateClassName()), _INSTANCE0, Modifier.FINAL, Modifier.STATIC)
                .initializer("new $T()", ClassName.get(getPackageName(), generateClassName())).build());
    }

    private void setInterface() {
        ClassName className = ClassName.get(getPackageName(), getEnclosingClassName());
        clazzBuilder.addSuperinterface(className);
    }

    private String generateClassName() {
        StringBuffer sb = new StringBuffer();
        return sb.append(getClassName()).append("Impl").toString();
    }

}
