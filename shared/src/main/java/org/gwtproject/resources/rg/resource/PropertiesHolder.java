package org.gwtproject.resources.rg.resource;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.TreeSet;

/**
 * @author Dmitrii Tikhomirov
 * Created by treblereel 11/7/18
 */
public final class PropertiesHolder {

    private final Filer filer;
    // This can be used to make CssResource produce human-readable CSS
    public String STYLE = "obf"; // CssResource.style // obf stable stable-shorttype debug stable-notype pretty

    // This allows merging of CSS rules to be disabled.
    public boolean MERGE_ENABLED = true; // CssResource.mergeEnabled

    // This allows the developer to use shorter obfuscated class names.
    // Is is valid to extend this property to use a custom name.
    public String OBFUSCATION_PREFIX = "default"; // CssResource.obfuscationPrefix


    // A multi-valued configuration property that defines class name prefixes
    // the CssResource obfuscator should not use.
    public TreeSet<String> RESERVED_PREFIXES = new TreeSet<>();//CssResource.reservedClassPrefixes


    // This can be used to disable the use of strongly-named files
    public boolean ENABLE_RENAMING = true; //ClientBundle.enableRenaming

    // Is inlining enabled?
    public boolean ENABLE_INLINING = true;    //ClientBundle.enableInlining


    public boolean ENABLE_GSS = false;    //CssResource.enableGss

    public File GWT_CACHE_DIR;
    public String GWT_CACHE_URL = "/gwt-cache/";
    public String GWT_CACHE_LOCATION = "src/main/webapp/gwt-cache";

    public PropertiesHolder(Filer filer) {
        this.filer = filer;

        setGWTCacheUrl();
        setGWTCacheLocation();
        setGWTenableInlining();
        setGWTenableRenaming();
        setGWTreservedClassPrefixes();
        setGWTobfuscationPrefix();
        setGWTmergeEnabled();
        setGWTstyle();
        setGWTenableGss();
    }

    private void setGWTCacheUrl() {
        String url = System.getProperty("gwt.clientbundle.gwt-cache.url");
        if (url != null) {
            GWT_CACHE_URL = url;
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
                this.GWT_CACHE_DIR = new File(path.toUri().getPath() + cacheLocation);
            } else {
                this.GWT_CACHE_DIR = new File(path.toUri().getPath() + GWT_CACHE_LOCATION);
            }

            if (!GWT_CACHE_DIR.exists()) {
                GWT_CACHE_DIR.mkdir();
            }

        } catch (IOException e) {
            throw new Error("Unable to locate gwt cache folder " + e.getMessage());
        }
    }

    private void setGWTenableInlining() {
        String value = System.getProperty("ClientBundle.enableInlining");
        if (value != null) {
            ENABLE_INLINING = Boolean.valueOf(value);
        }
    }

    private void setGWTenableRenaming() {
        String value = System.getProperty("ClientBundle.enableRenaming");
        if (value != null) {
            ENABLE_RENAMING = Boolean.valueOf(value);
        }
    }

    private void setGWTmergeEnabled() {
        String value = System.getProperty("CssResource.mergeEnabled");
        if (value != null) {
            MERGE_ENABLED = Boolean.valueOf(value);
        }
    }

    private void setGWTenableGss() {
        String value = System.getProperty("CssResource.enableGss");
        if (value != null) {
            ENABLE_GSS = Boolean.valueOf(value);
        }
    }

    private void setGWTreservedClassPrefixes() {
        String values = System.getProperty("CssResource.reservedClassPrefixes");
        if (values != null) {
            Arrays.stream(values.split(",")).forEach(e -> RESERVED_PREFIXES.add(e));
        } else {
            RESERVED_PREFIXES.add("gwt-");
        }
    }

    private void setGWTobfuscationPrefix() {
        String value = System.getProperty("CssResource.obfuscationPrefixs");
        if (value != null) {
            OBFUSCATION_PREFIX = value;
        }
    }

    private void setGWTstyle() {
        String value = System.getProperty("CssResource.style");
        if (value != null) {
            STYLE = value;
        }
    }
}
