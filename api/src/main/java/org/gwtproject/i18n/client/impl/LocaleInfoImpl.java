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
package org.gwtproject.i18n.client.impl;

import org.gwtproject.i18n.client.constants.NumberConstants;

/**
 * Implementation detail of LocaleInfo -- not a public API and subject to
 * change.
 * <p>
 * Generated interface for locale information.  The default implementation
 * returns null, which is used if the i18n module is not imported.
 * @see org.gwtproject.i18n.client.LocaleInfo
 */
public class LocaleInfoImpl {

    public boolean hasAnyRTL() {
        return false;
    }

    public NumberConstants getNumberConstants() {
        return new NumberConstantsImpl_();
    }
}
