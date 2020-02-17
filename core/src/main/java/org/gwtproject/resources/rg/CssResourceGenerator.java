/*
 *
 * Copyright © ${year} ${name}
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gwtproject.resources.rg;

import static org.gwtproject.resources.client.CssResource.ClassName;
import static org.gwtproject.resources.client.CssResource.Import;
import static org.gwtproject.resources.client.CssResource.ImportedWithPrefix;
import static org.gwtproject.resources.client.CssResource.NotStrict;
import static org.gwtproject.resources.client.CssResource.Shared;
import static org.gwtproject.resources.client.CssResource.Strict;
import static org.gwtproject.resources.rg.css.ast.CssProperty.DotPathValue;
import static org.gwtproject.resources.rg.css.ast.CssProperty.ListValue;
import static org.gwtproject.resources.rg.css.ast.CssProperty.NumberValue;
import static org.gwtproject.resources.rg.css.ast.CssProperty.Value;
import static org.gwtproject.resources.rg.resource.ConfigurationProperties.KEY_CSS_RESOURCE_ENABLE_GSS;
import static org.gwtproject.resources.rg.resource.ConfigurationProperties.KEY_CSS_RESOURCE_MERGE_ENABLED;
import static org.gwtproject.resources.rg.resource.ConfigurationProperties.KEY_CSS_RESOURCE_OBFUSCATION_PREFIX;
import static org.gwtproject.resources.rg.resource.ConfigurationProperties.KEY_CSS_RESOURCE_STYLE;

import com.google.auto.common.MoreElements;
import com.google.auto.common.MoreTypes;
import com.google.common.base.Joiner;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.Adler32;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.gwtproject.i18n.shared.cldr.LocaleInfo;
import org.gwtproject.resources.client.CssResource;
import org.gwtproject.resources.client.CssResourceBase;
import org.gwtproject.resources.ext.NotFoundException;
import org.gwtproject.resources.ext.PropertyOracle;
import org.gwtproject.resources.ext.ResourceContext;
import org.gwtproject.resources.ext.ResourceGeneratorUtil;
import org.gwtproject.resources.ext.TreeLogger;
import org.gwtproject.resources.ext.UnableToCompleteException;
import org.gwtproject.resources.rg.css.ClassRenamer;
import org.gwtproject.resources.rg.css.CssGenerationVisitor;
import org.gwtproject.resources.rg.css.DefsCollector;
import org.gwtproject.resources.rg.css.ExternalClassesCollector;
import org.gwtproject.resources.rg.css.GenerateCssAst;
import org.gwtproject.resources.rg.css.IfEvaluator;
import org.gwtproject.resources.rg.css.MergeIdenticalSelectorsVisitor;
import org.gwtproject.resources.rg.css.MergeRulesByContentVisitor;
import org.gwtproject.resources.rg.css.RtlVisitor;
import org.gwtproject.resources.rg.css.SplitRulesVisitor;
import org.gwtproject.resources.rg.css.Spriter;
import org.gwtproject.resources.rg.css.SubstitutionCollector;
import org.gwtproject.resources.rg.css.SubstitutionReplacer;
import org.gwtproject.resources.rg.css.ast.CollapsedNode;
import org.gwtproject.resources.rg.css.ast.CssCompilerException;
import org.gwtproject.resources.rg.css.ast.CssDef;
import org.gwtproject.resources.rg.css.ast.CssIf;
import org.gwtproject.resources.rg.css.ast.CssNode;
import org.gwtproject.resources.rg.css.ast.CssProperty;
import org.gwtproject.resources.rg.css.ast.CssRule;
import org.gwtproject.resources.rg.css.ast.CssStylesheet;
import org.gwtproject.resources.rg.css.ast.CssSubstitution;
import org.gwtproject.resources.rg.css.ast.HasNodes;
import org.gwtproject.resources.rg.util.DefaultTextOutput;
import org.gwtproject.resources.rg.util.SourceWriter;
import org.gwtproject.resources.rg.util.StringSourceWriter;
import org.gwtproject.resources.rg.util.Util;

/** @author Dmitrii Tikhomirov Created by treblereel 11/15/18 */
public class CssResourceGenerator extends AbstractCssResourceGenerator {

  /**
   * A lookup table of base-32 chars we use to encode CSS idents. Because CSS class selectors may be
   * case-insensitive, we don't have enough characters to use a base-64 encoding.
   *
   * <p>Note that the character ESERVED_IDENT_CHAR is intentionally missing from this array. It is
   * used to prefix identifiers produced by {@link #makeIdent} if they conflict with reserved
   * class-name prefixes.
   */
  static final char[] BASE32_CHARS =
      new char[] {
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N',
        'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', '-', '0', '1',
        '2', '3', '4', '5'
      };
  /**
   * This value is used by {@link #concatOp} to help create a more balanced AST tree by producing
   * parenthetical expressions.
   */
  private static final int CONCAT_EXPRESSION_LIMIT = 20;
  /** This character must not appear in {@link #BASE32_CHARS}. */
  private static final char RESERVED_IDENT_CHAR = 'Z';
  /** These constants are used to cache obfuscated class names. */
  private static final String KEY_BY_CLASS_AND_METHOD = "classAndMethod";

  private static final String KEY_CLASS_PREFIX = "prefix";
  private static final String KEY_CLASS_COUNTER = "counter";
  private static final String KEY_HAS_CACHED_DATA = "hasCachedData";
  private static final String KEY_RESERVED_PREFIXES = "CssResource.reservedClassPrefixes";
  private static final String KEY_SHARED_METHODS = "sharedMethods";

