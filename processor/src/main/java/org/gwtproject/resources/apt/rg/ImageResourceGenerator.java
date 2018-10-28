package org.gwtproject.resources.apt.rg;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.apache.commons.io.FilenameUtils;
import org.gwtproject.resources.apt.ClientBundleGeneratorContext;
import org.gwtproject.resources.apt.exceptions.UnableToCompleteException;
import org.gwtproject.resources.apt.resource.Resource;
import org.gwtproject.resources.apt.resource.impl.FakeResource;
import org.gwtproject.resources.client.ImageResource;
import org.w3c.dom.Node;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.tools.Diagnostic;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @author Dmitrii Tikhomirov <chani@me.com>
 * Created by treblereel on 10/22/18.
 */
public final class ImageResourceGenerator extends InlineResourceGenerator {
    private final ClassName imageResourceClassName = ClassName.get("org.gwtproject.resources.client", "ImageResource");
    private final AtomicInteger externalImageCounter = new AtomicInteger(0);

    private static final String BUNDLE_FILE_TYPE = "png";
    private static final String BUNDLE_MIME_TYPE = "image/png";

    public ImageResourceGenerator(ClientBundleGeneratorContext context, Element clazz, TypeSpec.Builder builder) {
        super(context, clazz, builder);
    }

    @Override
    void process() throws UnableToCompleteException {
        List<ImageResourceDeclaration> resources = scan(ImageResource.class).stream().map(ImageResourceDeclaration::new).collect(Collectors.toList());
        resources.forEach(image -> {
            try {
                addMethodBody(image.method, imageResourceClassName, generateResourceMethodImpl(image));
                addMethodInitializer(image.method);
                addGetResourceMethod(image.method);
            } catch (UnableToCompleteException e) {
                context.messager.printMessage(Diagnostic.Kind.ERROR, "Unable to process ImageResource " + image.method + " in " + clazz);
                throw new Error(e);
            }
        });
    }


    private MethodSpec.Builder generateResourceMethodImpl(ImageResourceDeclaration imageOptions) throws UnableToCompleteException {
        String externalImage = "externalImage" + externalImageCounter.getAndIncrement();
        MethodSpec.Builder initializer = MethodSpec.methodBuilder(imageOptions.method.getSimpleName().toString() + "Initializer")
                .addModifiers(Modifier.PRIVATE)
                .returns(void.class)
                .addStatement(" $L = new org.gwtproject.resources.client.impl.ImageResourcePrototype($S,$L,$L,$L,$L,$L)",
                        imageOptions.method.getSimpleName().toString(),
                        imageOptions.method.getSimpleName().toString(),
                        "org.gwtproject.safehtml.shared.UriUtils.fromTrustedString(" + externalImage + ")",
                        imageOptions.width, imageOptions.height, imageOptions.animated, imageOptions.lossy);
        generateContent(externalImage, imageOptions);
        return initializer;
    }

    private void generateContent(String fieldName, ImageResourceDeclaration ird) throws UnableToCompleteException {
        byte[] data = readByteArrayFromResource(ird.resource);
        if (!ird.animated && !ird.lossy) {
            byte[] newPng = toPng(ird);
            if (data.length > newPng.length) {
                context.messager.printMessage(Diagnostic.Kind.NOTE, "compress " + ird.resource.getUrl().getFile() + " from " + data.length + " byte to " + newPng.length + " bytes");
                data = newPng;
                String fileName = FilenameUtils.getBaseName(ird.resource.getUrl().getFile()) + "." + BUNDLE_FILE_TYPE;
                ird.mimeType = BUNDLE_MIME_TYPE;
                ird.resource = new FakeResource(fileName, data);
            }
        }
        String outputUrlExpression = deploy(ird.resource, ird.mimeType, ird.preventInlining);
        addResourceField(fieldName, outputUrlExpression);
    }

