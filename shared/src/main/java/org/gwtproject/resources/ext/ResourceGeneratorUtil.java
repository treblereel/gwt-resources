package org.gwtproject.resources.ext;

import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.google.common.io.BaseEncoding;
import org.apache.commons.codec.binary.Base64;
import org.gwtproject.resources.context.AptContext;
import org.gwtproject.resources.rg.resource.impl.ResourceOracleImpl;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import static org.gwtproject.resources.client.ClientBundle.Source;

/**
 * @author Dmitrii Tikhomirov
 * Created by treblereel 11/11/18
 */
public class ResourceGeneratorUtil {

    private static ResourceOracle resourceOracle = new ResourceOracleImpl();

    /**
     * Returns the base filename of a resource. The behavior is similar to the unix
     * command <code>basename</code>.
     *
     * @param resource the URL of the resource
     * @return the final name segment of the resource
     */
    public static String baseName(URL resource) {
        String path = resource.getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    public static String toBase64(byte[] data) {
        return BaseEncoding.base64().encode(data).replaceAll("\\s+", "");
    }

    public static Resource getResource(TreeLogger logger, ExecutableElement method, AptContext context) throws UnableToCompleteException {
        Source source = method.getAnnotation(Source.class);
        Resource resource;
        try {
            if (source != null) {
                resource = resourceOracle.getResource(MoreElements.getPackage(method).getQualifiedName().toString(), source.value());
            } else {
                DefaultExtensions extensions = findAnnotationInTypeHierarchy(logger, (TypeElement) MoreTypes.asElement(method.getReturnType()), DefaultExtensions.class);
                resource = getResourceByExtensions(method, extensions.value());
            }
        } catch (UnableToCompleteException e) {
            logger.log(TreeLogger.Type.ERROR, "Unable to find resource for " + method + " in " + method.getEnclosingElement());
            throw new UnableToCompleteException();
        }
        if (resource != null && resource.getUrl().getFile() != null) {
            return resource;
        }
        logger.log(TreeLogger.Type.ERROR, "Unable to find resource for " + method + " in " + method.getEnclosingElement());
        throw new UnableToCompleteException();
    }

    public static <A extends Annotation> A findAnnotationInTypeHierarchy(TreeLogger logger, TypeElement typeElement,
                                                                         Class annotation) throws UnableToCompleteException {
        if (MoreElements.isAnnotationPresent(typeElement, annotation)) {
            return (A) typeElement.getAnnotation(annotation);
        } else {
            Set<TypeMirror> parents = getAllParents(typeElement);
            for (TypeMirror parent : parents) {
                if (MoreTypes.asElement(parent).getAnnotation(annotation) != null) {
                    return (A) MoreTypes.asElement(parent).getAnnotation(annotation);
                }
            }
        }
        logger.log(TreeLogger.Type.ERROR, "Unable to find annotation " + annotation + " in type hierarchy for " + typeElement);
        throw new UnableToCompleteException();
    }

    public static Resource getResourceByExtensions(ExecutableElement method, String[] extensions) throws UnableToCompleteException {
        String[] paths = new String[extensions.length];
        for (int i = 0; i < extensions.length; i++) {
            StringBuffer sb = new StringBuffer();
            sb.append(method.getSimpleName().toString()).append(extensions[i]);
            paths[i] = sb.toString();
        }
        return resourceOracle.getResource(MoreElements.getPackage(method).getQualifiedName().toString(), paths);
    }

    public static Resource getResourceByExtensions(Element method, String[] extensions) throws UnableToCompleteException {
        String[] paths = new String[extensions.length];
        for (int i = 0; i < extensions.length; i++) {
            StringBuffer sb = new StringBuffer();
            sb.append(method.getSimpleName().toString()).append(extensions[i]);
            paths[i] = sb.toString();
        }
        return resourceOracle.getResource(MoreElements.getPackage(method.getEnclosingElement()).getQualifiedName().toString(), paths);
    }

    /**
     * Find all resources referenced by a method in a bundle. The method's
     * {@link Source} annotation will be examined and the specified locations will
     * be expanded into URLs by which they may be accessed on the local system.
     * <p>
     * This method is sensitive to the <code>locale</code> deferred-binding
     * property and will attempt to use a best-match lookup by removing locale
     * components.
     * <p>
     * The compiler's ResourceOracle will be used to resolve resource locations.
     * If the desired resource cannot be found in the ResourceOracle, this method
     * will fall back to using the current thread's context ClassLoader. If it is
     * necessary to alter the way in which resources are located, use the overload
     * that accepts a ClassLoader.
     * <p>
     * If the method's return type declares the {@link DefaultExtensions}
     * annotation, the value of this annotation will be used to find matching
     * resource names if the method lacks a {@link Source} annotation.
     *
     * @param logger a TreeLogger that will be used to report errors or warnings
     * @param method the method to examine for {@link Source} annotations
     * @return URLs for each {@link Source} annotation value defined on the
     * method.
     * @throws UnableToCompleteException if ore or more of the sources could not
     *                                   be found. The error will be reported via the <code>logger</code>
     *                                   provided to this method
     */
    public static Resource findResource(TreeLogger logger, ExecutableElement method) throws UnableToCompleteException {
        TypeElement returnType = (TypeElement) MoreTypes.asElement(method.getReturnType());
        assert returnType.getKind().isInterface() || returnType.getKind().isClass();
        DefaultExtensions annotation = findDefaultExtensionsInClassHierarcy(returnType);
        String[] extensions;
        if (annotation != null) {
            extensions = annotation.value();
        } else {
            extensions = new String[0];
        }
        return findResource(logger, method, extensions);
    }

    private static DefaultExtensions findDefaultExtensionsInClassHierarcy(TypeElement resourceType) {
        Set<TypeMirror> sets = new HashSet<>();
        getAllParents(resourceType, sets);
        for (TypeMirror e : sets) {
            DefaultExtensions a = MoreTypes.asElement(e).getAnnotation(DefaultExtensions.class);
            if (a != null)
                return a;
        }
        return null;
    }

    /**
     * Main implementation of findResources.
     */
    public static Resource findResource(TreeLogger logger, ExecutableElement method, String[] defaultSuffixes)
            throws UnableToCompleteException {
        logger = logger.branch(TreeLogger.DEBUG, "Finding resources");

        boolean error = false;
        Source resourceAnnotation = method.getAnnotation(Source.class);
        Resource toReturn = null;

        if (resourceAnnotation == null) {
            if (defaultSuffixes != null) {
                return getResourceByExtensions(method, defaultSuffixes);
            }

            logger.log(TreeLogger.ERROR, "No " + Source.class.getName()
                    + " annotation and no resources found with default extensions");
            error = true;

        } else {
            // The user has put an @Source annotation on the accessor method
            String[] resources = resourceAnnotation.value();
            toReturn = resourceOracle.getResource(MoreElements.getPackage(method.getEnclosingElement()).getQualifiedName().toString(), resources);
            if (toReturn == null) {
                error = true;
                logger.log(TreeLogger.ERROR, "Resource for " + method + " in " + method.getEnclosingElement()
                        + " not found. Is the name specified as ClassLoader.getResource()"
                        + " would expect?");
            }
        }

        if (error) {
            throw new UnableToCompleteException();
        }

        return toReturn;
    }

    public static String readInputStreamAsText(Resource resource) {
        StringBuffer sb = new StringBuffer();
        try (InputStream is = resource.openContents()) {
            int c;
            while ((c = is.read()) != -1) {
                sb.append((char) c);
            }
        } catch (IOException | UnableToCompleteException e) {
            e.printStackTrace();

            throw new Error(e);
        }
        return sb.toString();
    }

    public static String guessContentTypeFromResource(Resource resource) {
        try {
            return guessContentType(readByteArrayFromResource(resource));
        } catch (UnableToCompleteException e) {
            throw new Error(e.getMessage());
        }
    }

    public static String guessContentType(byte[] content) {
        try {
            String type = URLConnection.guessContentTypeFromStream(new ByteArrayInputStream(content));
            return type != null ? type : "unknown";
        } catch (IOException e) {
            throw new Error(e.getMessage());
        }
    }

    public static byte[] readByteArrayFromResource(Resource resource) throws UnableToCompleteException {
        try {
            return org.apache.commons.io.IOUtils.toByteArray(resource.openContents());
        } catch (IOException e) {
            throw new UnableToCompleteException(e.getMessage());
        }
    }

    public static String readBase64InputStream(Resource resource) {
        try {
            return Base64.encodeBase64String(readByteArrayFromResource(resource));
        } catch (UnableToCompleteException e) {
            e.printStackTrace();
            throw new Error(e);
        }
    }

    /**
     * Finds a method by following a dotted path interpreted as a series of no-arg
     * method invocations from an instance of a given root type.
     *
     * @param rootType           the type from which the search begins
     * @param pathElements       a sequence of no-arg method names
     * @param expectedReturnType the expected return type of the method to locate,
     *                           or <code>null</code> if no constraint on the return type is
     *                           necessary
     * @return the requested JMethod
     * @throws NotFoundException if the requested method could not be found
     *                           TODO detailed errors
     */
    public static ExecutableElement getMethodByPath(TypeElement rootType, List<String> pathElements, Element expectedReturnType, Types types, Elements elements) throws NotFoundException, UnableToCompleteException {
        if (pathElements.isEmpty()) {
            throw new NotFoundException("No path specified");
        }
        TypeElement currentType = rootType;


        for (String pathElement : pathElements) {
            if (!isClassOrInterface(currentType)) {
                throw new NotFoundException("Cannot resolve member " + pathElement + " on type " + currentType);
            }

            for (ExecutableElement method : MoreElements.getLocalAndInheritedMethods(rootType, types, elements)) {
                if (method.getSimpleName().toString().equals(pathElement)) {
                    return method;
                }

            }
        }
        StringJoiner sj = new StringJoiner(" ");
        pathElements.forEach(sj::add);
        throw new UnableToCompleteException("Cannot resolve member " + sj.toString() + " on type " + currentType);
    }

    public static boolean isClassOrInterface(TypeElement clazz) {
        return clazz.getKind().isClass() || clazz.getKind().isInterface();
    }

    /**
     * Given a user-defined type name, determine the type name for the generated
     * class based on accumulated requirements.
     */
    public static String generateSimpleSourceName(TreeLogger logger, TypeElement element) {
        StringBuilder toReturn = new StringBuilder();
        if (!element.getEnclosingElement().getKind().equals(ElementKind.PACKAGE)) {
            toReturn.append(element.getEnclosingElement().getSimpleName().toString());
        }
        toReturn.append(element.getSimpleName().toString().replaceAll("[.$]", "_"));
        toReturn.append("Impl");
        return toReturn.toString();
    }

    public static Set<TypeMirror> getAllParents(TypeElement candidate) {
        Set<TypeMirror> set = new HashSet<>();
        getAllParents(candidate, set);
        return set;
    }

    private static void getAllParents(TypeElement candidate, Set<TypeMirror> set) {
        candidate.getInterfaces().forEach(e -> {
            set.add(e);
            getAllParents((TypeElement) MoreTypes.asElement(e), set);
        });
    }

    public static URL[] findResources(TreeLogger logger, ExecutableElement method) throws UnableToCompleteException {
        TypeElement returnType = (TypeElement) MoreTypes.asElement(method.getReturnType());
        assert isClassOrInterface(returnType);
        DefaultExtensions annotation = findAnnotationInTypeHierarchy(logger, returnType, DefaultExtensions.class);
        String[] extensions;
        if (annotation != null) {
            extensions = annotation.value();
        } else {
            extensions = new String[0];
        }
        return findResources(logger, method, extensions);
    }

    /**
     * Find all resources referenced by a method in a bundle. The method's
     * {@link Source} annotation will be examined and the specified locations will
     * be expanded into URLs by which they may be accessed on the local system.
     * <p>
     * This method is sensitive to the <code>locale</code> deferred-binding
     * property and will attempt to use a best-match lookup by removing locale
     * components.
     * <p>
     * The compiler's ResourceOracle will be used to resolve resource locations.
     * If the desired resource cannot be found in the ResourceOracle, this method
     * will fall back to using the current thread's context ClassLoader. If it is
     * necessary to alter the way in which resources are located, use the overload
     * that accepts a ClassLoader.
     *
     * @param logger          a TreeLogger that will be used to report errors or warnings
     * @param method          the method to examine for {@link Source} annotations
     * @param defaultSuffixes if the supplied method does not have any
     *                        {@link Source} annotations, act as though a Source annotation was
     *                        specified, using the name of the method and each of supplied
     *                        extensions in the order in which they are specified
     * @return URLs for each {@link Source} annotation value defined on the
     * method.
     * @throws UnableToCompleteException if ore or more of the sources could not
     *                                   be found. The error will be reported via the <code>logger</code>
     *                                   provided to this method
     */
    public static URL[] findResources(TreeLogger logger,
                                      ExecutableElement method, String[] defaultSuffixes)
            throws UnableToCompleteException {
        logger = logger.branch(TreeLogger.DEBUG, "Finding resources");

        //String locale = getLocale(logger, context.getGeneratorContext());
        String locale = null;
        //checkForDeprecatedAnnotations(logger, method);

        boolean error = false;
        Source resourceAnnotation = method.getAnnotation(Source.class);
        PackageElement packageElement = MoreElements.getPackage(method.getEnclosingElement());
        URL[] toReturn;
        if (resourceAnnotation == null) {
            if (defaultSuffixes != null) {
                for (String extension : defaultSuffixes) {
                    if (logger.isLoggable(TreeLogger.SPAM)) {
                        logger.log(TreeLogger.SPAM, "Trying default extension " + extension);
                    }

                    URL resourceUrl = tryFindResource(
                            getPathRelativeToPackage(packageElement,
                                    method.getSimpleName() + extension), locale);
                    // Take the first match
                    if (resourceUrl != null) {
                        return new URL[]{resourceUrl};
                    }
                }
            }

            logger.log(TreeLogger.ERROR, "No " + Source.class.getName()
                    + " annotation and no resources found with default extensions");
            toReturn = null;
            error = true;

        } else {
            // The user has put an @Source annotation on the accessor method
            String[] resources = resourceAnnotation.value();

            toReturn = new URL[resources.length];

            int tagIndex = 0;
            for (String resource : resources) {
                // Try to find the resource relative to the package.
                URL resourceURL = tryFindResource(getPathRelativeToPackage(packageElement,
                        resource), locale);
                /*
                 * If we didn't find the resource relative to the package, assume it
                 * is absolute.
                 */
                if (resourceURL == null) {
                    resourceURL = tryFindResource(resource, locale);
                }

                // If we have found a resource, take the first match
                if (resourceURL != null) {
                    toReturn[tagIndex++] = resourceURL;
                }

                if (resourceURL == null) {
                    error = true;
                    logger.log(TreeLogger.ERROR, "Resource " + resource
                            + " not found. Is the name specified as ClassLoader.getResource()"
                            + " would expect?");
                }
            }
        }

        if (error) {
            throw new UnableToCompleteException();
        }

        return toReturn;
    }

    /**
     * Converts a package relative path into an absolute path.
     *
     * @param pkg  the package
     * @param path a path relative to the package
     * @return an absolute path
     */
    private static String getPathRelativeToPackage(PackageElement pkg, String path) {
        return pkg.toString().replace('.', '/') + '/' + path;
    }

    /**
     * This performs the locale lookup function for a given resource name.
     *
     * @param resourceName the string name of the desired resource
     * @param locale       the locale of the current rebind permutation
     * @return a URL by which the resource can be loaded, <code>null</code> if one
     * cannot be found
     */
    public static URL tryFindResource(String resourceName,
                                      String locale) throws UnableToCompleteException {
        Resource resource = null;

        // Look for locale-specific variants of individual resources
        if (locale != null) {
            // Convert language_country_variant to independent pieces
            String[] localeSegments = locale.split("_");
            int lastDot = resourceName.lastIndexOf(".");
            String prefix = lastDot == -1 ? resourceName : resourceName.substring(0,
                    lastDot);
            String extension = lastDot == -1 ? "" : resourceName.substring(lastDot);

            for (int i = localeSegments.length - 1; i >= -1; i--) {
                String localeInsert = "";
                for (int j = 0; j <= i; j++) {
                    localeInsert += "_" + localeSegments[j];
                }
                resource = resourceOracle.getResource(prefix + localeInsert + extension);
                if (resource != null) {
                    break;
                }
            }
        } else {
            resource = resourceOracle.getResource(resourceName);
        }

        if (resource != null) {
            return resource.getUrl();
        }
        return null;
    }
}