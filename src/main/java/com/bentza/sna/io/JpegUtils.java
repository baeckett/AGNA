package com.bentza.sna.io;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

/**
 * JPEG encoding/decoding helpers.
 * 
 * <p>
 * Replaces the internal {@code com.sun.image.codec.jpeg} API (removed from
 * modern JDKs) with the standard {@link ImageIO} while preserving control over
 * the compression quality that the old code exposed.
 */
public final class JpegUtils
    {
    private JpegUtils()
        {
        }

    /**
     * Encodes an image as JPEG to the given stream.
     * 
     * @param image
     *            image to encode
     * @param out
     *            destination stream (not closed here)
     * @param quality
     *            0.0 .. 1.0, 1.0 being the least compression
     * @throws IOException
     *             if no JPEG writer is available or the write fails
     */
    public static void writeJpeg(BufferedImage image, OutputStream out,
            float quality) throws IOException
        {
        if (image == null)
            throw new IOException("Cannot encode a null image.");
        if (quality < 0f || quality > 1f)
            quality = 0.9f;

        Iterator<ImageWriter> writers = ImageIO
                .getImageWritersByFormatName("jpeg");
        if (!writers.hasNext())
            throw new IOException("No JPEG image writer available.");
        ImageWriter writer = writers.next();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(out))
            {
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            param.setCompressionQuality(quality);
            writer.write(null, new IIOImage(image, null, null), param);
            }
        finally
            {
            writer.dispose();
            }
        }
    }