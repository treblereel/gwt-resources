package org.gwtproject.resources.context;

import org.gwtproject.resources.ext.GeneratorContext;
import org.gwtproject.resources.ext.TreeLogger;

import javax.lang.model.element.TypeElement;

/**
 * This is a refinement that will use data urls for browsers that support them.
 * Only files whose size are smaller than MAX_INLINE_SIZE will be inlined.
 * Larger files will use the standard CacheBundle behavior.
 *
 * @see "RFC 2397"
 */
public final class InlineClientBundleGenerator extends AbstractClientBundleGenerator {

    private final ClientBundleContext clientBundleCtx = new ClientBundleContext();

    @Override
    protected AbstractResourceContext createResourceContext(TreeLogger logger, GeneratorContext context, TypeElement resourceBundleType) {
        return new InlineResourceContext(logger.branch(TreeLogger.DEBUG, "Using inline resources", null), context, resourceBundleType,
                clientBundleCtx);
    }

}
