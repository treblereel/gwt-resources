package org.gwtproject.i18n.client;

import elemental2.core.JsArray;
import jsinterop.base.Js;
import org.gwtproject.core.client.JavaScriptObject;

public class CurrencyList_ extends CurrencyList {
  
  @Override
  protected CurrencyData getDefaultNative() {
    String[] arr = {"USD", "US$", "2", "US$", "$"};
    JsArray<String> result = new JsArray();
    return Js.uncheckedCast(result.push("USD", "US$", "2", "US$", "$"));
  }

  @Override
  protected JavaScriptObject loadCurrencyMapNative() {
    return overrideMap(super.loadCurrencyMapNative(), loadMyCurrencyMapOverridesNative());
  }
  
  private JavaScriptObject loadMyCurrencyMapOverridesNative() {
    throw new UnsupportedOperationException("loadMyCurrencyMapOverridesNative");
  }
}
