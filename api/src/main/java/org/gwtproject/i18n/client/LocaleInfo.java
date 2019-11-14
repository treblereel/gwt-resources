/*
 * Copyright 2008 Google Inc.
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
package org.gwtproject.i18n.client;

import org.gwtproject.i18n.client.constants.DateTimeConstants;
import org.gwtproject.i18n.client.constants.NumberConstants;
import org.gwtproject.i18n.client.impl.CldrImpl;
import org.gwtproject.i18n.client.impl.LocaleInfoImpl;
import org.gwtproject.i18n.client.impl.NumberConstantsImpl_;
import org.gwtproject.i18n.client.impl.cldr.DateTimeFormatInfoImpl;
import org.gwtproject.i18n.client.impl.cldr.DateTimeFormatInfoImpl_en;

/**
 * Provides access to the currently-active locale and the list of available
 * locales.
 */
@SuppressWarnings("deprecation")
public class LocaleInfo {

    public static boolean hasAnyRTL() {
        return instance.infoImpl.hasAnyRTL();
    }

    private final LocaleInfoImpl infoImpl;

    private final CldrImpl cldrImpl;

    private DateTimeConstants dateTimeConstants;

    private DateTimeFormatInfo dateTimeFormatInfo;// = new DateTimeFormatInfoImpl_en();

    private NumberConstants numberConstants;

    /**
     * Currently we only support getting the currently running locale, so this
     * is a static.  In the future, we would need a hash map from locale names
     * to LocaleInfo instances.
     */
    private static LocaleInfo instance = new LocaleInfo(new LocaleInfoImpl(),
                                                        new CldrImpl());

    /**
     * Constructor to be used by subclasses, such as mock classes for testing.
     * Any such subclass should override all methods.
     */
    protected LocaleInfo() {
        infoImpl = null;
        cldrImpl = null;
    }

    /**
     * Create a LocaleInfo instance, passing in the implementation classes.
     * @param impl LocaleInfoImpl instance to use
     * @param cldr CldrImpl instance to use
     */
    private LocaleInfo(LocaleInfoImpl impl, CldrImpl cldr) {
        this.infoImpl = impl;
        this.cldrImpl = cldr;
    }

    public static LocaleInfo getCurrentLocale() {
        return new LocaleInfo();
    }

    /**
     * Returns true if this locale is right-to-left instead of left-to-right.
     */
    public final boolean isRTL() {
        return false;
    }

    public DateTimeFormatInfo getDateTimeFormatInfo() {
        return dateTimeFormatInfo;
    }

    public final NumberConstants getNumberConstants() {
        ensureNumberConstants();
        return numberConstants;
    }

    private void ensureNumberConstants() {
        if (numberConstants == null) {
            numberConstants = new NumberConstantsImpl_();
        }
    }
}
