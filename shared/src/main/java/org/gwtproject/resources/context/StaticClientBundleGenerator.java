package org.gwtproject.resources.context;

import org.gwtproject.resources.ext.GeneratorContext;
import org.gwtproject.resources.ext.TreeLogger;

import javax.lang.model.element.TypeElement;

/**
 * Copies selected files into module output with strong names and generates the
 * ClientBundle mappings.
 */
public final class StaticClientBundleGenerator extends AbstractClientBundleGenerator {

    private final ClientBundleContext clientBundleCtx = new ClientBundleContext();

    @Override
    protected AbstractResourceContext createResourceContext(TreeLogger logger,
                                                            GeneratorContext context, TypeElement resourceBundleType) {
        return new StaticResourceContext(logger.branch(TreeLogger.DEBUG, "Using static resources", null), context, resourceBundleType,
                clientBundleCtx);
    }
}