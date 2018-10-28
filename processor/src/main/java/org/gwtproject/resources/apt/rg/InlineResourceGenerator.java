package org.gwtproject.resources.apt.rg;

import com.squareup.javapoet.TypeSpec;
import org.gwtproject.resources.apt.ClientBundleGeneratorContext;
import org.gwtproject.resources.apt.exceptions.UnableToCompleteException;
import org.gwtproject.resources.apt.resource.Resource;

import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

/**
 * @author Dmitrii Tikhomirov <chani@me.com>
 * Created by treblereel on 10/27/18.
 */
public abstract class InlineResourceGenerator extends StaticResourceGenerator {

    InlineResourceGenerator(ClientBundleGeneratorContext context, Element clazz, TypeSpec.Builder builder) {
        super(context, clazz, builder);
    }

    public String deploy(Resource resource, String mimeType, boolean forceExternal) throws UnableToCompleteException {
        byte[] data = readByteArrayFromResource(resource);
        if ((!forceExternal) && (data.length < MAX_INLINE_SIZE)) {
            context.messager.printMessage(Diagnostic.Kind.NOTE, "Inlining");

            String base64Contents = toBase64(data);

            String encoded = "\"data:" + mimeType.replaceAll("\"", "\\\\\"")
                    + ";base64," + base64Contents + "\"";
            /*
             * We know that the encoded format will be one byte per character, since
             * we're using only ASCII characters.
             */
            if (encoded.length() < MAX_ENCODED_SIZE) {
                return encoded;
            }
        }

        return super.deploy(resource, mimeType, true);
    }
}