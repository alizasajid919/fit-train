package com.fitness.app;

import org.junit.Test;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class IconGeneratorTest {

    @Test
    public void generateIcons() throws Exception {
        String srcPath = "C:\\Users\\My Computer\\.gemini\\antigravity\\brain\\96b2e27e-98a9-4c6e-8f28-f07489ba104b\\fittrain_launcher_icon_mockup_1783228125041.png";
        String resBase = "C:\\Users\\My Computer\\.gemini\antigravity\\scratch\\FitnessApp\\app\\src\\main\\res";
        
        File srcFile = new File(srcPath);
        if (!srcFile.exists()) {
            System.err.println("Source image not found: " + srcPath);
            throw new RuntimeException("Source image not found");
        }

        BufferedImage img = ImageIO.read(srcFile);
        int w = img.getWidth();
        int h = img.getHeight();
        System.out.println("Loaded mockup image of size " + w + "x" + h);

        // Average corners to detect background color
        int[] corners = {
            img.getRGB(0, 0),
            img.getRGB(w - 1, 0),
            img.getRGB(0, h - 1),
            img.getRGB(w - 1, h - 1)
        };
        int bgR = 0, bgG = 0, bgB = 0;
        for (int c : corners) {
            bgR += (c >> 16) & 0xFF;
            bgG += (c >> 8) & 0xFF;
            bgB += c & 0xFF;
        }
        bgR /= 4;
        bgG /= 4;
        bgB /= 4;
        System.out.println("Detected background color: RGB(" + bgR + ", " + bgG + ", " + bgB + ")");

        // Key out background to get foreground
        BufferedImage fgImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        int minX = w, maxX = 0, minY = h, maxY = 0;
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                double dist = Math.sqrt((r - bgR)*(r - bgR) + (g - bgG)*(g - bgG) + (b - bgB)*(b - bgB));
                double lowThresh = 35.0;
                double highThresh = 80.0;

                int alpha;
                if (dist < lowThresh) {
                    alpha = 0;
                } else if (dist > highThresh) {
                    alpha = 255;
                } else {
                    alpha = (int) (255 * (dist - lowThresh) / (highThresh - lowThresh));
                }

                if (alpha > 0) {
                    if (x < minX) minX = x;
                    if (x > maxX) maxX = x;
                    if (y < minY) minY = y;
                    if (y > maxY) maxY = y;
                }

                int newRgb = (alpha << 24) | (r << 16) | (g << 8) | b;
                fgImg.setRGB(x, y, newRgb);
            }
        }

        // Bounding box validation
        if (minX >= maxX || minY >= maxY) {
            minX = 0;
            minY = 0;
            maxX = w - 1;
            maxY = h - 1;
        }
        BufferedImage croppedLogo = fgImg.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
        System.out.println("Cropped logo dimensions: " + croppedLogo.getWidth() + "x" + croppedLogo.getHeight());

        // Density buckets
        String[] folders = {
            "mipmap-mdpi",
            "mipmap-hdpi",
            "mipmap-xhdpi",
            "mipmap-xxhdpi",
            "mipmap-xxxhdpi"
        };
        int[] legacySizes = {48, 72, 96, 144, 192};
        int[] adaptiveSizes = {108, 162, 216, 324, 432};

        for (int i = 0; i < folders.length; i++) {
            String folder = folders[i];
            int legacySize = legacySizes[i];
            int adaptiveSize = adaptiveSizes[i];

            File folderDir = new File(resBase, folder);
            if (!folderDir.exists()) {
                folderDir.mkdirs();
            }

            // 1. Legacy square launcher
            BufferedImage legacySquare = resize(img, legacySize, legacySize);
            ImageIO.write(legacySquare, "png", new File(folderDir, "ic_launcher.png"));

            // 2. Legacy round launcher
            BufferedImage legacyRound = makeCircular(legacySquare);
            ImageIO.write(legacyRound, "png", new File(folderDir, "ic_launcher_round.png"));

            // 3. Adaptive Foreground
            int safeSize = (int) (adaptiveSize * 0.65);
            int lw = croppedLogo.getWidth();
            int lh = croppedLogo.getHeight();
            double aspect = (double) lw / lh;
            int newW, newH;
            if (lw > lh) {
                newW = safeSize;
                newH = (int) (safeSize / aspect);
            } else {
                newH = safeSize;
                newW = (int) (safeSize * aspect);
            }
            BufferedImage scaledLogo = resize(croppedLogo, newW, newH);

            BufferedImage adaptiveFg = new BufferedImage(adaptiveSize, adaptiveSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = adaptiveFg.createGraphics();
            g2.drawImage(scaledLogo, (adaptiveSize - newW) / 2, (adaptiveSize - newH) / 2, null);
            g2.dispose();

            ImageIO.write(adaptiveFg, "png", new File(folderDir, "ic_launcher_foreground.png"));
            System.out.println("Exported assets for " + folder);
        }

        System.out.println("Icon asset generation completed successfully!");
    }

    private BufferedImage resize(BufferedImage src, int width, int height) {
        BufferedImage dest = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = dest.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(src, 0, 0, width, height, null);
        g2.dispose();
        return dest;
    }

    private BufferedImage makeCircular(BufferedImage src) {
        int size = src.getWidth();
        BufferedImage dest = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = dest.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setClip(new Ellipse2D.Float(0, 0, size, size));
        g2.drawImage(src, 0, 0, null);
        g2.dispose();
        return dest;
    }
}
