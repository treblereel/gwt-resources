package org.gwtproject.resources.client;

import static elemental2.dom.DomGlobal.document;
import static org.gwtproject.resources.client.ImageResource.ImageOptions;
import static org.gwtproject.resources.client.ImageResource.RepeatStyle;

import elemental2.dom.Image;
import org.gwtproject.resources.client.impl.ImageResourcePrototype;

import static junit.framework.TestCase.*;

/** @author Dmitrii Tikhomirov <chani.liet@gmail.com> Created by treblereel on 10/22/18. */
public class ImageResourceTest {

  /**
   * The default timeout of asynchronous tests. This should be larger than LOAD_EVENT_TIMEOUT and
   * SYNTHETIC_LOAD_EVENT_TIMEOUT.
   */
  private static final int DEFAULT_TEST_TIMEOUT = 10000;

  /** The amount of time to wait for a load event to fire in milliseconds. */
  private static final int LOAD_EVENT_TIMEOUT = 7000;

  /**
   * The amount of time to wait for a clipped image to fire a synthetic load event in milliseconds.
   */
  private static final int SYNTHETIC_LOAD_EVENT_TIMEOUT = 1000;

  public void testAltText() {
    final String altText = "this is an image";
    final Image image = (Image) document.createElement("img");

    assertEquals("", image.alt);
    image.alt = altText;
    assertEquals(altText, image.alt);
    image.alt = "";
    assertEquals("", image.alt);
  }

  /** Test that attaching and immediately detaching an element does not cause an error.
  public void testAttachDetach() {

    final Image image = (Image) document.createElement("img");
    document.body.appendChild(image);
    document.body.removeChild(image);
    // Wait for the synthetic event to attempt to fire.
    delayTestFinish(DEFAULT_TEST_TIMEOUT);
    new Timer() {
      @Override
      public void run() {
        // The synthetic event did not cause an error.
        finishTest();
      }
    }.schedule(SYNTHETIC_LOAD_EVENT_TIMEOUT);
  }*/

  public void testAnimated() {
    ImageResources r = new ImageResourceTest_ImageResourcesImpl();

    ImageResource a = r.animated();

    assertTrue(a.isAnimated());
    assertFalse(((ImageResourcePrototype) a).isLossy());
    assertEquals(16, a.getWidth());
    assertEquals(16, a.getHeight());

    // Make sure the animated image is encoded separately
    assertFalse(a.getSafeUri().equals(r.i16x16().getSafeUri()));
  }

  public void testDedup() {
    ImageResources r = new ImageResourceTest_ImageResourcesImpl();

    ImageResource a = r.i64x64();
    ImageResource b = r.i64x64Dup();
    ImageResource c = r.i64x64Dup2();
    assertEquals(64, a.getHeight());
    assertEquals(64, a.getWidth());
  }

  public void testPacking() {
    ImageResources r = new ImageResourceTest_ImageResourcesImpl();

    ImageResource i64 = r.i64x64();
    ImageResource lossy = r.largeLossy();
    ImageResource lossless = r.largeLossless();

    assertFalse(((ImageResourcePrototype) lossless).isLossy());

    // Make sure that the large, lossy image isn't bundled with the rest
    assertTrue(((ImageResourcePrototype) lossy).isLossy());
    assertTrue(!i64.getSafeUri().equals(lossy.getSafeUri()));
    assertFalse(lossy instanceof TextResource);

    assertEquals(16, r.i16x16Vertical().getWidth());
    assertEquals(16, r.i16x16Vertical().getHeight());

    assertEquals(16, r.i16x16Horizontal().getWidth());
    assertEquals(16, r.i16x16Horizontal().getHeight());

    // Check scaling and aspect ratio
    assertEquals(32, r.scaledDown().getWidth());
    assertEquals(32, r.scaledDown().getHeight());
    assertEquals(128, r.scaledUp().getWidth());
    assertEquals(128, r.scaledUp().getHeight());
  }

  public void testPreventInlining() {
    ImageResources r = new ImageResourceTest_ImageResourcesImpl();
    ImageResource a = r.i16x16();
    ImageResource b = r.i32x32();
    ImageResource c = r.linuxLogo();

    // Should never be a data URL
    assertTrue(a.getSafeUri().asString().startsWith("data:"));
    assertTrue(b.getSafeUri().asString().startsWith("data:"));
    assertFalse(c.getSafeUri().asString().startsWith("data:"));
    // Should be fetched from different URLs
    assertFalse(a.getSafeUri().asString().equals(b.getSafeUri().asString()));
  }

  @Resource
  interface ExternalResources extends ClientBundle {

    @ImageOptions(preventInlining = true)
    @Source("16x16.png")
    ImageResource i16x16();

    @ImageOptions(preventInlining = true)
    @Source("32x32.png")
    ImageResource i32x32();

    @Source("16x16.png")
    ImageResource i16x16RepeatBoth();

    @Source("32x32.png")
    ImageResource i32x32RepeatBoth();
  }

  @Resource
  interface ImageResources extends ClientBundle {
    @Source("animated.gif")
    ImageResource animated();

    /**
     * This image shouldn't be re-encoded as a PNG or it will dramatically increase in size,
     * although it's still small enough to be encoded as a data URL as-is.
     */
    ImageResource complexLossy();

    @Source("16x16.png")
    ImageResource i16x16();

    @Source("16x16.png")
    @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
    ImageResource i16x16Horizontal();

    @Source("16x16.png")
    @ImageOptions(repeatStyle = RepeatStyle.Vertical)
    ImageResource i16x16Vertical();

    @Source("32x32.png")
    ImageResource i32x32();

    @Source("32x32.png")
    @ImageOptions(repeatStyle = RepeatStyle.Horizontal)
    ImageResource i32x32Horizontal();

    @Source("32x32.png")
    @ImageOptions(repeatStyle = RepeatStyle.Vertical)
    ImageResource i32x32Vertical();

    @Source("64x64.png")
    ImageResource i64x64();

    @Source("64x64.png")
    ImageResource i64x64Dup();

    @Source("64x64-dup.png")
    ImageResource i64x64Dup2();

    // Test default filename lookup while we're at it
    ImageResource largeLossless();

    // Test default filename lookup while we're at it
    ImageResource largeLossy();

    @Source("64x64.png")
    @ImageOptions(width = 32)
    ImageResource scaledDown();

    @Source("64x64.png")
    @ImageOptions(width = 128)
    ImageResource scaledUp();

    @Source("logo.png")
    ImageResource linuxLogo();
  }
}
