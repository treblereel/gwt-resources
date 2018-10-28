/*
 * Copyright 2010 Google Inc.
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
package org.gwtproject.resources.shared;

import com.google.gwt.junit.client.GWTTestCase;
import org.gwtproject.resources.client.ClientBundle;
import org.gwtproject.resources.client.DataResource;

import static org.gwtproject.resources.client.ClientBundle.*;

/**
 * Tests for {@link DataResource.DoNotEmbed @DoNotEmbed} resource annotations.
 */
public class DataResourceDoNotEmbedTest extends GWTTestCase {

    @ClientBundle
    interface DataResourcesDoNotEmbed {

        /**
         * This is a binary file containing four 0x00 bytes, which is small enough
         * to be embeddable, and contains insufficient information for a
         * determination of a recognizable MIME Type.
         */
        String FOUR_ZEROS_SOURCE = "fourZeros.dat";

        // Purposely missing a @DoNotEmbed annotation
        @Source(FOUR_ZEROS_SOURCE)
        DataResource resourceDoNotEmbedAnnotationMissing();

        @DataResource.DoNotEmbed
        @Source(FOUR_ZEROS_SOURCE)
        DataResource resourceDoNotEmbedAnnotationPresent();
    }

    /**
     * RFC 2397 data URL scheme.
     */
    private static final String DATA_URL_SCHEME = "data:";

    @Override
    public String getModuleName() {
        return "org.gwtproject.resources.ResourcesTestsModule";
    }

    public void testDoNotEmbedAnnotationMissingShouldEmbed() {
        DataResourcesDoNotEmbed r = new DataResourcesDoNotEmbedImpl();
        String url = r.resourceDoNotEmbedAnnotationMissing().getSafeUri().asString();
        assertTrue("url '" + url + "' doesn't start with'" + DATA_URL_SCHEME + "'",
                url.startsWith(DATA_URL_SCHEME));
    }

    public void testDoNotEmbedAnnotationPresentShouldNotEmbed() {
        DataResourcesDoNotEmbed r = new DataResourcesDoNotEmbedImpl();
        String url = r.resourceDoNotEmbedAnnotationPresent().getSafeUri().asString();
        assertFalse(
                "url '" + url + "' mustn't start with'" + DATA_URL_SCHEME + "'",
                url.startsWith(DATA_URL_SCHEME));
    }
}
