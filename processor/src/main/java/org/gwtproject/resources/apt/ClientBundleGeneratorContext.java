package org.gwtproject.resources.apt;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Dmitrii Tikhomirov <chani@me.com>
 * Created by treblereel on 10/26/18.
 */
public class ClientBundleGeneratorContext {
    public final Messager messager;
    public final Filer filer;
    public final Elements elementUtils;
    public final Types typeUtils;

    public File gwtCacheDir;
    public String gwtCacheUrl = "/gwt-cache/";   //default value
    public String GWT_CACHE_LOCATION = "src/main/webapp/gwt-cache";


    public ClientBundleGeneratorContext(final ProcessingEnvironment processingEnv) {
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();

        setGWTCacheLocation();
        setGWTCacheUrl();
    }

    private void setGWTCacheUrl() {
        String url = System.getProperty("gwt.clientbundle.gwt-cache.url");
        if (url != null) {
            gwtCacheUrl = url;
        }
    }

    private void setGWTCacheLocation() {
        try {
            FileObject fileObject = filer.createResource(StandardLocation.SOURCE_OUTPUT, "", "dummy" + System.currentTimeMillis());

            Path path = Paths.get(fileObject.toUri())
                    .getParent()  // {PROJECT_ROOT}/target/generated-sources/annotations
                    .getParent()  // {PROJECT_ROOT}/target/generated-sources
                    .getParent()  // {PROJECT_ROOT}/target
                    .getParent(); // {PROJECT_ROOT}

            String cacheLocation = System.getProperty("gwt.clientbundle.gwt-cache.location");
            if (cacheLocation != null) {
                this.gwtCacheDir = new File(path.toUri().getPath() + cacheLocation);
            } else {
                this.gwtCacheDir = new File(path.toUri().getPath() + GWT_CACHE_LOCATION);
            }

            if (!gwtCacheDir.exists()) {
                gwtCacheDir.mkdir();
            }

        } catch (IOException e) {
            throw new Error("Unable to locate gwt cache folder " + e.getMessage());
        }
    }
}