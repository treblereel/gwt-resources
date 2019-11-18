package org.gwtproject.resources;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.gwtproject.resources.rg.CssClassNamesTestCase;
import org.gwtproject.resources.rg.css.*;

/** @author Dmitrii Tikhomirov Created by treblereel 11/30/18 */
public class ResourcesJreSuite {
  public static Test suite() {

    TestSuite suite = new TestSuite("JRE test for com.google.gwt.resources");
    suite.addTestSuite(CssClassNamesTestCase.class);
    suite.addTestSuite(CssExternalTest.class);
    suite.addTestSuite(CssNodeClonerTest.class);
    // suite.addTestSuite(CssReorderTest.class);
    // suite.addTestSuite(CssRtlTest.class);
    // suite.addTestSuite(ExtractClassNamesVisitorTest.class);
    // suite.addTestSuite(ResourceGeneratorUtilTest.class);
    suite.addTestSuite(UnknownAtRuleTest.class);

    // GSS tests
    /*        suite.addTestSuite(ExternalClassesCollectorTest.class);
    suite.addTestSuite(RenamingSubstitutionMapTest.class);
    suite.addTestSuite(ImageSpriteCreatorTest.class);
    suite.addTestSuite(ClassNamesCollectorTest.class);
    suite.addTestSuite(CssPrinterTest.class);
    suite.addTestSuite(PermutationsCollectorTest.class);
    suite.addTestSuite(RecordingBidiFlipperTest.class);
    suite.addTestSuite(ResourceUrlFunctionTest.class);
    suite.addTestSuite(ExtendedEliminateConditionalNodesTest.class);
    suite.addTestSuite(PermutationsCollectorTest.class);
    suite.addTestSuite(ResourceUrlFunctionTest.class);
    suite.addTestSuite(RuntimeConditionalBlockCollectorTest.class);
    suite.addTestSuite(ValidateRuntimeConditionalNodeTest.class);
    suite.addTestSuite(ValueFunctionTest.class);
    suite.addTestSuite(BooleanConditionCollectorTest.class);*/

    // CSS to GSS converter tests
    /*        suite.addTestSuite(Css2GssTest.class);
    suite.addTestSuite(CssOutputTestCase.class);
    suite.addTestSuite(DefCollectorVisitorTest.class);
    suite.addTestSuite(ElseNodeCreatorTest.class);
    suite.addTestSuite(AlternateAnnotationCreatorVisitorTest.class);
    suite.addTestSuite(UndefinedConstantVisitorTest.class);*/

    return suite;
  }
}
