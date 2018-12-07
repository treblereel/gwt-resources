package org.gwtproject.resources.rg.resource.impl;

import org.gwtproject.resources.ext.Resource;
import org.gwtproject.resources.ext.UnableToCompleteException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author Dmitrii Tikhomirov
 * Created by treblereel on 10/8/18.
 */
public class JarResource implements Resource {
    private final URL url;

    public JarResource(URL url) {
        this.url = url;
    }

    /**
     * Returns the contents of the resource. The caller is responsible for closing the stream.
     */
    @Override
    public InputStream openContents() throws UnableToCompleteException {
        try {
            return (url.openConnection()).getInputStream();
        } catch (IOException e) {
            e.getStackTrace();
            throw new UnableToCompleteException("Unable to open InputStream" + url);
        }
    }

    @Override
    public URL getUrl() {
        return url;
    }
}
