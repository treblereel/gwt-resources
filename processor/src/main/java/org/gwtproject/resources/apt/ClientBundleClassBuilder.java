package org.gwtproject.resources.apt;

import org.gwtproject.resources.context.InlineClientBundleGenerator;
import org.gwtproject.resources.ext.StandardGeneratorContext;
import org.gwtproject.resources.ext.TreeLogger;
import org.gwtproject.resources.ext.UnableToCompleteException;
import org.gwtproject.resources.rg.AptContext;

import javax.lang.model.element.TypeElement;
import java.util.Map;
import java.util.Set;

/**
 * @author Dmitrii Tikhomirov
 * Created by treblereel 11/11/18
 */
public class ClientBundleClassBuilder {
    private final TreeLogger logger;
    private final AptContext context;

    private final Set<TypeElement> elements;

    public ClientBundleClassBuilder(TreeLogger logger, AptContext context, Set<TypeElement> elements) {
        this.logger = logger;
        this.context = context;
        this.elements = elements;
    }

    public void process() throws UnableToCompleteException {
        StandardGeneratorContext standardGeneratorContext = new StandardGeneratorContext(context);
        InlineClientBundleGenerator inlineClientBundleGenerator = new InlineClientBundleGenerator();
        Map<TypeElement, String> generatedSimpleSourceNames = inlineClientBundleGenerator.generate(logger, standardGeneratorContext, elements);
        new ClientBundleFactoryBuilder(generatedSimpleSourceNames, context).build();
    }
}
