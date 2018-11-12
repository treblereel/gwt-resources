package org.gwtproject.resources.apt;

import com.google.auto.service.AutoService;
import org.gwtproject.resources.ext.TreeLogger;
import org.gwtproject.resources.ext.UnableToCompleteException;
import org.gwtproject.resources.logger.PrintWriterTreeLogger;
import org.gwtproject.resources.rg.AptContext;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.Set;

/**
 * @author Dmitrii Tikhomirov
 * Created by treblereel on 9/30/18.
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes({"org.gwtproject.resources.client.Resource"})
public class ClientBundleAnnotationProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        if (annotations.isEmpty()) {
            return false;
        }
        AptContext context = new AptContext(processingEnv, roundEnvironment);
        TreeLogger logger = new PrintWriterTreeLogger();
        ((PrintWriterTreeLogger) logger).setMaxDetail(TreeLogger.Type.INFO);
        for (TypeElement annotation : annotations) {
            Set<TypeElement> elements = (Set<TypeElement>) roundEnvironment.getElementsAnnotatedWith(annotation);
            try {
                new ClientBundleClassBuilder(logger, context, elements).process();
            } catch (UnableToCompleteException e) {
                throw new Error(e);
            }
        }
        return true;
    }

}
