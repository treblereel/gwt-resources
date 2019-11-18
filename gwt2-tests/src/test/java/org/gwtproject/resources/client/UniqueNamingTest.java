package org.gwtproject.resources.client;

import com.google.gwt.junit.client.GWTTestCase;

public class UniqueNamingTest extends GWTTestCase {

  static class OuterA {

    static class BundleHolder {

      @Resource
      interface Resources extends ClientBundle {
        @Source("hello.txt")
        TextResource hello();
      }
    }
  }

  static class OuterB {

    static class BundleHolder {

      @Resource
      interface Resources extends ClientBundle {
        @Source("hello2.txt")
        TextResource hello();
      }
    }
  }

  @Override
  public String getModuleName() {
    return "org.gwtproject.resources.ResourcesTestsModule";
  }

  public void testUniqueNaming() {
    OuterA.BundleHolder.Resources outerAResources =
        new UniqueNamingTest_OuterA_BundleHolder_ResourcesImpl();
    OuterB.BundleHolder.Resources outerBResources =
        new UniqueNamingTest_OuterB_BundleHolder_ResourcesImpl();
    assertNotSame(outerAResources.hello(), outerBResources.hello());
  }
}