  protected CssObfuscationStyle obfuscationStyle;
  private Counter classCounter;
  private boolean enableMerge;
  private boolean gssEnabled;
  private GssResourceGenerator gssResourceGenerator;

  private List<String> ignoredMethods = new ArrayList<>();
  private Map<TypeElement, Map<ExecutableElement, String>> replacementsByClassAndMethod;
  private Map<ExecutableElement, String> replacementsForSharedMethods;
  private Map<ExecutableElement, CssStylesheet> stylesheetMap = new IdentityHashMap<>();

  private static String makeIdent(long id) {
    assert id >= 0;

    StringBuilder b = new StringBuilder();

    // Use only guaranteed-alpha characters for the first character
    b.append(BASE32_CHARS[(int) (id & 0xf)]);
    id >>= 4;

    while (id != 0) {
      b.append(BASE32_CHARS[(int) (id & 0x1f)]);
      id >>= 5;
    }

    return b.toString();
  }

  /**
   * Create a Java expression that evaluates to a string representation of the given node. Visible
   * only for testing.
   */
  static <T extends CssNode & HasNodes> String makeExpression(
      TreeLogger logger, ResourceContext context, T node, boolean prettyOutput)
      throws UnableToCompleteException {
    Types types = context.getGeneratorContext().getAptContext().types;
    Elements elements = context.getGeneratorContext().getAptContext().elements;

    // Generate the CSS template
    DefaultTextOutput out = new DefaultTextOutput(!prettyOutput);
    CssGenerationVisitor v = new CssGenerationVisitor(out);
    v.accept(node);

    // Generate the final Java expression
    String template = out.toString();
    StringBuilder b = new StringBuilder();
    int start = 0;

    /*
     * Very large concatenation expressions using '+' cause the GWT compiler to
     * overflow the stack due to deep AST nesting. The workaround for now is to
     * force it to be more balanced using intermediate concatenation groupings.
     *
     * This variable is used to track the number of subexpressions within the
     * current parenthetical expression.
     */
    int numExpressions = 0;
    b.append('(');

    for (Map.Entry<Integer, List<CssSubstitution>> entry :
        v.getSubstitutionPositions().entrySet()) {
      // Add the static section between start and the substitution point
      b.append('"');
      b.append(Generator.escape(template.substring(start, entry.getKey())));
      b.append('\"');
      numExpressions = concatOp(numExpressions, b);

      // Add the nodes at the substitution point
      for (CssSubstitution x : entry.getValue()) {
        TreeLogger loopLogger =
            logger.branch(TreeLogger.DEBUG, "Performing substitution in node " + x.toString());

        if (x instanceof CssIf) {
          CssIf asIf = (CssIf) x;
          // Generate the sub-expressions
          String expression =
              makeExpression(loopLogger, context, new CollapsedNode(asIf), prettyOutput);
          String elseExpression;
          if (asIf.getElseNodes().isEmpty()) {
            // We'll treat an empty else block as an empty string
            elseExpression = "\"\"";
          } else {
            elseExpression =
                makeExpression(
                    loopLogger, context, new CollapsedNode(asIf.getElseNodes()), prettyOutput);
          }

          // ((expr) ? "CSS" : "elseCSS") +
          b.append(
              "((" + asIf.getExpression() + ") ? " + expression + " : " + elseExpression + ") ");
          numExpressions = concatOp(numExpressions, b);

        } else if (x instanceof CssProperty) {
          CssProperty property = (CssProperty) x;
          validateValue(
              loopLogger, context.getClientBundleType(), property.getValues(), types, elements);
          // (expr) +
          b.append("(" + property.getValues().getExpression() + ") ");
          numExpressions = concatOp(numExpressions, b);
        } else {
          // This indicates that some unexpected node is slipping by our visitors
          loopLogger.log(TreeLogger.ERROR, "Unhandled substitution " + x.getClass());
          throw new UnableToCompleteException();
        }
      }
      start = entry.getKey();
    }
    // Add the remaining parts of the template
    b.append('"');
    b.append(Generator.escape(template.substring(start)));
    b.append('"');
    b.append(')');

    return b.toString();
  }

  /**
   * Returns <code>true</code> if <code>target</code> starts with any of the prefixes in the
   * supplied set. The check is performed in a case-insensitive manner, assuming that the values in
   * <code>prefixes</code> have already been converted to lower-case.
   */
  private static String stringStartsWithAny(String target, SortedSet<String> prefixes) {
    if (prefixes.isEmpty()) {
      return null;
    }
    /*
     * The headSet() method returns values strictly less than the search value,
     * so we want to append a trailing character to the end of the search in
     * case the obfuscated class name is exactly equal to one of the prefixes.
     */
    String search = target.toLowerCase(Locale.ROOT) + " ";
    SortedSet<String> headSet = prefixes.headSet(search);
    if (!headSet.isEmpty()) {
      String prefix = headSet.last();
      if (search.startsWith(prefix)) {
        return prefix;
      }
    }
    return null;
  }

  /** This function validates any context-sensitive Values. */
  private static void validateValue(
      TreeLogger logger,
      TypeElement resourceBundleType,
      Value value,
      Types types,
      Elements elements)
      throws UnableToCompleteException {
    ListValue list = value.isListValue();
    if (list != null) {
      for (Value v : list.getValues()) {
        validateValue(logger, resourceBundleType, v, types, elements);
      }
      return;
    }
    DotPathValue dot = value.isDotPathValue();
    if (dot != null) {
      try {
        // This will either succeed or throw an exception
        ResourceGeneratorUtil.getMethodByPath(
            resourceBundleType, dot.getParts(), null, types, elements);
      } catch (NotFoundException e) {
        logger.log(TreeLogger.ERROR, e.getMessage());
        throw new UnableToCompleteException();
      }
    }
  }

