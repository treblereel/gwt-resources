package org.gwtproject.resources.client;

import static junit.framework.TestCase.*;

/** @author Dmitrii Tikhomirov <chani.liet@gmail.com> Created by treblereel on 10/19/18. */
public class TextResourceTest {
  private static final String HELLO = "Hello World!";
  String result;

  /**
   * Test fix for problem where large text files caused out of memory errors when run in Development
   * Mode.
   */
  public void testBigTextResource() {
    final Resources r = new TextResourceTest_ResourcesImpl();
    String result = r.bigTextResource().getText();
    int length = result.length();
    assertEquals(12737792, length);
  }

  public void testInline() {
    Resources r = new TextResourceTest_ResourcesImpl();
    assertEquals(HELLO, r.helloWorldRelative().getText());
    assertEquals(HELLO, r.helloWorldAbsolute().getText());
  }

  public void testMeta() {
    Resources r = new TextResourceTest_ResourcesImpl();

    // assertEquals(GWT.getModuleBaseForStaticFiles(), DomGlobal.location.getOrigin());

    assertEquals("helloWorldAbsolute", r.helloWorldAbsolute().getName());
    assertEquals("helloWorldRelative", r.helloWorldRelative().getName());
    assertEquals("helloWorldExternal", r.helloWorldExternal().getName());

    ResourcePrototype[] resources = r.getResources();
    assertEquals(6, resources.length);
  }

  public void testAnotatelessExternalText() {
    Resources r = new TextResourceTest_ResourcesImpl();

    try {
      r.helloWorldExternal()
          .getText(
              new ResourceCallback<TextResource>() {
                @Override
                public void onError(ResourceException e) {}

                @Override
                public void onSuccess(TextResource resource) {
                  result = resource.getText();
                  assertEquals("Hello World!", resource.getText());
                }
              });
    } catch (ResourceException e) {
      throw new Error(e);
    }
  }

  public void testAnotatelessText() {
    Resources r = new TextResourceTest_ResourcesImpl();
    assertEquals(HELLO, r.hello().getText());
  }

  public void testOutsideResourceOracle() {
    Resources r = new TextResourceTest_ResourcesImpl();
    assertTrue(r.helloWorldOutsideResourceOracle().getText().startsWith(HELLO));
  }

  @Resource
  interface Resources extends ClientBundleWithLookup {

    @Source("bigtextresource.txt")
    TextResource bigTextResource();

    @Source("org/gwtproject/resources/client/hello.txt")
    TextResource helloWorldAbsolute();

    @Source("hello.txt")
    ExternalTextResource helloWorldExternal();

    @Source("org/gwtproject/resources/server/outside_resource_oracle.txt")
    TextResource helloWorldOutsideResourceOracle();

    @Source("hello.txt")
    TextResource helloWorldRelative();

    TextResource hello();
  }
}
