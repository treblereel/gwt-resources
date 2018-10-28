package org.gwtproject.resources.apt.rg;

import com.squareup.javapoet.TypeSpec;
import org.gwtproject.resources.apt.ClientBundleGeneratorContext;
import org.gwtproject.resources.apt.exceptions.UnableToCompleteException;
import org.gwtproject.resources.apt.resource.Resource;

import javax.lang.model.element.Element;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.gwtproject.resources.apt.util.Util.computeStrongName;

/**
 * @author Dmitrii Tikhomirov <chani@me.com>
 * Created by treblereel on 10/27/18.
 */
public abstract class StaticResourceGenerator extends AbstractResourceGenerator {
    boolean enableRenaming = true;

    StaticResourceGenerator(ClientBundleGeneratorContext context, Element clazz, TypeSpec.Builder builder) {
        super(context, clazz, builder);
    }

    public String deploy(Resource resource, String mimeType, boolean forceExternal) throws UnableToCompleteException {
        // See if filename obfuscation should be enabled
        String suggestedFileName = resource.getUrl().getFile();
        byte[] data = readByteArrayFromResource(resource);
        // Determine the final filename for the resource's file
        String outputName;
        if (enableRenaming) {
            String strongName = computeStrongName(readByteArrayFromResource(resource));
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

        writeFileToDisk(data, outputName);
        return "\"" + context.gwtCacheUrl + outputName + "\"";
    }

    protected void writeFileToDisk(byte[] result, String outputName) throws UnableToCompleteException {
        try (FileOutputStream fileOuputStream = new FileOutputStream(new File(context.gwtCacheDir, outputName))) {
            fileOuputStream.write(result);
        } catch (IOException ioe) {
            throw new UnableToCompleteException(ioe.getMessage());
        }
    }
}