  /**
   * Check if number of concat expressions currently exceeds limit and either append '+' if the
   * limit isn't reached or ') + (' if it is.
   *
   * @return numExpressions + 1 or 0 if limit was exceeded.
   */
  private static int concatOp(int numExpressions, StringBuilder b) {
    /*
     * TODO: Fix the compiler to better handle arbitrarily long concatenation
     * expressions.
     */
    if (numExpressions >= CONCAT_EXPRESSION_LIMIT) {
      b.append(") + (");
      return 0;
    }

    b.append(" + ");
    return numExpressions + 1;
  }

  /**
   * Builds a CSV file mapping obfuscated CSS class names to their qualified source name and outputs
   * it as a private build artifact.
   */
  protected static void outputCssMapArtifact(
      TreeLogger logger,
      ResourceContext context,
      ExecutableElement method,
      Map<ExecutableElement, String> actualReplacements) {
    // There may be several css resources that have the same css resource subtype (e.g. CssResource)
    // so the qualified accessor method name is used for the unique output file name.
    TypeElement bundleType = (TypeElement) method.getEnclosingElement();

    String qualifiedMethodName =
        bundleType.getQualifiedName() + "." + method.getSimpleName().toString();

    String mappingFileName = "cssResource/" + qualifiedMethodName + ".cssmap";

    OutputStream os = null;
    try {
      os = context.getGeneratorContext().tryCreateResource(logger, mappingFileName);
    } catch (UnableToCompleteException e) {
      logger.log(TreeLogger.WARN, "Could not create resource: " + mappingFileName);
      return;
    }

    if (os == null) {
      // If the returned OutputStream is null, that typically means the resource already exists.
      // No need to write it out again.
      return;
    }

    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
    try {
      for (Map.Entry<ExecutableElement, String> replacement : actualReplacements.entrySet()) {

        String qualifiedName =
            ((TypeElement) replacement.getKey().getEnclosingElement())
                .getQualifiedName()
                .toString();
        String baseName = replacement.getKey().getSimpleName().toString();
        writer.write(qualifiedName.replaceAll("[.$]", "-") + "-" + baseName);
        writer.write(",");
        writer.write(replacement.getValue());
        writer.newLine();
      }
      writer.flush();
      writer.close();
    } catch (IOException e) {
      logger.log(TreeLogger.WARN, "Error writing artifact: " + mappingFileName);
    }

    try {
      context.getGeneratorContext().commitResource(logger, os);
    } catch (UnableToCompleteException e) {
      logger.log(TreeLogger.WARN, "Error trying to commit artifact: " + mappingFileName);
    }
  }

  /**
   * Compute an obfuscated CSS class name that is guaranteed not to conflict with a set of reserved
   * prefixes. Visible for testing.
   */
  static String computeObfuscatedClassName(
      String classPrefix, Counter classCounter, SortedSet<String> reservedPrefixes) {
    String obfuscatedClassName = classPrefix + makeIdent(classCounter.next());

    /*
     * Ensure that the name won't conflict with any reserved prefixes. We can't
     * just keep incrementing the counter, because that could take an
     * arbitrarily long amount of time to return a good value.
     */
    String conflict = stringStartsWithAny(obfuscatedClassName, reservedPrefixes);
    while (conflict != null) {
      Adler32 hash = new Adler32();
      hash.update(Util.getBytes(conflict));
      /*
       * Compute a new prefix for the identifier to mask the prefix and add the
       * reserved identifier character to prevent conflicts with makeIdent().
       *
       * Assuming "gwt-" is a reserved prefix: gwt-A -> ab32ZA
       */
      String newPrefix =
          makeIdent(hash.getValue()).substring(0, conflict.length()) + RESERVED_IDENT_CHAR;
      obfuscatedClassName = newPrefix + obfuscatedClassName.substring(conflict.length());
      conflict = stringStartsWithAny(obfuscatedClassName, reservedPrefixes);
    }

    return obfuscatedClassName;
  }

  public static boolean haveCommonProperties(CssRule a, CssRule b) {
    if (a.getProperties().size() == 0 || b.getProperties().size() == 0) {
      return false;
    }

    SortedSet<String> aProperties = new TreeSet<>();
    SortedSet<String> bProperties = new TreeSet<>();

    for (CssProperty p : a.getProperties()) {
      aProperties.add(p.getName());
    }
    for (CssProperty p : b.getProperties()) {
      bProperties.add(p.getName());
    }

    Iterator<String> ai = aProperties.iterator();
    Iterator<String> bi = bProperties.iterator();

    String aName = ai.next();
    String bName = bi.next();
    for (; ; ) {
      int comp = aName.compareToIgnoreCase(bName);
      if (comp == 0) {
        return true;
      } else if (comp > 0) {
        if (aName.startsWith(bName + "-")) {
          return true;
        }

        if (!bi.hasNext()) {
          break;
        }
        bName = bi.next();
      } else {
        if (bName.startsWith(aName + "-")) {
          return true;
        }
        if (!ai.hasNext()) {
          break;
        }
        aName = ai.next();
      }
    }

    return false;
  }

  protected String getCssExpression(
      TreeLogger logger, ResourceContext context, ExecutableElement method)
      throws UnableToCompleteException {
    return makeExpression(logger, context, stylesheetMap.get(method));
  }

