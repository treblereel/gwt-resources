package org.gwtproject.resources.apt.rg;

import com.google.auto.common.MoreTypes;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.gwtproject.resources.apt.exceptions.UnableToCompleteException;
import org.gwtproject.resources.apt.resource.Resource;
import org.gwtproject.resources.apt.resource.ResourceOracle;
import org.gwtproject.resources.apt.resource.impl.ResourceOracleImpl;
import org.gwtproject.resources.client.ClientBundle;
import org.gwtproject.resources.client.DefaultExtensions;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Dmitrii Tikhomirov <chani@me.com>
 * Created by treblereel on 10/18/18.
 */
public abstract class AbstractResourceGenerator {
    protected final ProcessingEnvironment processingEnv;
    protected final Element clazz;
    protected TypeSpec.Builder clazzBuilder;
    private final ResourceOracle resourceOracle = new ResourceOracleImpl();

    protected final String _INSTANCE0 = "_instance0";
    final String GWT_CACHE_LOCATION = "src/main/webapp/gwt-cache";
    private MessageDigest messageDigest = null;


    AbstractResourceGenerator(ProcessingEnvironment processingEnv, Element clazz, TypeSpec.Builder builder) {
        this.processingEnv = processingEnv;
        this.clazz = clazz;
        this.clazzBuilder = builder;
    }

    protected MessageDigest getMessageDigest() {
        try {
            if (messageDigest == null) {
                this.messageDigest = MessageDigest.getInstance("MD5");
            }
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return messageDigest;
    }

    protected String getPackageName() {
        PackageElement pkg = processingEnv.getElementUtils().getPackageOf(clazz);
        return pkg.getQualifiedName().toString();
    }

    protected String getClassName() {
        return clazz.getSimpleName().toString();
    }

    protected String getEnclosingClassName() {
        return clazz.toString().replace(getPackageName() + ".", "");
    }

    abstract void process() throws UnableToCompleteException;

    protected Set<Element> scan() {
        return clazz.getEnclosedElements().stream().collect(Collectors.toSet());
    }

    protected Set<Element> scan(Class returnClass) {
        return scan().stream().filter(method -> MoreTypes.isTypeOf(returnClass, getReturnTypeMirror(method))).collect(Collectors.toSet());
    }

    protected TypeMirror getReturnTypeMirror(Element method) {
        return ((ExecutableType) method.asType()).getReturnType();
    }

    protected void addMethodInitializer(Element method) {
        clazzBuilder.addType(TypeSpec.classBuilder(method.getSimpleName().toString() + "Initializer")
                .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                .addMethod(MethodSpec.methodBuilder("get")
                        .addModifiers(Modifier.STATIC)
                        .addStatement("return $L", method.getSimpleName().toString())
                        .returns(ClassName.get(getReturnTypeMirror(method))).build())
                .addStaticBlock(CodeBlock.of("$L.$L", _INSTANCE0, method.getSimpleName().toString() + "Initializer();").toBuilder().build()).build());
    }

    protected String getMD5Signature(String result) {
        getMessageDigest().update(result.getBytes());
        byte[] digest = messageDigest.digest();
        String hash = DatatypeConverter.printHexBinary(digest).toUpperCase();
        return hash;
    }

    protected void addGetResourceMethod(Element method) {
        clazzBuilder.addMethod(MethodSpec.methodBuilder(method.getSimpleName().toString()).addModifiers(Modifier.PUBLIC)
                .returns(ClassName.get(getReturnTypeMirror(method)))
                .addCode(CodeBlock.of("return $L.$L();", method.getSimpleName().toString() + "Initializer", "get")).build());
    }

    protected Resource getResource(Element method) throws UnableToCompleteException {
        ClientBundle.Source source = method.getAnnotation(ClientBundle.Source.class);
        try {
            if (source != null) {
                return resourceOracle.getResource(getPackageName(), source.value());
            } else {
                Element type = processingEnv.getTypeUtils().asElement(getReturnTypeMirror(method));
                DefaultExtensions extensions = type.getAnnotation(DefaultExtensions.class);
                for (String ext : extensions.value()) {
                    StringBuffer sb = new StringBuffer();
                    sb.append(method.getSimpleName().toString()).append(ext);
                    Resource resource = resourceOracle.getResource(getPackageName(), sb.toString());
                    if (resource != null) {
                        return resource;
                    }
                }
            }
        } catch (UnableToCompleteException e) {
            throw new UnableToCompleteException("Unable to find resource for " + clazz + ", method " + method);
        }
        throw new UnableToCompleteException("Unable to find resource for " + clazz + ", method " + method);
    }

    protected StringBuffer readInputStream(Resource resource) {
        StringBuffer sb = new StringBuffer();
        try {
            int c;
            InputStream is = resource.openContents();
            while ((c = is.read()) != -1) {
                sb.append((char) c);
            }
        } catch (IOException | UnableToCompleteException e) {
            throw new Error(e);
        }
        return sb;
    }

    /**
     * Escapes string content to be a valid string literal.
     *
     * @return an escaped version of <code>unescaped</code>, suitable for being enclosed in double
     * quotes in Java source
     */
    public static String escape(String unescaped) {
        int extra = 0;
        for (int in = 0, n = unescaped.length(); in < n; ++in) {
            switch (unescaped.charAt(in)) {
                case '\0':
                case '\n':
                case '\r':
                case '\"':
                case '\\':
                    ++extra;
                    break;
            }
        }

        if (extra == 0) {
            return unescaped;
        }

        char[] oldChars = unescaped.toCharArray();
        char[] newChars = new char[oldChars.length + extra];
        for (int in = 0, out = 0, n = oldChars.length; in < n; ++in, ++out) {
            char c = oldChars[in];
            switch (c) {
                case '\0':
                    newChars[out++] = '\\';
                    c = '0';
                    break;
                case '\n':
                    newChars[out++] = '\\';
                    c = 'n';
                    break;
                case '\r':
                    newChars[out++] = '\\';
                    c = 'r';
                    break;
                case '\"':
                    newChars[out++] = '\\';
                    c = '"';
                    break;
                case '\\':
                    newChars[out++] = '\\';
                    c = '\\';
                    break;
            }
            newChars[out] = c;
        }

        return String.valueOf(newChars);
    }
}
