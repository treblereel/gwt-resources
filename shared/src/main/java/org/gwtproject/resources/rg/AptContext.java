package org.gwtproject.resources.rg;

import org.gwtproject.resources.client.*;
import org.gwtproject.resources.ext.ResourceGenerator;
import org.gwtproject.resources.ext.ResourceGeneratorType;
import org.gwtproject.resources.rg.resource.PropertiesHolder;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dmitrii Tikhomirov <chani@me.com>
 * Created by treblereel on 10/26/18.
 */
public class AptContext {
    public final Messager messager;
    public final Filer filer;
    public final Elements elementUtils;
    public final Types typeUtils;
    public final RoundEnvironment roundEnvironment;
    public final PropertiesHolder propertiesHolder;
    public final ProcessingEnvironment processingEnv;

    public final Map<Element, Class<? extends ResourceGenerator>> generators = new HashMap<>();

    public AptContext(final ProcessingEnvironment processingEnv, final RoundEnvironment roundEnvironment) {
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
        this.elementUtils = processingEnv.getElementUtils();
        this.typeUtils = processingEnv.getTypeUtils();
        this.roundEnvironment = roundEnvironment;

        this.processingEnv = processingEnv;

        propertiesHolder = new PropertiesHolder(filer);
        initGenerators();
    }

    private void initGenerators() {
        preBuildGenerators();
        userDefinedGenerators();
    }

    private void preBuildGenerators() {
        generators.put(elementUtils.getTypeElement(ClientBundle.class.getCanonicalName()), BundleResourceGenerator.class);
        generators.put(elementUtils.getTypeElement(CssResource.class.getCanonicalName()), CssResourceGenerator.class);
        generators.put(elementUtils.getTypeElement(DataResource.class.getCanonicalName()), DataResourceGenerator.class);
        generators.put(elementUtils.getTypeElement(ExternalTextResource.class.getCanonicalName()), ExternalTextResourceGenerator.class);
        generators.put(elementUtils.getTypeElement(ImageResource.class.getCanonicalName()), ImageResourceGenerator.class);
        generators.put(elementUtils.getTypeElement(TextResource.class.getCanonicalName()), TextResourceGenerator.class);
    }

    private void userDefinedGenerators() {


        roundEnvironment.getElementsAnnotatedWith(ResourceGeneratorType.class).forEach(e -> {
/*            e.getAnnotationMirrors().forEach(a -> {

                System.out.println("A " + a);
                a.getElementValues().forEach((k,v ) ->{
                    System.out.println("??? " + k.getSimpleName().toString().equals("value") + " "  + v);
                    String value = v.getValue().toString();
                    System.out.println(value);
                    try {
                        Class.forName(value);
                    } catch (ClassNotFoundException e1) {
                        e1.printStackTrace();
                    }
                });

            });*/


            ResourceGeneratorType resourceGeneratorType = e.getAnnotation(ResourceGeneratorType.class);
            String resourceGeneratorName = getResourceGeneratorType(resourceGeneratorType).toString();
            URL location = AptContext.class.getProtectionDomain().getCodeSource().getLocation();
            System.out.println(location.getFile());

            System.out.println("? " + resourceGeneratorName);
/*            try {


                Class.forName(resourceGeneratorName);

                generators.put(e, (Class<? extends ResourceGenerator>) Class.forName(resourceGeneratorName));
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
                throw new Error(e1);
            }*/
        });
    }

    private TypeMirror getResourceGeneratorType(ResourceGeneratorType annotation) {
        try {
            annotation.value();
        } catch (MirroredTypeException mte) {
            //TypeMirror typeMirror = mte.getTypeMirror();
            //System.out.println("? " + typeMirror.getKind());
            //System.out.println("? " + MoreTypes.asElement(typeMirror).getEnclosingElement());
            //System.out.println("? " + MoreTypes.asElement(typeMirror).getSimpleName().toString());
            //MoreTypes.asElement(typeMirror).getAnnotationMirrors();
            return mte.getTypeMirror();
        }
        return null;
    }

}