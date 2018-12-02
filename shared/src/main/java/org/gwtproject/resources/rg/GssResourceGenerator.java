package org.gwtproject.resources.rg;

import com.google.auto.common.MoreElements;
import com.google.common.css.compiler.ast.CssTree;
import com.google.gwt.i18n.client.LocaleInfo;
import org.gwtproject.resources.client.ClientBundle;
import org.gwtproject.resources.ext.ResourceContext;
import org.gwtproject.resources.ext.ResourceGeneratorUtil;
import org.gwtproject.resources.ext.TreeLogger;
import org.gwtproject.resources.ext.UnableToCompleteException;
import org.gwtproject.resources.rg.gss.CssPrinter;

import javax.lang.model.element.ExecutableElement;
import java.net.URL;
import java.util.Map;
import java.util.Set;

import static org.gwtproject.resources.client.ClientBundle.Source;

/**
 * @author Dmitrii Tikhomirov
 * Created by treblereel 12/1/18
 */
public class GssResourceGenerator extends AbstractCssResourceGenerator {

    private Map<ExecutableElement, CssParsingResult> cssParsingResultMap;

    /**
     * Temporary method needed when GSS and the old CSS syntax are both supported by the sdk.
     * It aims to choose the right resource file according to whether gss is enabled or not. If gss is
     * enabled, it will try to find the resource file ending by .gss first. If GSS is disabled it will
     * try to find the .css file. This logic is applied even if a
     * {@link ClientBundle.Source} annotation is used to define
     * the resource file.
     * <p>
     * This method can be deleted once the support for the old CssResource is removed and use directly
     * ResourceGeneratorUtil.findResources().
     */
    static URL[] findResources(TreeLogger logger, ResourceContext context, ExecutableElement method,
                               boolean gssEnabled) throws UnableToCompleteException {
        boolean isSourceAnnotationUsed = method.getAnnotation(Source.class) != null;

        if (!isSourceAnnotationUsed) {
            // ResourceGeneratorUtil will try to find automatically the resource file. Give him the right
            // extension to use first
            String[] extensions = gssEnabled ?
                    new String[]{".gss", ".css"} : new String[]{".css", ".gss"};
            return ResourceGeneratorUtil.findResources(logger, method, extensions);
        }
        // find the original resource files specified by the @Source annotation
        URL[] originalResources = ResourceGeneratorUtil.findResources(logger, method);
        URL[] resourcesToUse = new URL[originalResources.length];

        String preferredExtension = gssEnabled ? ".gss" : ".css";

        // Try to find all the resources by using the preferred extension according to whether gss is
        // enabled or not. If one file with the preferred extension is missing, return the original
        // resource files otherwise return the preferred files.
        String[] sourceFiles = method.getAnnotation(Source.class).value();
        for (int i = 0; i < sourceFiles.length; i++) {
            String original = sourceFiles[i];

            if (!original.endsWith(preferredExtension) && original.length() > 4) {
                String preferredFile = original.substring(0, original.length() - 4) + preferredExtension;

                // try to find the resource relative to the package
                String path = MoreElements.getPackage(method).getQualifiedName().toString().replace('.', '/') + '/';
                URL preferredUrl = ResourceGeneratorUtil
                        .tryFindResource(path + preferredFile, null);

                if (preferredUrl == null) {
                    // if it doesn't exist, assume it is absolute
                    preferredUrl = ResourceGeneratorUtil.tryFindResource(preferredFile, null);
                }

                if (preferredUrl == null) {
                    // avoid to mix gss and css, if one file with the preferred extension is missing
                    return originalResources;
                }

                logger.log(TreeLogger.Type.DEBUG, "Preferred resource file found: " + preferredFile + ". This file " +
                        "will be used in replacement of " + original);

                resourcesToUse[i] = preferredUrl;
            } else {
                // gss and css files shouldn't be used together for a same resource. So if one of the file
                // is using the the preferred extension, return the original resources. If the dev has mixed
                // gss and ccs files, that will fail later.
                return originalResources;
            }
        }
        return resourcesToUse;
    }

    @Override
    protected String getCssExpression(TreeLogger logger, ResourceContext context, ExecutableElement method) throws UnableToCompleteException {
        CssTree cssTree = cssParsingResultMap.get(method).tree;

        String standard = printCssTree(cssTree);

        // TODO add configuration properties for swapLtrRtlInUrl, swapLeftRightInUrl and
        // shouldFlipConstantReferences booleans
        RecordingBidiFlipper recordingBidiFlipper =
                new RecordingBidiFlipper(cssTree.getMutatingVisitController(), false, false, true);
        recordingBidiFlipper.runPass();

        if (recordingBidiFlipper.nodeFlipped()) {
            String reversed = printCssTree(cssTree);
            return LocaleInfo.class.getName() + ".getCurrentLocale().isRTL() ? "
                    + reversed + " : " + standard;
        } else {
            return standard;
        }
    }

    private String printCssTree(CssTree tree) {
        CssPrinter cssPrinterPass = new CssPrinter(tree);
        cssPrinterPass.runPass();

        return cssPrinterPass.getCompactPrintedString();
    }

    private static class ConversionResult {
        final String gss;
        final Map<String, String> defNameMapping;

        private ConversionResult(String gss, Map<String, String> defNameMapping) {
            this.gss = gss;
            this.defNameMapping = defNameMapping;
        }
    }

    private static class RenamingResult {
        final Map<String, String> mapping;
        final Set<String> externalClassCandidate;

        private RenamingResult(Map<String, String> mapping, Set<String> externalClassCandidate) {
            this.mapping = mapping;
            this.externalClassCandidate = externalClassCandidate;
        }
    }

    @Override
    public String createAssignment(TreeLogger logger, ResourceContext context, AptContext aptContext, ExecutableElement method) throws UnableToCompleteException {
        return null;
    }

    private static class CssParsingResult {
        final CssTree tree;
        final Map<String, String> originalConstantNameMapping;
        final Set<String> trueConditions;

        private CssParsingResult(CssTree tree, Set<String> trueConditions,
                                 Map<String, String> originalConstantNameMapping) {
            this.tree = tree;
            this.originalConstantNameMapping = originalConstantNameMapping;
            this.trueConditions = trueConditions;
        }
    }
}
