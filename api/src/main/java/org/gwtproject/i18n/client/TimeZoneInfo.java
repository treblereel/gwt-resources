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

import jsinterop.annotations.JsConstructor;
import jsinterop.base.Js;
import org.gwtproject.core.client.JavaScriptObject;
import org.gwtproject.core.client.JsArrayInteger;
import org.gwtproject.core.client.JsArrayString;
import org.gwtproject.core.client.JsonUtils;

/**
 * A JavaScript Overlay type on top of the JSON data describing everything we
 * need to know about a particular timezone. The relevant strings of JSON can
 * be found in TimeZoneConstants, or versions localized for non-en locales can
 * be downloaded elsewhere.
 */
public class TimeZoneInfo extends JavaScriptObject {
  /**
   * Construct a TimeZoneData javascript overlay object given some json text.
   * This method directly evaluates the String that you pass in; no error or
   * safety checking is performed, so be very careful about the source of
   * your data.
   *
   * @param json JSON text describing a time zone, like what comes from
   * {@link  org.gwtproject.i18n.client.constants.TimeZoneConstants}.
   * @return a TimeZoneInfo object made from the supplied JSON.
   */
  public static TimeZoneInfo buildTimeZoneData(String json) {
    return JsonUtils.safeEval(json);
  }

  @JsConstructor
  protected TimeZoneInfo() { }

  public final String getID() {
   return Js.uncheckedCast(Js.asPropertyMap(this).get("id"));
  }

  public final JsArrayString getNames() {
    return Js.uncheckedCast(Js.asPropertyMap(this).get("names"));
  }

  public final int getStandardOffset() {
    return Js.uncheckedCast(Js.asPropertyMap(this).get("std_offset"));
  }

  public final JsArrayInteger getTransitions() {
    return Js.uncheckedCast(Js.asPropertyMap(this).get("transitions"));
  }
}