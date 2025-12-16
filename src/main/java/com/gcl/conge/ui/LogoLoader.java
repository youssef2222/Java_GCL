package com.gcl.conge.ui;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class LogoLoader {
    public static ImageIcon loadLogoIcon() {
        String[] classpathCandidates = new String[] {
                "/com/gcl/conge/ui/logo.png",
                "/com/gcl/conge/ui/logo.jpg",
                "/com/gcl/conge/ui/logo.jpeg"
        };
        for (String cp : classpathCandidates) {
            URL res = LogoLoader.class.getResource(cp);
            if (res != null) {
                try {
                    return new ImageIcon(ImageIO.read(res));
                } catch (IOException ignored) {
                }
            }
        }
        String[] fileCandidates = new String[] {
                "src/main/resources/com/gcl/conge/ui/logo.png",
                "src/main/resources/com/gcl/conge/ui/logo.jpg",
                "src/main/resources/com/gcl/conge/ui/logo.jpeg",
                "logo.png",
                "logo.jpg",
                "logo.jpeg"
        };
        for (String path : fileCandidates) {
            File f = new File(path);
            if (f.exists()) {
                try {
                    return new ImageIcon(ImageIO.read(f));
                } catch (IOException ignored) {
                }
            }
        }
        return null;
    }

    public static Image scaleForHeader(Image img, int maxWidth, int maxHeight) {
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        if (w <= 0 || h <= 0)
            return img;
        double rw = (double) maxWidth / w;
        double rh = (double) maxHeight / h;
        double r = Math.min(rw, rh);
        int nw = (int) Math.max(1, Math.round(w * r));
        int nh = (int) Math.max(1, Math.round(h * r));
        return img.getScaledInstance(nw, nh, Image.SCALE_SMOOTH);
    }
}
