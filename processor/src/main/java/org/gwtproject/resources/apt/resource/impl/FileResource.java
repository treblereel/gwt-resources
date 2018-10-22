package org.gwtproject.resources.apt.resource.impl;

import org.gwtproject.resources.apt.exceptions.UnableToCompleteException;
import org.gwtproject.resources.apt.resource.Resource;
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

    public FileResource(URL url){
        this.url = url;
    }

    @Override
    public InputStream openContents() throws IOException, UnableToCompleteException {
        if (url.getContent() instanceof PlainTextInputStream) {
            return (PlainTextInputStream) url.getContent();
        }
        throw new UnableToCompleteException("unable to open InputStream" + url);
    }
}
