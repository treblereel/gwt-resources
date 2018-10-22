package org.gwtproject.resources.apt;

import com.google.auto.service.AutoService;
import org.gwtproject.resources.apt.exceptions.UnableToCompleteException;
import org.gwtproject.resources.apt.rg.ClientBundleClassBuilder;
import org.gwtproject.resources.client.ClientBundle;
import org.gwtproject.resources.client.ClientBundleGenerators;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Dmitrii Tikhomirov
 * Created by treblereel on 9/30/18.
 */
@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_8)
@SupportedAnnotationTypes("org.gwtproject.resources.client.ClientBundle")
public class ClientBundleAnnotationProcessor extends AbstractProcessor {
    private Set<Class> generators = new HashSet<>();


    public ClientBundleAnnotationProcessor() throws UnableToCompleteException {
        locateGenerators();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        if (annotations.isEmpty()) {
            return false;
        }
        for (TypeElement annotation : annotations) {
            Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(annotation);
            try {
                for (final Element element : elements) {
                    new ClientBundleClassBuilder(processingEnv, roundEnvironment, generators, element).process();
                }
            } catch (UnableToCompleteException e) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
                e.printStackTrace();
            }
        }
        return true;
    }

    public void locateGenerators() throws UnableToCompleteException {
        String[] names = ClientBundle.class.getAnnotation(ClientBundleGenerators.class).value();
        for (String name : names) {
            try {
                generators.add(Class.forName(name));
            } catch (ClassNotFoundException e) {
                throw new UnableToCompleteException(e.getMessage());
            }
        }
    }

}