    //TODO
    private void addResourceField(String fieldName, String encoded) {
        clazzBuilder.addField(FieldSpec.builder(ClassName.get(String.class), fieldName, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                .initializer("$L", encoded)
                .build());
    }

    /**
     * TODO this class must be redone
     */
    public class ImageResourceDeclaration {
        final Element method;
        Resource resource;
        ImageResource.RepeatStyle repeatStyle = ImageResource.RepeatStyle.None;
        boolean animated = false;
        boolean lossy = false;
        BufferedImage bimg;
        boolean preventInlining = false;
        int height = -1, originalHeight;
        int width = -1, originalWidth;
        String mimeType;
        ImageResource.ImageOptions options;

        ImageResourceDeclaration(Element method) {
            this.method = method;
            setOptions(method.getAnnotation(ImageResource.ImageOptions.class));
            setSize();
            mimeType = getContentType();
            animated = isAnimated(this);
            lossy = isLossy(this);
        }

        private void setSize() {
            try {
                this.resource = getResource(method);
                bimg = ImageIO.read(this.resource.openContents());
                this.originalHeight = bimg.getHeight();
                this.originalWidth = bimg.getWidth();
            } catch (UnableToCompleteException | IOException e) {
                e.printStackTrace();
                throw new Error(e);
            }

            if (this.width != -1 && this.height == -1) {
                this.height = this.width;
            } else if (this.height != -1 && this.width == -1) {
                this.width = this.height;
            } else {
                this.width = this.originalWidth;
                this.height = this.originalHeight;
            }

        }

        public String getContent() {
            return readBase64InputStream(resource);
        }

        private String getContentType() {
            return guessContentTypeFromResource(resource);
        }

        private void setOptions(ImageResource.ImageOptions options) {
            if (options != null) {
                this.preventInlining = options.preventInlining();
                this.repeatStyle = options.repeatStyle();

                if (options.height() != -1) {
                    this.height = options.height();
                }

                if (options.width() != -1) {
                    this.width = options.width();
                }
            }
        }

    }

    public boolean isAnimated(ImageResourceDeclaration ird) {
        if (!ird.mimeType.equals("image/gif")) {
            return false;
        }
        ImageReader is = ImageIO.getImageReadersBySuffix("GIF").next();
        try (ImageInputStream iis = ImageIO.createImageInputStream(ird.resource.openContents())) {
            is.setInput(iis);
            return is.getNumImages(true) > 1 ? true : false;
        } catch (IOException | UnableToCompleteException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isLossy(ImageResourceDeclaration ird) {
        boolean result = false;
        if (ird.mimeType.equals("image/png")) { //always lossless
            return false;
        }

        try {
            ImageInputStream in = new MemoryCacheImageInputStream(
                    new ByteArrayInputStream(readByteArrayFromResource(ird.resource)));
            ImageReader reader = ImageIO.getImageReaders(in).next();
            reader.setInput(in);
            // Retrieve standard image metadata tree
            IIOMetadata meta = reader.getImageMetadata(0);
            if (meta == null || !meta.isStandardMetadataFormatSupported()) {
                throw new Error("Unable to process the image");
            }
            Node data = meta.getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName);
            for (int i = 0, j = data.getChildNodes().getLength(); i < j; i++) {
                Node child = data.getChildNodes().item(i);
                if (child.getLocalName().equalsIgnoreCase("compression")) {
                    for (int k = 0, l = child.getChildNodes().getLength(); k < l; k++) {
                        Node child2 = child.getChildNodes().item(k);
                        if (child2.getLocalName().equalsIgnoreCase("lossless")) {
                            Node value = child2.getAttributes().getNamedItem("value");
                            if (value == null) {
                                // The default is true, according to the DTD
                                result = false;
                            } else {
                                result = !Boolean.parseBoolean(value.getNodeValue());
                            }
                        }
                    }
                }
            }
        } catch (UnableToCompleteException | IOException ioe) {
            ioe.printStackTrace();
            throw new Error("Unable to process the image " + ird.method + " " + ioe.getMessage());
        }
        return result;
    }

    private byte[] toPng(ImageResourceDeclaration rect)
            throws UnableToCompleteException {

        BufferedImage bundledImage = new BufferedImage(rect.width, rect.height, BufferedImage.TYPE_INT_ARGB_PRE);

        Graphics2D g2d = bundledImage.createGraphics();
        setBetterRenderingQuality(g2d);
        g2d.drawImage(rect.bimg, new AffineTransform(), null);
        g2d.dispose();

        byte[] imageBytes = createImageBytes(bundledImage);
        return imageBytes;
    }

    private byte[] createImageBytes(BufferedImage bundledImage) throws UnableToCompleteException {
        byte[] imageBytes;

        try {
            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
            boolean writerAvailable = ImageIO.write(bundledImage, BUNDLE_FILE_TYPE,
                    byteOutputStream);
            if (!writerAvailable) {
                context.messager.printMessage(Diagnostic.Kind.ERROR, "No " + BUNDLE_FILE_TYPE
                        + " writer available");
                throw new UnableToCompleteException("No " + BUNDLE_FILE_TYPE
                        + " writer available");
            }
            imageBytes = byteOutputStream.toByteArray();
        } catch (IOException e) {
            context.messager.printMessage(Diagnostic.Kind.ERROR,
                    "An error occurred while trying to write the image bundle.");
            throw new UnableToCompleteException("An error occurred while trying to write the image bundle.");
        }
        return imageBytes;
    }

    private static void setBetterRenderingQuality(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION,
                RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
    }


}