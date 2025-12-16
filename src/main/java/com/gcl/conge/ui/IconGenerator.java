package com.gcl.conge.ui;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class IconGenerator {
    private static BufferedImage loadLogo() {
        try {
            URL u = IconGenerator.class.getResource("/com/gcl/conge/ui/logo.jpg");
            if (u != null) {
                return ImageIO.read(u);
            }
        } catch (Exception ignored) {}
        try {
            URL u = IconGenerator.class.getResource("/com/gcl/conge/ui/logo.png");
            if (u != null) {
                return ImageIO.read(u);
            }
        } catch (Exception ignored) {}
        try {
            File f = new File("src/main/resources/com/gcl/conge/ui/logo.jpg");
            if (f.exists()) return ImageIO.read(f);
        } catch (Exception ignored) {}
        try {
            File f = new File("src/main/resources/com/gcl/conge/ui/logo.png");
            if (f.exists()) return ImageIO.read(f);
        } catch (Exception ignored) {}
        try {
            File f = new File("logo.jpg");
            if (f.exists()) return ImageIO.read(f);
        } catch (Exception ignored) {}
        try {
            File f = new File("logo.png");
            if (f.exists()) return ImageIO.read(f);
        } catch (Exception ignored) {}
        return null;
    }

    private static BufferedImage scaleToSquare(BufferedImage src, int size) {
        BufferedImage dst = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.drawImage(src, 0, 0, size, size, null);
        g.dispose();
        return dst;
    }

    private static byte[] toPngBytes(BufferedImage img) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", baos);
        return baos.toByteArray();
    }

    private static void writeIcoWithPng(byte[] png, int width, int height, File out) throws Exception {
        ByteBuffer header = ByteBuffer.allocate(6 + 16);
        header.order(ByteOrder.LITTLE_ENDIAN);
        header.putShort((short)0);
        header.putShort((short)1);
        header.putShort((short)1);
        header.put((byte)(width >= 256 ? 0 : width));
        header.put((byte)(height >= 256 ? 0 : height));
        header.put((byte)0);
        header.put((byte)0);
        header.putShort((short)1);
        header.putShort((short)32);
        header.putInt(png.length);
        header.putInt(6 + 16);
        try (FileOutputStream fos = new FileOutputStream(out)) {
            fos.write(header.array());
            fos.write(png);
        }
    }

    public static void main(String[] args) {
        try {
            BufferedImage src = loadLogo();
            if (src == null) return;
            int size = Math.min(256, Math.max(src.getWidth(), src.getHeight()));
            BufferedImage scaled = scaleToSquare(src, size);
            byte[] png = toPngBytes(scaled);
            File ico = new File("logo.ico");
            writeIcoWithPng(png, scaled.getWidth(), scaled.getHeight(), ico);
        } catch (Exception ignored) {}
    }
}

