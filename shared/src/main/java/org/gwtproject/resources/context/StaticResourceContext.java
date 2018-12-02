package org.gwtproject.resources.context;

import org.gwtproject.resources.ext.GeneratorContext;
import org.gwtproject.resources.ext.TreeLogger;
import org.gwtproject.resources.ext.UnableToCompleteException;
import org.gwtproject.resources.rg.AptContext;
import org.gwtproject.resources.rg.util.Util;

import javax.lang.model.element.TypeElement;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Dmitrii Tikhomirov
 * Created by treblereel 11/11/18
 */
class StaticResourceContext extends AbstractResourceContext {

    StaticResourceContext(TreeLogger logger, GeneratorContext context,
                          TypeElement resourceBundleType, ClientBundleContext clientBundleCtx) {
        super(logger, context, resourceBundleType, clientBundleCtx);
    }

    public String deploy(String suggestedFileName, String mimeType, byte[] data,
                         boolean forceExternal) throws UnableToCompleteException {
        TreeLogger logger = getLogger();
        GeneratorContext context = getGeneratorContext();


        // See if filename obfuscation should be enabled
        boolean enableRenaming = context.getAptContext().propertiesHolder.ENABLE_RENAMING;


        // Determine the final filename for the resource's file
        String outputName;
        if (enableRenaming) {
            String strongName = Util.computeStrongName(data);

            // Determine the extension of the original file
            String extension;
            int lastIdx = suggestedFileName.lastIndexOf('.');
            if (lastIdx != -1) {
                extension = suggestedFileName.substring(lastIdx + 1);
            } else {
                extension = "noext";
            }
            // The name will be MD5.cache.ext
            outputName = strongName + ".cache." + extension;
        } else {
            outputName = suggestedFileName.substring(suggestedFileName.lastIndexOf('/') + 1);
        }

        //writeFileToDisk(logger, context.getAptContext(), data, outputName);

        // Ask the context for an OutputStream into the named resource
        OutputStream out = context.tryCreateResource(logger, outputName);

        // This would be null if the resource has already been created in the
        // output (because two or more resources had identical content).
        if (out != null) {
            try {
                out.write(data);

            } catch (IOException e) {
                logger.log(TreeLogger.ERROR, "Unable to write data to output name "
                        + outputName, e);
                throw new UnableToCompleteException();
            }

            // If there's an error, this won't be called and there will be nothing
            // created in the output directory.
            context.commitResource(logger, out);
            if (logger.isLoggable(TreeLogger.DEBUG)) {
                logger.log(TreeLogger.DEBUG, "Copied " + data.length + " bytes to " + outputName, null);
            }
        }


        // Return a Java expression
        return "\"" + context.getAptContext().propertiesHolder.GWT_CACHE_URL + outputName + "\"";
    }

    protected void writeFileToDisk(TreeLogger logger, AptContext context, byte[] result, String outputName) throws UnableToCompleteException {
        try (FileOutputStream fileOuputStream = new FileOutputStream(new File(context.propertiesHolder.GWT_CACHE_DIR, outputName))) {
            fileOuputStream.write(result);
        } catch (IOException ioe) {
            logger.log(TreeLogger.Type.ERROR, "Unable to write a file " + ioe.getMessage());
            throw new UnableToCompleteException();
        }
    }

    @Override
    public boolean supportsDataUrls() {
        return false;
    }

}