  /**
   * Create a Java expression that evaluates to the string representation of the stylesheet
   * resource.
   */
  private String makeExpression(TreeLogger logger, ResourceContext context, CssStylesheet sheet)
      throws UnableToCompleteException {
    try {
      String standard = makeExpression(logger, context, sheet, obfuscationStyle.isPretty());
      (new RtlVisitor()).accept(sheet);
      String reversed = makeExpression(logger, context, sheet, obfuscationStyle.isPretty());
      if (standard.equals(reversed)) {
        return standard;
      } else {
        return LocaleInfo.class.getName()
            + ".getCurrentLocale().isRTL() ? ("
            + reversed
            + ") : ("
            + standard
            + ")";
      }

    } catch (CssCompilerException e) {
      // Take this as a sign that one of the visitors was unhappy, but only
      // log the stack trace if there's a causal (i.e. unknown) exception.
      logger.log(TreeLogger.ERROR, "Unable to process CSS", e.getCause() == null ? null : e);
      throw new UnableToCompleteException();
    }
  }

  @Override
  public String createAssignment(
      TreeLogger logger, ResourceContext context, ExecutableElement method)
      throws UnableToCompleteException {
    // if Gss is enabled, defer the call to the Gss generator.
    if (gssEnabled) {
      return gssResourceGenerator.createAssignment(logger, context, method);
    }

    TypeMirror cssResourceSubtype = method.getReturnType();
    assert MoreTypes.asElement(cssResourceSubtype).getKind().equals(ElementKind.INTERFACE);
    CssStylesheet stylesheet = stylesheetMap.get(method);

    // Optimize the stylesheet, recording the class selector obfuscations
    Map<ExecutableElement, String> actualReplacements = optimize(logger, context, method);
    // outputCssMapArtifact(logger, context, method, actualReplacements);

    outputAdditionalArtifacts(
        logger,
        context,
        method,
        actualReplacements,
        MoreTypes.asTypeElement(cssResourceSubtype),
        stylesheet);
    String result =
        getResourceImplAsString(
            logger,
            context,
            method,
            actualReplacements,
            MoreTypes.asTypeElement(cssResourceSubtype),
            stylesheet);
    return result;
  }

  protected String getResourceImplAsString(
      TreeLogger logger,
      ResourceContext context,
      ExecutableElement method,
      Map<ExecutableElement, String> actualReplacements,
      TypeElement cssResourceSubtype,
      CssStylesheet stylesheet)
      throws UnableToCompleteException {
    Types type = context.getGeneratorContext().getAptContext().types;
    Elements elements = context.getGeneratorContext().getAptContext().elements;

    SourceWriter sw = new StringSourceWriter();
    // Write the expression to create the subtype.
    sw.println(
        "new "
            + ((TypeElement) MoreTypes.asElement(method.getReturnType())).getQualifiedName()
            + "() {");
    sw.indent();
    // Methods defined by CssResource interface
    writeEnsureInjected(sw);
    writeGetName(method, sw);
    // Create the Java expression that generates the CSS
    writeGetText(logger, context, method, sw);
    /*        List<Element> methodsList = cssResourceSubtype.getEnclosedElements().stream().
    filter(element -> element.getKind().
            equals(ElementKind.METHOD)).collect(Collectors.toList());*/
    Set<ExecutableElement> methodsSet =
        MoreElements.getLocalAndInheritedMethods(cssResourceSubtype, type, elements);
    // getOverridableMethods is used to handle CssResources extending
    // non-CssResource types. See the discussion in computeReplacementsForType.
    ExecutableElement[] methods = methodsSet.toArray(new ExecutableElement[methodsSet.size()]);
    writeUserMethods(logger, sw, stylesheet, methods, actualReplacements);

    sw.outdent();
    sw.println("}");

    return sw.toString();
  }

  /** Write all of the user-defined methods in the CssResource subtype. */
  protected void writeUserMethods(
      TreeLogger logger,
      SourceWriter sw,
      CssStylesheet sheet,
      ExecutableElement[] methods,
      Map<ExecutableElement, String> obfuscatedClassNames)
      throws UnableToCompleteException {

    // Get list of @defs
    DefsCollector collector = new DefsCollector();
    collector.accept(sheet);
    Set<String> defs = collector.getDefs();

    for (ExecutableElement toImplement : methods) {

      String name = toImplement.getSimpleName().toString();
      if (ignoredMethods.contains(name)) {
        continue;
      }

      // Bomb out if there is a collision between @def and a style name
      if (defs.contains(name) && obfuscatedClassNames.containsKey(toImplement)) {
        logger.log(
            TreeLogger.ERROR,
            "@def shadows CSS class name: "
                + name
                + ". Fix by renaming the @def name or the CSS class name.");
        throw new UnableToCompleteException();
      }

      if (defs.contains(toImplement.getSimpleName().toString())
          && toImplement.getParameters().size() == 0) {
        writeDefAssignment(logger, sw, toImplement, sheet);
      } else if (toImplement.getReturnType().toString().equals("java.lang.String")
          && toImplement.getParameters().size() == 0) {
        writeClassAssignment(sw, toImplement, obfuscatedClassNames);
      } else {
        logger.log(TreeLogger.ERROR, "Don't know how to implement method " + toImplement);
        throw new UnableToCompleteException();
      }
    }
  }

