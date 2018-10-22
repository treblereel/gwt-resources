package org.gwtproject.resources.client;

import elemental2.core.JsObject;
import jsinterop.annotations.JsType;

import static jsinterop.annotations.JsPackage.GLOBAL;

/**
 * @author Dmitrii Tikhomirov
 * Created by treblereel on 10/16/18.
 */
@JsType(isNative = true, namespace = GLOBAL)
public class JSON {

    public native static String stringify(JsObject obj);

    public native static JsObject parse(String obj);

}