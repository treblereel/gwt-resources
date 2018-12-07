package org.gwtproject.resources.rg.resource.impl;

import org.gwtproject.resources.ext.Resource;
import org.gwtproject.resources.ext.UnableToCompleteException;
import sun.net.www.content.text.PlainTextInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author Dmitrii Tikhomirov <chani@me.com>
 * Created by treblereel on 10/8/18.
 */
public class FileResource implements Resource {
    private final URL url;

    public FileResource(URL url) {
        this.url = url;
    }

    @Override
    public InputStream openContents() throws UnableToCompleteException {
        try {
            if (url.getContent() instanceof PlainTextInputStream) {
                return (PlainTextInputStream) url.getContent();
            } else if (url.getContent() instanceof sun.awt.image.URLImageSource) {
                return url.openStream();
            } else if (url.getContent() instanceof java.io.BufferedInputStream) {
                return url.openStream();
            }
        } catch (IOException e) {
            throw new UnableToCompleteException("Unable to open InputStream " + url);
        }
        throw new UnableToCompleteException("Unable to open InputStream " + url);
    }

    @Override
    public URL getUrl() {
        return url;
    }
}
