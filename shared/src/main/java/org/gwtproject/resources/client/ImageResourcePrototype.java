package org.gwtproject.resources.client;

/*
 * Copyright 2007 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

import elemental2.dom.Image;
import org.gwtproject.safehtml.shared.SafeUri;

import static elemental2.dom.DomGlobal.document;

/**
 * This is part of an implementation of the ImageBundle optimization implemented
 * with ClientBundle.
 */
public class ImageResourcePrototype implements ImageResource {

    private final boolean animated;
    private final boolean lossy;
    private final String name;
    private final SafeUri url;
    private final int width;
    private final int height;

    public ImageResourcePrototype(String name, SafeUri url, int width, int height,
                                  boolean animated, boolean lossy) {
        this.name = name;
        this.height = height;
        this.width = width;
        this.url = url;
        this.animated = animated;
        this.lossy = lossy;
    }

    public int getHeight() {
        return height;
    }

    /**
     * Returns the Image
     */
    @Override
    public Image getImage() {
        elemental2.dom.Image image = (elemental2.dom.Image) document.createElement("img");
        image.src = getSafeUri().asString();
        image.name = name;
        image.width = width;
        image.height = height;
        //image.dir = "";
        //setSetting and so on
        return image;
    }

    public String getName() {
        return name;
    }

    public SafeUri getSafeUri() {
        return url;
    }

    public String getURL() {
        return url.asString();
    }

    public int getWidth() {
        return width;
    }

    public boolean isAnimated() {
        return animated;
    }

    public boolean isLossy() {
        return lossy;
    }
}

