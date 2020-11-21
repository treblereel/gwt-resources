package org.gwtproject.resources.client;

import static junit.framework.TestCase.*;

public class UniqueNamingTest {

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

  public void testUniqueNaming() {
    OuterA.BundleHolder.Resources outerAResources =
        new UniqueNamingTest_OuterA_BundleHolder_ResourcesImpl();
    OuterB.BundleHolder.Resources outerBResources =
        new UniqueNamingTest_OuterB_BundleHolder_ResourcesImpl();
    assertNotSame(outerAResources.hello(), outerBResources.hello());
  }
}