  private void writeDefAssignment(
      TreeLogger logger,
      SourceWriter sw,
      ExecutableElement toImplement,
      CssStylesheet cssStylesheet)
      throws UnableToCompleteException {

    SubstitutionCollector collector = new SubstitutionCollector();
    collector.accept(cssStylesheet);
    String name = toImplement.getSimpleName().toString();
    // TODO: Annotation for override

    CssDef def = collector.getSubstitutions().get(name);
    if (def == null) {
      logger.log(TreeLogger.ERROR, "No @def rule for name " + name);
      throw new UnableToCompleteException();
    }
    assert toImplement.getReturnType().getKind().isPrimitive()
        || toImplement.getReturnType().getKind().toString().equals("java.lang.String");

    if (def.getValues().size() != 1 && !isReturnTypeString(toImplement.getReturnType())) {
      logger.log(
          TreeLogger.ERROR,
          "@def rule " + name + " must define exactly one value or return type must be String");
      throw new UnableToCompleteException();
    }
    String returnExpr = "";
    if (isReturnTypeString(toImplement.getReturnType())) {
      List<String> returnValues = new ArrayList<String>();
      for (Value val : def.getValues()) {
        returnValues.add(Generator.escape(val.toString()));
      }
      returnExpr = "\"" + Joiner.on(" ").join(returnValues) + "\"";
    } else {
      TypeMirror returnType = toImplement.getReturnType();
      if (returnType == null) {
        logger.log(
            TreeLogger.ERROR,
            toImplement.getSimpleName()
                + ": Return type must be primitive type or String for "
                + "@def accessors");
        throw new UnableToCompleteException();
      }

      NumberValue numberValue = def.getValues().get(0).isNumberValue();
      if (returnType.getKind() == TypeKind.INT || returnType.getKind() == TypeKind.LONG) {
        returnExpr = "" + Math.round(numberValue.getValue());
      } else if (returnType.getKind() == TypeKind.FLOAT) {
        returnExpr = numberValue.getValue() + "F";
      } else if (returnType.getKind() == TypeKind.DOUBLE) {
        returnExpr = "" + numberValue.getValue();
      } else {
        logger.log(
            TreeLogger.ERROR,
            returnType + " is not a valid primitive return type for @def accessors");
        throw new UnableToCompleteException();
      }
    }
    writeSimpleGetter(toImplement, returnExpr, sw);
  }

  /** Write the CssResource accessor method for simple String return values. */
  private void writeClassAssignment(
      SourceWriter sw,
      ExecutableElement toImplement,
      Map<ExecutableElement, String> classReplacements) {
    String replacement = classReplacements.get(toImplement);
    assert replacement != null : "Missing replacement for " + toImplement.getSimpleName();

    writeSimpleGetter(toImplement, "\"" + replacement + "\"", sw);
  }

  /**
   * Output additional artifacts. Does nothing in this baseclass, but is a hook for subclasses to do
   * so.
   */
  protected void outputAdditionalArtifacts(
      TreeLogger logger,
      ResourceContext context,
      ExecutableElement method,
      Map<ExecutableElement, String> actualReplacements,
      TypeElement cssResourceSubtype,
      CssStylesheet stylesheet) {}

  private Map<ExecutableElement, String> optimize(
      TreeLogger logger, ResourceContext context, ExecutableElement method)
      throws UnableToCompleteException {
    // Compute the local effective namespace
    TypeElement cssResourceSubtype = (TypeElement) MoreTypes.asElement(method.getReturnType());
    Map<String, Map<ExecutableElement, String>> classReplacementsWithPrefix =
        processImports(logger, cssResourceSubtype, method, context);

    boolean strict = isStrict(logger, method);

    CssStylesheet sheet = stylesheetMap.get(method);
    // Create CSS sprites
    new Spriter(logger, context).accept(sheet);
    SubstitutionCollector collector = new SubstitutionCollector();
    collector.accept(sheet);
    new SubstitutionReplacer(logger, context, collector.getSubstitutions()).accept(sheet);

    // locale
    // user.agent
    // formfactor
    // Evaluate @if statements based on deferred binding properties
    new IfEvaluator(logger).accept(sheet);
    // Rename css .class selectors. We look for all @external declarations in
    // the stylesheet and then compute the per-instance replacements.
    ExternalClassesCollector externalClasses = new ExternalClassesCollector();
    externalClasses.accept(sheet);

    ClassRenamer renamer =
        new ClassRenamer(logger, classReplacementsWithPrefix, strict, externalClasses.getClasses());
    renamer.accept(sheet);
    Map<ExecutableElement, String> actualReplacements =
        new TreeMap<>(
            new Comparator<ExecutableElement>() {
              @Override
              public int compare(ExecutableElement o1, ExecutableElement o2) {
                int result = source(o1).compareTo(source(o2));
                if (result == 0) {
                  result = o1.getSimpleName().toString().compareTo(o2.getSimpleName().toString());
                }
                return result;
              }

              private String source(ExecutableElement o) {
                TypeElement type = (TypeElement) o.getEnclosingElement();
                return type.getQualifiedName().toString();
              }
            });
    actualReplacements.putAll(renamer.getReplacements());
    // Combine rules with identical selectors
    if (enableMerge) {
      (new SplitRulesVisitor()).accept(sheet);
      (new MergeIdenticalSelectorsVisitor()).accept(sheet);
      (new MergeRulesByContentVisitor()).accept(sheet);
    }

    return actualReplacements;
  }

