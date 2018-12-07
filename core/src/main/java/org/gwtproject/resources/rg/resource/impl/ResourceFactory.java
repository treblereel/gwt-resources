package org.gwtproject.resources.rg.resource.impl;

import org.gwtproject.resources.ext.Resource;
import org.gwtproject.resources.ext.UnableToCompleteException;

import java.net.URL;

/**
 * @author Dmitrii Tikhomirov
 * Created by treblereel on 10/8/18.
 */
public class ResourceFactory {

    public static Resource get(URL url) throws UnableToCompleteException {
        if (url.getProtocol().equals("file")) {
            return new FileResource(url);
        } else if (url.getProtocol().equals("jar")) {
            return new JarResource(url);
        }
        throw new UnableToCompleteException("Unsupported resource type " + url);
    }
}
