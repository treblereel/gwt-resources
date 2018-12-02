package org.gwtproject.resources.context;

import org.gwtproject.resources.ext.GeneratorContext;
import org.gwtproject.resources.ext.TreeLogger;
import org.gwtproject.resources.ext.UnableToCompleteException;

import javax.lang.model.element.TypeElement;

/**
 * @author Dmitrii Tikhomirov
 * Created by treblereel 11/11/18
 */
class InlineResourceContext extends StaticResourceContext {
    /**
     * String constants in Java have a maximum limit that we must obey.
     */
    public static final int MAX_ENCODED_SIZE = (2 << 15) - 1;

    InlineResourceContext(TreeLogger logger, GeneratorContext context,
                          TypeElement resourceBundleType, ClientBundleContext clientBundleCtx) {
        super(logger, context, resourceBundleType, clientBundleCtx);
    }

    @Override
    public String deploy(String suggestedFileName, String mimeType, byte[] data,
                         boolean forceExternal) throws UnableToCompleteException {
        TreeLogger logger = getLogger();

        // data: URLs are not compatible with XHRs on FF and Safari browsers
        if ((!forceExternal) && (data.length < MAX_INLINE_SIZE)) {
            logger.log(TreeLogger.DEBUG, "Inlining", null);

            String base64Contents = toBase64(data);

            // CHECKSTYLE_OFF
            String encoded = "\"data:" + mimeType.replaceAll("\"", "\\\\\"")
                    + ";base64," + base64Contents + "\"";
            // CHECKSTYLE_ON

            /*
             * We know that the encoded format will be one byte per character, since
             * we're using only ASCII characters.
             */
            if (encoded.length() < MAX_ENCODED_SIZE) {
                return encoded;
            }
        }

        return super.deploy(suggestedFileName, mimeType, data, true);
    }

    @Override
    public boolean supportsDataUrls() {
        return true;
    }
}