  /**
   * Process the Import annotation on the associated JMethod and return a map of prefixes to
   * JMethods to locally obfuscated names.
   */
  private Map<String, Map<ExecutableElement, String>> processImports(
      TreeLogger logger,
      TypeElement cssResourceSubtype,
      ExecutableElement method,
      ResourceContext context)
      throws UnableToCompleteException {
    Map<String, Map<ExecutableElement, String>> replacementsWithPrefix = new HashMap<>();
    replacementsWithPrefix.put("", computeReplacementsForType(cssResourceSubtype, context));

    Import imp = method.getAnnotation(Import.class);
    if (imp != null) {
      boolean fail = false;
      Elements elements = context.getGeneratorContext().getAptContext().elements;
      for (TypeMirror type : getImportype(imp)) {
        TypeElement importType = elements.getTypeElement(type.toString());
        String prefix = getImportPrefix(importType);
        if (replacementsWithPrefix.put(prefix, computeReplacementsForType(importType, context))
            != null) {
          logger.log(TreeLogger.ERROR, "Multiple imports that would use the prefix " + prefix);
          fail = true;
        }
      }
      if (fail) {
        throw new UnableToCompleteException();
      }
    }
    return replacementsWithPrefix;
  }

  /** Returns the import prefix for a type, including the trailing hyphen. */
  public static String getImportPrefix(TypeElement importType) {
    String prefix = importType.getSimpleName().toString();
    ImportedWithPrefix exp = importType.getAnnotation(ImportedWithPrefix.class);
    if (exp != null) {
      prefix = exp.value();
    }
    return prefix + "-";
  }

  /**
   * Compute the mapping of original class names to obfuscated type names for a given subtype of
   * CssResource. Mappings are inherited from the type's supertypes.
   */
  private Map<ExecutableElement, String> computeReplacementsForType(
      TypeElement type, ResourceContext context) {
    Map<ExecutableElement, String> toReturn = new IdentityHashMap<>();
    if (replacementsByClassAndMethod.containsKey(type)) {
      toReturn.putAll(replacementsByClassAndMethod.get(type));
    }

    Types types = context.getGeneratorContext().getAptContext().types;
    Elements elements = context.getGeneratorContext().getAptContext().elements;

    for (ExecutableElement method :
        MoreElements.getLocalAndInheritedMethods(type, types, elements)) {
      if (replacementsForSharedMethods.containsKey(method)) {
        assert toReturn.containsKey(method);
        toReturn.put(method, replacementsForSharedMethods.get(method));
      }
    }

    return toReturn;
  }

  private static List<? extends TypeMirror> getImportype(Import annotation) {
    try {
      annotation.value();
    } catch (MirroredTypesException mte) {
      return mte.getTypeMirrors();
    }
    return null;
  }

  /**
   * Check for the presence of the NotStrict annotation on the method. This will also perform some
   * limited sanity-checking for the now-deprecated Strict annotation.
   */
  @SuppressWarnings("deprecation")
  // keep references to deprecated Strict annotation local
  private boolean isStrict(TreeLogger logger, ExecutableElement method) {
    Strict strictAnnotation = method.getAnnotation(Strict.class);
    NotStrict nonStrictAnnotation = method.getAnnotation(NotStrict.class);
    boolean strict = true;

    if (strictAnnotation != null && nonStrictAnnotation != null) {
      // Both annotations
      logger.log(
          TreeLogger.WARN,
          "Contradictory annotations "
              + Strict.class.getName()
              + " and "
              + NotStrict.class.getName()
              + " applied to the CssResource accessor method; assuming strict");

    } else if (nonStrictAnnotation != null) {
      // Only the non-strict annotation
      strict = false;
    }
    return strict;
  }

  @Override
  public void init(TreeLogger logger, ResourceContext context) throws UnableToCompleteException {
    PropertyOracle propertyOracle = context.getGeneratorContext().getPropertyOracle();
    Types types = context.getGeneratorContext().getAptContext().types;
    Elements elements = context.getGeneratorContext().getAptContext().elements;
    gssEnabled =
        propertyOracle
            .getConfigurationProperty(logger, KEY_CSS_RESOURCE_ENABLE_GSS)
            .asSingleBooleanValue();
    if (gssEnabled) {
      // if Gss is enabled, defer the call to the Gss generator.
      GssResourceGenerator.GssOptions gssOptions =
          GssResourceGenerator.getGssOptions(context, logger);
      if (gssOptions.isEnabled()) {
        gssEnabled = true;
        gssResourceGenerator = new GssResourceGenerator(gssOptions);
        gssResourceGenerator.init(logger, context);
        return;
      }
    }

    obfuscationStyle =
        CssObfuscationStyle.getObfuscationStyle(
            propertyOracle
                .getConfigurationProperty(logger, KEY_CSS_RESOURCE_STYLE)
                .asSingleValue());
    enableMerge =
        propertyOracle
            .getConfigurationProperty(logger, KEY_CSS_RESOURCE_MERGE_ENABLED)
            .asSingleBooleanValue();
    String classPrefix =
        propertyOracle
            .getConfigurationProperty(logger, KEY_CSS_RESOURCE_OBFUSCATION_PREFIX)
            .asSingleValue();

    TypeElement superInterface = elements.getTypeElement(CssResource.class.getCanonicalName());
    TypeElement baseInterface = elements.getTypeElement(CssResourceBase.class.getCanonicalName());

    for (Element m : MoreElements.getLocalAndInheritedMethods(superInterface, types, elements)) {
      if (m.getKind().equals(ElementKind.METHOD)) {
        ignoredMethods.add(m.getSimpleName().toString());
      }
    }
    stylesheetMap = new IdentityHashMap<>();

    SortedSet<TypeElement> cssResourceSubtypes =
        computeOperableTypes(logger, baseInterface, context.getClientBundleType(), context);
    initReplacements(logger, context, classPrefix, cssResourceSubtypes);
  }

