package org.gwtproject.resources.client;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Dmitrii Tikhomirov <chani@me.com>
 * Created by treblereel on 10/18/18.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ClientBundleGenerators {
    String[] value();
}
