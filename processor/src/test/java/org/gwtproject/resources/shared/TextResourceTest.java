package org.gwtproject.resources.shared;

import com.google.gwt.junit.client.GWTTestCase;
import org.gwtproject.resources.client.ClientBundle;
import org.gwtproject.resources.client.ClientBundle.Source;
import org.gwtproject.resources.client.ClientBundleWithLookup;
import org.gwtproject.resources.client.ExternalTextResource;
import org.gwtproject.resources.client.ResourcePrototype;
import org.gwtproject.resources.client.TextResource;

/**
 * @author Dmitrii Tikhomirov <chani@me.com>
 * Created by treblereel on 10/19/18.
 */
public class TextResourceTest extends GWTTestCase {
    private static final String HELLO = "Hello World!";

    @ClientBundle
    interface Resources extends ClientBundleWithLookup {

        @Source("bigtextresource.txt")
        TextResource bigTextResource();

        @Source("org/gwtproject/resources/shared/hello.txt")
        TextResource helloWorldAbsolute();

        @Source("hello.txt")
        ExternalTextResource helloWorldExternal();

        @Source("org/gwtproject/resources/server/outside_resource_oracle.txt")
        TextResource helloWorldOutsideResourceOracle();

        @Source("hello.txt")
        TextResource helloWorldRelative();

        TextResource hello();

    }

    @Override
    public String getModuleName() {
        return "org.gwtproject.resources.ResourcesTestsModule";
    }

    /**
     * Test fix for problem where large text files caused out of memory errors
     * when run in Development Mode.
     */
    public void testBigTextResource() {
        final Resources r = new ResourcesImpl();
        String result = r.bigTextResource().getText();
        int length = result.length();
        assertEquals(12737800, length);
    }

    public void testInline() {
        Resources r = new ResourcesImpl();
        assertEquals(HELLO, r.helloWorldRelative().getText());
        assertEquals(HELLO, r.helloWorldAbsolute().getText());
    }

    public void testMeta() {
        Resources r = new ResourcesImpl();
        assertEquals("helloWorldAbsolute", r.helloWorldAbsolute().getName());
        assertEquals("helloWorldRelative", r.helloWorldRelative().getName());
        assertEquals("helloWorldExternal", r.helloWorldExternal().getName());

        ResourcePrototype[] resources = r.getResources();
        assertEquals(6, resources.length);
    }

    public void testAnotatelessText(){
        Resources r = new ResourcesImpl();
        assertEquals(HELLO, r.hello().getText());
    }

    public void testOutsideResourceOracle() {
        Resources r = new ResourcesImpl();
        assertTrue(r.helloWorldOutsideResourceOracle().getText().startsWith(HELLO));
    }

}