  /** This method will initialize the maps that contain the obfuscated class names. */
  @SuppressWarnings("unchecked")
  private void initReplacements(
      TreeLogger logger,
      ResourceContext context,
      String classPrefix,
      SortedSet<TypeElement> operableTypes)
      throws UnableToCompleteException {

    /*
     * This code was originally written to take a snapshot of all the
     * CssResource descendants in the TypeOracle on its first run and calculate
     * the obfuscated names in one go, to ensure that the same obfuscation would
     * result regardless of the order in which the generators fired. (It no
     * longer behaves that way, as that scheme prevented the generation of new
     * CssResource interfaces, but the complexity lives on.)
     *
     * TODO(rjrjr,bobv) These days scottb tells us we're guaranteed that the
     * recompiling the same code will fire the generators in a consistent order,
     * so the old gymnastics aren't really justified anyway. It would probably
     * be be worth the effort to simplify this.
     */
    PropertyOracle propertyOracle = context.getGeneratorContext().getPropertyOracle();

    if (context.getCachedData(KEY_HAS_CACHED_DATA, Boolean.class) != Boolean.TRUE) {
      TreeSet<String> reservedPrefixes =
          new TreeSet(
              propertyOracle.getConfigurationProperty(logger, KEY_RESERVED_PREFIXES).getValues());
      String computedPrefix = computeClassPrefix(classPrefix, operableTypes, reservedPrefixes);

      context.putCachedData(
          KEY_BY_CLASS_AND_METHOD,
          new IdentityHashMap<TypeElement, Map<ExecutableElement, String>>());
      context.putCachedData(KEY_CLASS_PREFIX, computedPrefix);
      context.putCachedData(KEY_CLASS_COUNTER, new Counter());
      context.putCachedData(KEY_HAS_CACHED_DATA, Boolean.TRUE);
      context.putCachedData(KEY_RESERVED_PREFIXES, reservedPrefixes);
      context.putCachedData(KEY_SHARED_METHODS, new IdentityHashMap<ExecutableElement, String>());
    }

    classCounter = context.getCachedData(KEY_CLASS_COUNTER, Counter.class);
    replacementsByClassAndMethod = context.getCachedData(KEY_BY_CLASS_AND_METHOD, Map.class);
    replacementsForSharedMethods = context.getCachedData(KEY_SHARED_METHODS, Map.class);

    classPrefix = context.getCachedData(KEY_CLASS_PREFIX, String.class);
    SortedSet<String> reservedPrefixes =
        context.getCachedData(KEY_RESERVED_PREFIXES, SortedSet.class);
    computeObfuscatedNames(logger, classPrefix, reservedPrefixes, operableTypes, context);
  }

  /**
   * Determine the class prefix that will be used. If a value is automatically computed, the <code>
   * reservedPrefixes</code> set will be cleared because the returned value is guaranteed to not
   * conflict with any reserved prefixes.
   */
  private String computeClassPrefix(
      String classPrefix,
      SortedSet<TypeElement> cssResourceSubtypes,
      TreeSet<String> reservedPrefixes) {
    if ("default".equals(classPrefix)) {
      classPrefix = null;
    } else if ("empty".equals(classPrefix)) {
      classPrefix = "";
    }

    if (classPrefix == null) {
      /*
       * Note that the checksum will miss some or all of the subtypes generated
       * by other generators.
       */
      Adler32 checksum = new Adler32();
      for (TypeElement type : cssResourceSubtypes) {
        checksum.update(Util.getBytes(type.getQualifiedName().toString()));
      }

      final int seed = Math.abs((int) checksum.getValue());
      classPrefix =
          "G"
              + computeObfuscatedClassName(
                  "",
                  new Counter() {
                    @Override
                    int next() {
                      return seed;
                    }
                  },
                  reservedPrefixes);

      // No conflicts are possible now
      reservedPrefixes.clear();
    }
    return classPrefix;
  }

  private void computeObfuscatedNames(
      TreeLogger logger,
      String classPrefix,
      SortedSet<String> reservedPrefixes,
      Set<TypeElement> cssResourceSubtypes,
      ResourceContext context) {
    Types types = context.getGeneratorContext().getAptContext().types;
    Elements elements = context.getGeneratorContext().getAptContext().elements;

    // ResourceGeneratorUtil.getAllParents()

    for (TypeElement type : cssResourceSubtypes) {
      if (type.toString().equals("org.gwtproject.resources.client.CssResource")
          || type.toString().equals("org.gwtproject.resources.client.CssResourceBase")
          || type.toString().equals("org.gwtproject.resources.client.ResourcePrototype")) {
        continue;
      }

      if (replacementsByClassAndMethod.containsKey(type)) {
        continue;
      }
      Map<ExecutableElement, String> replacements = new IdentityHashMap<>();
      replacementsByClassAndMethod.put(type, replacements);

      for (Element method : MoreElements.getLocalAndInheritedMethods(type, types, elements)) {

        String name = method.getSimpleName().toString();
        if (ignoredMethods.contains(name)) {
          continue;
        }

        // The user provided the class name to use
        ClassName classNameOverride = method.getAnnotation(ClassName.class);
        if (classNameOverride != null) {
          name = classNameOverride.value();
        }

        /*
         * Short name, based off a counter that is client by all of the
         * obfuscated css names in this compile.
         */
        String obfuscatedClassName =
            computeObfuscatedClassName(classPrefix, classCounter, reservedPrefixes);

        // Modify the name based on the obfuscation style requested //TODO Check
        obfuscatedClassName = obfuscationStyle.getPrettyName(name, type, obfuscatedClassName);

        if (method.getKind().equals(ElementKind.METHOD))
          replacements.put((ExecutableElement) method, obfuscatedClassName);

        if (isSameType(method.getEnclosingElement(), type, types)) {
          Shared shared = type.getAnnotation(Shared.class);
          if (shared != null) {
            if (method.getKind().equals(ElementKind.METHOD))
              replacementsForSharedMethods.put((ExecutableElement) method, obfuscatedClassName);
          }
        }

        logger.log(
            TreeLogger.DEBUG,
            "Mapped "
                + type.getQualifiedName().toString()
                + "."
                + name
                + " to "
                + obfuscatedClassName);
      }
    }
  }

