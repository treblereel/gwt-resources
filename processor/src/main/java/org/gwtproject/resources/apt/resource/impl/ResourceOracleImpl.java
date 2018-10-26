package org.gwtproject.resources.apt.resource.impl;

import org.gwtproject.resources.apt.exceptions.UnableToCompleteException;
import org.gwtproject.resources.apt.resource.Resource;
import org.gwtproject.resources.apt.resource.ResourceOracle;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * @author Dmitrii Tikhomirov <chani@me.com>
 * Created by treblereel on 10/8/18.
 */
public class ResourceOracleImpl implements ResourceOracle {
    private final String quote = Matcher.quoteReplacement(File.separator);

    public ResourceOracleImpl() {

    }

    /**
     * Returns the resource for the given path name or null if there is no such resource.
     *
     * @param packageName
     * @param pathName
     */
    @Override
    public Resource getResource(String packageName, String[] pathName) throws UnableToCompleteException {
        for (int i = 0; i < pathName.length; i++) {
            Resource resource = getResource(packageName, pathName[i]);
            if (resource != null) {
                return resource;
            }
        }
        throw new UnableToCompleteException("Unable to find resource for " + pathName);
    }

    @Override
    public Resource getResource(String packageName, String pathName) throws UnableToCompleteException {
        // absolute path
        URL url = this.getClass().getResource("/" + pathName);
        if (url != null) {
            Resource candidate = ResourceFactory.get(url);
            if (candidate != null) {
                return candidate;
            }
        }
        //relative path
        StringBuffer sb = new StringBuffer();
        sb.append(quote).append(packageName.replaceAll("\\.", quote)).append(quote).append(pathName);
        url = this.getClass().getResource(sb.toString());
        if (url != null) {
            return ResourceFactory.get(url);
        }
        return null;
    }

    /**
     * Returns the resource input stream for the given path name or null if there
     * is no such resource.
     *
     * @param pathName
     */
    @Override
    public InputStream getResourceAsStream(String pathName) {
        return null;
    }

    /**
     * Returns an unmodifiable set of unique resources with constant lookup time.
     */
    @Override
    public Set<Resource> getResources() {
        return null;
    }
}
