package org.gwtproject.resources.apt.rg;

import com.google.auto.common.MoreTypes;
import com.google.common.io.BaseEncoding;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.apache.commons.codec.binary.Base64;
import org.gwtproject.resources.apt.ClientBundleGeneratorContext;
import org.gwtproject.resources.apt.exceptions.UnableToCompleteException;
import org.gwtproject.resources.apt.resource.Resource;
import org.gwtproject.resources.apt.resource.ResourceOracle;
import org.gwtproject.resources.apt.resource.impl.ResourceOracleImpl;
import org.gwtproject.resources.client.ClientBundle;
import org.gwtproject.resources.client.DefaultExtensions;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Dmitrii Tikhomirov <chani@me.com>
 * Created by treblereel on 10/18/18.
 */
public abstract class AbstractResourceGenerator {
    protected final ClientBundleGeneratorContext context;
    protected final Element clazz;
    protected TypeSpec.Builder clazzBuilder;
    private final ResourceOracle resourceOracle = new ResourceOracleImpl();

    protected final String _INSTANCE0 = "_instance0";
    private MessageDigest messageDigest = null;


    AbstractResourceGenerator(ClientBundleGeneratorContext context, Element clazz, TypeSpec.Builder builder) {
        this.context = context;
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
        PackageElement pkg = context.elementUtils.getPackageOf(clazz);
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
        return getMD5Signature(result.getBytes());
    }

    protected String getMD5Signature(byte[] result) {
        getMessageDigest().update(result);
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
                Element type = context.typeUtils.asElement(getReturnTypeMirror(method));
                DefaultExtensions extensions = type.getAnnotation(DefaultExtensions.class);
                String[] paths = new String[extensions.value().length];
                for (int i = 0; i < extensions.value().length; i++) {
                    StringBuffer sb = new StringBuffer();
                    sb.append(method.getSimpleName().toString()).append(extensions.value()[i]);
                    paths[i] = sb.toString();
                }
                return resourceOracle.getResource(getPackageName(), paths);
            }
        } catch (UnableToCompleteException e) {
            throw new UnableToCompleteException("Unable to find resource for " + clazz + ", method " + method);
        }
    }

    public StringBuffer readTextInputStream(Resource resource) {
        StringBuffer sb = new StringBuffer();
        try (InputStream is = resource.openContents()) {
            int c;
            while ((c = is.read()) != -1) {
                sb.append((char) c);
            }
        } catch (IOException | UnableToCompleteException e) {
            throw new Error(e);
        }
        return sb;
    }

    protected String guessContentType(byte[] content) {
        try {
            String type = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(content));
            return type != null ? type : "unknown";
        } catch (IOException e) {
            throw new Error(e.getMessage());
        }
    }

    protected String guessContentTypeFromResource(Resource resource) {
        try {
            return guessContentType(readByteArrayFromResource(resource));
        } catch (UnableToCompleteException e) {
            throw new Error(e.getMessage());
        }
    }

    protected byte[] readByteArrayFromResource(Resource resource) throws UnableToCompleteException {
        try {
            return org.apache.commons.io.IOUtils.toByteArray(resource.openContents());
        } catch (IOException e) {
            throw new Error(e);
        }
    }

    protected static String toBase64(byte[] data) {
        return BaseEncoding.base64().encode(data).replaceAll("\\s+", "");
    }

    public String readBase64InputStream(Resource resource) {
        try {
            return Base64.encodeBase64String(readByteArrayFromResource(resource));
        } catch (UnableToCompleteException e) {
            e.printStackTrace();
            throw new Error(e);
        }
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