  public boolean isSameType(Element e1, Element e2, Types types) {
    return types.isSameType(e1.asType(), e2.asType());
  }

  /**
   * Returns all interfaces derived from CssResource, sorted by qualified name.
   *
   * <p>We'll ignore concrete implementations of CssResource, which include types
   * previously-generated by CssResourceGenerator and user-provided implementations of CssResource,
   * which aren't valid for use with CssResourceGenerator anyway. By ignoring newly-generated
   * CssResource types, we'll ensure a stable ordering, regardless of the actual execution order
   * used by the Generator framework.
   *
   * <p>It is still possible that additional pure-interfaces could be introduced by other
   * generators, which would change the result of this computation, but there is presently no way to
   * determine when, or by what means, a type was added to the TypeOracle. TODO or scan return types
   * ???
   */
  private SortedSet<TypeElement> computeOperableTypes(
      TreeLogger logger, TypeElement baseInterface, TypeElement bundle, ResourceContext context) {
    Types types = context.getGeneratorContext().getAptContext().types;
    Elements elements = context.getGeneratorContext().getAptContext().elements;
    logger = logger.branch(TreeLogger.DEBUG, "Finding operable CssResource subtypes");

    SortedSet<TypeElement> toReturn = new TreeSet<>(new JClassOrderComparator());
    for (Element elm : bundle.getEnclosedElements()) {
      if (elm.getKind().equals(ElementKind.METHOD)) {
        ExecutableElement method = (ExecutableElement) elm;

        Import imp = method.getAnnotation(Import.class);
        if (imp != null) {
          boolean fail = false;
          for (TypeMirror type : getImportype(imp)) {
            if (types.isSubtype(type, baseInterface.asType())) {
              TypeElement imported = elements.getTypeElement(type.toString());
              toReturn.add(imported);
            }
          }
        }
        if (types.isSubtype(method.getReturnType(), baseInterface.asType())) {
          if (logger.isLoggable(TreeLogger.SPAM)) {
            logger.log(TreeLogger.SPAM, "Added " + method);
          }
          toReturn.add(MoreTypes.asTypeElement(method.getReturnType()));
          ResourceGeneratorUtil.getAllParents(MoreTypes.asTypeElement(method.getReturnType()))
              .forEach(p -> toReturn.add(MoreTypes.asTypeElement(p)));
        } else {
          if (logger.isLoggable(TreeLogger.SPAM)) {
            logger.log(TreeLogger.SPAM, "Ignored " + method);
          }
        }
      } else if (elm.getKind().equals(ElementKind.INTERFACE)) {
        toReturn.add((TypeElement) elm);
      }
    }

    return toReturn;
  }

  @Override
  public void prepare(TreeLogger logger, ResourceContext context, ExecutableElement method)
      throws UnableToCompleteException {
    // if Gss is enabled, defer the call to the Gss generator.
    if (gssEnabled) {
      gssResourceGenerator.prepare(logger, context, method);
      return;
    }

    if (!MoreTypes.asElement(method.getReturnType()).getKind().equals(ElementKind.INTERFACE)) {
      logger.log(TreeLogger.ERROR, "Return type must be an interface");
      throw new UnableToCompleteException();
    }
    URL[] resources = getResources(logger, context, method);
    if (resources.length == 0) {
      logger.log(TreeLogger.ERROR, "At least one source must be specified");
      throw new UnableToCompleteException();
    }

    // At this point, gss is not enabled so we shouldn't try to compile a gss file.
    ensureNoGssFile(resources, logger);
    // Create the AST and do a quick scan for requirements
    CssStylesheet sheet = GenerateCssAst.exec(logger, resources);
    checkSheet(logger, sheet);
    stylesheetMap.put(method, sheet);
  }

  protected void checkSheet(TreeLogger logger, CssStylesheet stylesheet) {
    // Do nothing
  }

  private void ensureNoGssFile(URL[] resources, TreeLogger logger)
      throws UnableToCompleteException {
    for (URL stylesheet : resources) {
      if (stylesheet.getFile().endsWith(".gss")) {
        logger.log(
            TreeLogger.Type.ERROR,
            "GSS is not enabled. Add the following line to your gwt.xml file "
                + "to enable it: "
                + "<set-configuration-property name=\"CssResource.enableGss\" value=\"true\" />");
        throw new UnableToCompleteException();
      }
    }
  }

  protected URL[] getResources(TreeLogger logger, ResourceContext context, ExecutableElement method)
      throws UnableToCompleteException {
    return GssResourceGenerator.findResources(logger, context, method, false);
  }

  @SuppressWarnings("serial")
  static class JClassOrderComparator implements Comparator<TypeElement>, Serializable {
    @Override
    public int compare(TypeElement o1, TypeElement o2) {
      return o1.getQualifiedName().toString().compareTo(o2.getQualifiedName().toString());
    }
  }
}
