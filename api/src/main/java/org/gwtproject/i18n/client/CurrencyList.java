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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

import org.gwtproject.core.client.JavaScriptObject;

/**
 * Generated class containing all the CurrencyImpl instances.  This is just
 * the fallback in case the I18N module is not included.
 */
public class CurrencyList implements Iterable<CurrencyData> {

    /**
     * Inner class to avoid CurrencyList.clinit calls and allow this to be
     * completely removed from the generated code if instance isn't referenced
     * (such as when all you call is CurrencyList.get().getDefault() ).
     */
    private static class CurrencyListInstance {
        private static CurrencyList instance = new CurrencyList_();
    }

    /**
     * Return the singleton instance of CurrencyList.
     */
    public static CurrencyList get() {
        return CurrencyListInstance.instance;
    }

    /**
     * Add all entries in {@code override} to the original map, replacing
     * any existing entries.  This is used by subclasses that need to slightly
     * alter the data used by the parent locale.
     */
    protected static final JavaScriptObject overrideMap(
            JavaScriptObject original, JavaScriptObject override) {
        throw new UnsupportedOperationException("overrideMap");
    }
    /**
     * Add currency codes contained in the map to an ArrayList.
     */
    private static void loadCurrencyValuesNative(JavaScriptObject map, ArrayList<CurrencyData> collection) {
        throw new UnsupportedOperationException("loadCurrencyValuesNative");
    }

    /**
     * Directly reference an entry in the currency names map JSO.
     *
     * @param code ISO4217 currency code
     * @return currency name, or the currency code if not known
     */
    private static String lookupNameNative(JavaScriptObject namesMap, String code) {
        throw new UnsupportedOperationException("lookupNameNative");
    }

    /**
     * Directly reference an entry in the currency map JSO.
     *
     * @param code ISO4217 currency code
     * @return currency data
     */
    private static CurrencyData lookupNative(JavaScriptObject dataMap, String code) {
        throw new UnsupportedOperationException("lookupNative");
    }

    /**
     * Map of currency codes to CurrencyData.
     */
    protected HashMap<String, CurrencyData> dataMapJava;

    /**
     * JS map of currency codes to CurrencyData objects. Each currency code is
     * assumed to be a valid JS object key.
     */
    protected JavaScriptObject dataMapNative;

    /**
     * Map of currency codes to localized currency names. This is kept separate
     * from {@link #dataMapJava} above so that the names can be completely removed by
     * the compiler if they are not used.
     */
    protected HashMap<String, String> namesMapJava;

    /**
     * JS map of currency codes to localized currency names. This is kept separate
     * from {@link #dataMapNative} above so that the names can be completely
     * removed by the compiler if they are not used. Each currency code is assumed
     * to be a valid JS object key.
     */
    protected JavaScriptObject namesMapNative;

    /**
     * Return the default currency data for this locale.
     *
     * Generated implementations override this method.
     */
    public CurrencyData getDefault() {
       return getDefaultNative();
    }

    /**
     * Returns an iterator for the list of currencies.
     *
     * Deprecated currencies will not be included.
     */
    @Override
    public final Iterator<CurrencyData> iterator() {
        return iterator(false);
    }

    /**
     * Returns an iterator for the list of currencies, optionally including
     * deprecated ones.
     *
     * @param includeDeprecated true if deprecated currencies should be included
     */
    public final Iterator<CurrencyData> iterator(boolean includeDeprecated) {
        ensureCurrencyMap();
        ArrayList<CurrencyData> collection = new ArrayList<CurrencyData>();
            loadCurrencyValuesNative(dataMapNative, collection);
        if (!includeDeprecated) {
            ArrayList<CurrencyData> newCollection = new ArrayList<CurrencyData>();
            for (CurrencyData value : collection) {
                if (!value.isDeprecated()) {
                    newCollection.add(value);
                }
            }
            collection = newCollection;
        }
        return Collections.unmodifiableList(collection).iterator();
    }

    /**
     * Lookup a currency based on the ISO4217 currency code.
     *
     * @param currencyCode ISO4217 currency code
     * @return currency data, or null if code not found
     */
    public final CurrencyData lookup(String currencyCode) {
        ensureCurrencyMap();
            return lookupNative(dataMapNative, currencyCode);
    }

    /**
     * Lookup a currency name based on the ISO4217 currency code.
     *
     * @param currencyCode ISO4217 currency code
     * @return name of the currency, or null if code not found
     */
    public final String lookupName(String currencyCode) {
        ensureNamesMap();
            return lookupNameNative(namesMapNative, currencyCode);
    }

    /**
     * Return the default currency data for this locale.
     *
     * Generated implementations override this method.
     */
    protected CurrencyData getDefaultNative() {
        throw new UnsupportedOperationException("getDefaultNative");
    }

    /**
     * Loads the currency map from a JS object literal.
     *
     * Generated implementations override this method.
     */
    protected JavaScriptObject loadCurrencyMapNative() {
        throw new UnsupportedOperationException("getDefaultNative");
    }

    /**
     * Loads the currency names map.
     *
     * Generated implementations override this method.
     */
    protected HashMap<String, String> loadNamesMapJava() {
        HashMap<String, String> result = new HashMap<String, String>();
        result.put("USD", "US Dollar");
        result.put("EUR", "Euro");
        result.put("GBP", "British Pound Sterling");
        result.put("JPY", "Japanese Yen");
        return result;
    }

    /**
     * Loads the currency names map from a JS object literal.
     *
     * Generated implementations override this method.
     */
    protected JavaScriptObject loadNamesMapNative() {
        throw new UnsupportedOperationException("getDefaultNative");
    }

    /**
     * Ensure that the map of currency data has been initialized.
     */
    private void ensureCurrencyMap() {
            if (dataMapNative == null) {
                dataMapNative = loadCurrencyMapNative();
            }
    }

    /**
     * Ensure that the map of currency data has been initialized.
     */
    private void ensureNamesMap() {
            if (namesMapNative == null) {
                namesMapNative = loadNamesMapNative();
            }
    }
}
