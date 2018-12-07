package org.gwtproject.resources.apt;

import com.google.auto.common.MoreElements;
import org.gwtproject.resources.context.AptContext;

import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * @author Dmitrii Tikhomirov
 * Created by treblereel 12/1/18
 */
public class ClientBundleFactoryBuilder {
    private final Map<TypeElement, String> generatedSimpleSourceNames;
    private final AptContext aptContext;
    private final String BUILDER_CLASS_NAME = "ClientBundleFactory";
    private final String BUILDER_PACKAGE_NAME = "org.gwtproject.resources.client";

    ClientBundleFactoryBuilder(Map<TypeElement, String> generatedSimpleSourceNames, AptContext aptContext) {
        this.generatedSimpleSourceNames = generatedSimpleSourceNames;
        this.aptContext = aptContext;
    }

    void build() {
        JavaFileObject builderFile = null;
        try {
            builderFile = aptContext.filer.createSourceFile(BUILDER_PACKAGE_NAME + "." + BUILDER_CLASS_NAME);


            try (PrintWriter out = new PrintWriter(builderFile.openWriter())) {
                out.print("package ");
                out.print(BUILDER_PACKAGE_NAME);
                out.println(";");
                out.println("import java.util.Map;");
                out.println("import java.util.HashMap;");

                out.println();

                out.print("public class ");
                out.print(BUILDER_CLASS_NAME);
                out.println(" {");
                out.println();

                out.print("  private static ");
                out.print("Map<Class, Object>");
                out.print(" map = new ");
                out.print("HashMap<>");
                out.print("();");
                out.println();

                out.println("   static {");
                generatedSimpleSourceNames.forEach((k, v) -> {
                    out.println("     map.put(" + k + ".class, new " + MoreElements.getPackage(k) + "." + v + "());");
                });
                out.println("   }");

                out.println("  public static <T> T get(Class clazz) {");
                out.println("  return (T) map.get(clazz);");


                out.println("  }");
                out.println("}");

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
