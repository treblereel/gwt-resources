package org.gwtproject.resources.apt.resource.impl;

import org.gwtproject.resources.apt.exceptions.UnableToCompleteException;
import org.gwtproject.resources.apt.resource.Resource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Dmitrii Tikhomirov <chani@me.com>
 * Created by treblereel on 10/28/18.
 */
public class FakeResource implements Resource {

    private byte[] data;
    private URL url;

    public FakeResource(String url, byte[] data) {
        this.data = data;
        try {
            this.url = new File(url).toURI().toURL();
        } catch (MalformedURLException e) {
            new UnableToCompleteException(e);
        }
    }

    /**
     * Returns the contents of the resource. The caller is responsible for closing the stream.
     */
    @Override
    public InputStream openContents() {
        return new ByteArrayInputStream(data);
    }

    @Override
    public URL getUrl() {
        return this.url;
    }
}
