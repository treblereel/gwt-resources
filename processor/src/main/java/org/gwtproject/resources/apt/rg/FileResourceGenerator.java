package org.gwtproject.resources.apt.rg;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;
import org.gwtproject.resources.apt.ClientBundleGeneratorContext;
import org.gwtproject.resources.apt.exceptions.UnableToCompleteException;

import javax.lang.model.element.Element;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author Dmitrii Tikhomirov <chani@me.com>
 * Created by treblereel on 10/24/18.
 */
public abstract class FileResourceGenerator extends AbstractResourceGenerator {
    protected final ClassName domGlobalClassName = ClassName.get("elemental2.dom", "DomGlobal");


    FileResourceGenerator(ClientBundleGeneratorContext context, Element clazz, TypeSpec.Builder builder) {
        super(context, clazz, builder);
    }

    protected void writeFileToDisk(String result, String filename) throws UnableToCompleteException {
        try (FileWriter fw = new FileWriter(new File(context.gwtCacheDir, filename))) {
            fw.append(result);
        } catch (IOException ioe) {
            throw new UnableToCompleteException(ioe.getMessage());
        }
    }

    protected void writeFileToDisk(byte[] result, String filename) throws UnableToCompleteException {
        try (FileOutputStream fileOuputStream = new FileOutputStream(new File(context.gwtCacheDir, filename))) {
            fileOuputStream.write(result);
        } catch (IOException ioe) {
            throw new UnableToCompleteException(ioe.getMessage());
        }
    }


}
