package org.gwtproject.resources.rg.resource.impl;

import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import org.gwtproject.resources.context.AptContext;
import org.gwtproject.resources.ext.DefaultExtensions;
import org.gwtproject.resources.ext.ResourceGeneratorUtil;
import org.gwtproject.resources.ext.ResourceOracle;
import org.gwtproject.resources.ext.TreeLogger;
import org.gwtproject.resources.ext.UnableToCompleteException;

import javax.annotation.processing.FilerException;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.gwtproject.resources.client.ClientBundle.Source;

/**
 * @author Dmitrii Tikhomirov <chani@me.com>
 * Created by treblereel on 10/8/18.
 */
public class ResourceOracleImpl implements ResourceOracle {
    private final AptContext aptContext;

    public ResourceOracleImpl(AptContext context) {
        this.aptContext = context;
    }

    private URL[] getResourcesByExtensions(ExecutableElement method, String[] extensions) throws UnableToCompleteException {
        String[] paths = new String[extensions.length];
        for (int i = 0; i < extensions.length; i++) {
            StringBuffer sb = new StringBuffer();
            sb.append(method.getSimpleName().toString()).append(extensions[i]);
            paths[i] = sb.toString();
        }
        return findResources(MoreElements.getPackage(method).getQualifiedName().toString(), paths);
    }

    @Override
    public URL[] findResources(CharSequence packageName, CharSequence[] pathName) {
        List<URL> result = new ArrayList<>();
        for (int i = 0; i < pathName.length; i++) {
            URL resource = findResource(packageName, pathName[i]);
            if (resource != null) {
                result.add(resource);
            } else {
                resource = findResource(pathName[i]);
                if (resource != null) {
                    result.add(resource);
                }
            }
        }
        if (result.size() > 0) {
            return result.toArray(new URL[result.size()]);
        }

        return null;
    }

    /**
     * Locates a resource by searching multiple locations.
     *
     * <p>This method assumes that the path is a full package path such as
     * <code>org/gwtproject/uibinder/example/view/SimpleFormView.ui.xml</code>
     *
     * @return FileObject or null if file is not found.
     * @see #findResource(CharSequence, CharSequence)
     */
    @Override
    public URL findResource(CharSequence path) {
        String packageName = "";
        String relativeName = path.toString();

        int index = relativeName.lastIndexOf('/');
        if (index >= 0) {
            packageName = relativeName.substring(0, index)
                    .replace('/', '.');
            relativeName = relativeName.substring(index + 1);
        }

        return findResource(packageName, relativeName);
    }

    @Override
    public URL[] findResources(TreeLogger logger, ExecutableElement method) throws UnableToCompleteException {
        TypeElement returnType = (TypeElement) MoreTypes.asElement(method.getReturnType());
        assert returnType.getKind().isInterface() || returnType.getKind().isClass();
        DefaultExtensions annotation = ResourceGeneratorUtil.findDefaultExtensionsInClassHierarcy(returnType);
        String[] extensions;
        if (annotation != null) {
            extensions = annotation.value();
        } else {
            extensions = new String[0];
        }
        return findResources(logger, method, extensions);
    }

    @Override
    public URL[] findResources(TreeLogger logger, ExecutableElement method, String[] defaultSuffixes) throws UnableToCompleteException {
        boolean error = false;
        Source resourceAnnotation = method.getAnnotation(Source.class);
        URL[] toReturn = null;

        if (resourceAnnotation == null) {
            if (defaultSuffixes != null) {

                for (String extension : defaultSuffixes) {
                    if (logger.isLoggable(TreeLogger.SPAM)) {
                        logger.log(TreeLogger.SPAM, "Trying default extension " + extension);
                    }
                    String url = (MoreElements.getPackage(method) + "." + method.getSimpleName()).replace('.', '/') + extension;
                    URL resourceUrl = findResource(url);

                    // Take the first match
                    if (resourceUrl != null) {
                        return new URL[]{resourceUrl};
                    }
                }


            }
            logger.log(TreeLogger.ERROR, "No " + Source.class.getName()
                    + " annotation and no resources found with default extensions");
            error = true;
        } else {
            // The user has put an @Source annotation on the accessor method
            String[] resources = resourceAnnotation.value();
            toReturn = findResources(MoreElements.getPackage(method.getEnclosingElement()).getQualifiedName().toString(), resources);
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

    /**
     * Locates a resource by searching multiple locations.
     *
     * <p>Searches in the order of</p>
     * <ul>
     * <li>{@link StandardLocation#SOURCE_PATH}</li>
     * <li>{@link StandardLocation#CLASS_PATH}</li>
     * <li>{@link StandardLocation#CLASS_OUTPUT}</li>
     * </ul>
     *
     * @return FileObject or null if file is not found.
     */
    @Override
    public URL findResource(CharSequence pkg, CharSequence relativeName) {
        return findResource(
                Arrays.asList(
                        StandardLocation.SOURCE_PATH,
                        StandardLocation.SOURCE_OUTPUT,
                        StandardLocation.CLASS_PATH,
                        StandardLocation.CLASS_OUTPUT
                ), pkg, relativeName);
    }

    /**
     * Locates a resource by searching multiple locations.
     *
     * @return FileObject or null if file is not found in given locations.
     */
    private URL findResource(List<Location> searchLocations, CharSequence pkg,
                             CharSequence relativeName) {
        if (searchLocations == null || searchLocations.isEmpty()) {
            return null;
        }
        for (Location location : searchLocations) {
            String path = "";
            if (pkg.length() > 0) {
                path = String.valueOf(pkg).replace('.', File.separatorChar) + '/';
            }
            try {
                FileObject fileObject = aptContext.filer.getResource(location, "", path + relativeName);
                if (new File(fileObject.getName()).exists()) {
                    return fileObject.toUri().toURL();
                }
            } catch (FilerException ignored) {
                File openedfile = new File(ignored.getMessage().replace("Attempt to reopen a file for path ", ""));
                if (openedfile.exists()) {
                    try {
                        return openedfile.toURI().toURL();
                    } catch (MalformedURLException e) {
                        // ignored
                    }
                }
                // ignored
            } catch (IOException ignored) {
                // ignored
            }
        }
        // unable to locate, return null.
        return null;
    }
}