/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glcd.UI;

import glcd.GLCDImageLoader.GLCDImageLoaderException;
import glcd.GLCDImageLoader.RawImage;
import glcd.Image.ColorReducer;
import glcd.Util;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

/**
 *
 * @author ideras
 */
public class ImageItem {

    private String imageName;
    private String imageFilePath;
    private String imageOutputFilePath;
    private int outputFormat;
    private boolean invertedPixels;
    private BufferedImage image;
    private int width, height;
    private int threshold;
    private float scaleX, scaleY;

    public ImageItem() {
        image = null;
        width = 0;
        height = 0;
        scaleX = 1;
        scaleY = 1;
        threshold = 50;
        imageName = "";
        imageFilePath = "";
        imageOutputFilePath = "";
        outputFormat = RawImage.GENERIC_1BPP_PAGED;
        invertedPixels = false;
    }

    public void loadImageFromFile(File imageFile) throws IOException, GLCDImageLoaderException {
        imageName = imageFile.getName();
        imageFilePath = imageFile.getAbsolutePath();

        setImage(ImageIO.read(imageFile));
    }

    public BufferedImage getImage() {
        return image;
    }

    public void setImage(BufferedImage image) {
        this.image = image;

        width = image.getWidth(null);
        height = image.getHeight(null);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public float getScaleX() {
        return scaleX;
    }

    public void setScaleX(float scaleX) {
        this.scaleX = scaleX;
    }

    public float getScaleY() {
        return scaleY;
    }

    public void setScaleY(float scaleY) {
        this.scaleY = scaleY;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public boolean isInvertedPixels() {
        return invertedPixels;
    }

    public void setInvertedPixels(boolean invertedPixels) {
        this.invertedPixels = invertedPixels;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageFilePath() {
        return imageFilePath;
    }

    public void setImageFilePath(String imageFilePath) {
        this.imageFilePath = imageFilePath;
    }

    public String getImageOutputFilePath() {
        return imageOutputFilePath;
    }

    public void setImageOutputFilePath(String imageOutputFilePath) {
        this.imageOutputFilePath = imageOutputFilePath;
    }

    public int getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(int outputFormat) {
        this.outputFormat = outputFormat;
    }

    public byte[] getData1BPP() {
        int size = (width * height + 7) / 8;
        byte data[] = new byte[size];
        byte value = 0;
        int index = 0;
        byte bitMask = 0x01;

        int thresholdIntensity = threshold * 255 / 100;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                int c = image.getRGB(x, y);

                int r = (c >> 16) & 0xFF;      // get the rgb values 
                int g = (c >> 8) & 0xFF;
                int b = c & 0xFF;

                int grayIntensity = (21 * r + 72 * g + 7 * b) / 100;

                boolean setPixel;
                if (!invertedPixels) {
                    setPixel = grayIntensity > thresholdIntensity;
                } else {
                    setPixel = grayIntensity < thresholdIntensity;
                }

                if (setPixel) {
                    value |= bitMask;   // set the bit if this pixel is more dark than light
                }

                bitMask <<= 1;
                if (bitMask == 0) {
                    data[index++] = value;
                    bitMask = 0x01;
                    value = 0;

                }
            }
        }

        if (index <= data.length - 1) {
            data[index] = value;
        }

        return data;
    }

    // Return the byte representing data a the given page and x offset
    private int buildPageValue(int x, int page, byte[] imageData) {
        int val = 0;

        for (byte bit = 0; bit < 8; bit++) {
            int y = page * 8 + bit;
            int pos = y * width + x;
            int index = pos / 8;
            byte mask = (byte) (1 << (pos % 8));

            if (pos < width * height) // skip padding if at the end of real data
            {
                byte b = imageData[index];

                if ((b & mask) != 0) {
                    val |= (1 << bit);
                }
            }
        }
        return val;
    }

    private String byteToHex(byte b) {
        final char hexDigit[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        byte digit1 = (byte) ((b >> 4) & 0x0F);
        byte digit2 = (byte) (b & 0x0F);

        String str = hexDigit[digit1] + "" + hexDigit[digit2];

        return str;
    }

    private String wordToHex(short w) {
        return "0x" + byteToHex((byte) (w & 0x00FF)) + ", 0x" + byteToHex((byte) ((w >> 8) & 0x00FF));
    }

    private String generateCCodeForGeneric1BPPPaged() {
        StringBuilder sb = new StringBuilder();
        int pageCount;

        pageCount = (height + 7) / 8; // round up so each page contains 8 pixels    

        sb.append("/* ").append(imageName).append(": */\n");
        sb.append("/* Width:  ").append(width).append(" */\n");
        sb.append("/* Height: ").append(height).append(" */\n");
        sb.append("/* Format: Generic Bitmap 1BPP Paged */\n");
        sb.append("const unsigned char ");
        sb.append(glcd.Util.getBaseName(imageName));
        sb.append("[] = {\n");

        sb.append("  ").append(width).append(", //Width\n");
        sb.append("  ").append(height).append(", //Height");

        byte[] imageData = getData1BPP();

        for (int page = 0; page < pageCount; page++) {
            sb.append("\n  /* page ").append(page).append(" (lines ").append(page * 8).append("-").append(page * 8 + 7).append(") */\n");
            sb.append("  ");
            for (int x = 0; x < width; x++) {
                int pixelsPerPage = buildPageValue(x, page, imageData);
                sb.append("0x").append(byteToHex((byte) pixelsPerPage));
                if ((x == (width - 1)) && (page == pageCount - 1)) {
                    sb.append("\n"); // this is the last element so new line instead of comma
                } else {
                    sb.append(",");   // comma on all but last entry
                }
                if (((x + 1) % 16 == 0) && (x < width - 1)) {
                    sb.append("\n  ");
                }
            }
        }
        sb.append("};\n");

        return sb.toString();
    }

    private int getColorIndex(byte[] r, byte[] g, byte[] b, int color) {

        byte cr = (byte) ((color & 0x00FF0000) >> 16);
        byte cg = (byte) ((color & 0x0000FF00) >> 8);
        byte cb = (byte) ((color & 0x000000FF));

        for (int i = 0; i < b.length; i++) {
            byte red = r[i];
            byte green = g[i];
            byte blue = b[i];

            if ((cr == red) && (cg == green) && (cb == blue)) {
                return i;
            }
        }

        return 0;
    }

    private String generateCCodeForWindowsBitmap4BPP() {

        BufferedImage outImage;

        if (image.getType() != BufferedImage.TYPE_BYTE_INDEXED) {
            outImage = ColorReducer.reduce24(image, 16);
        } else {
            outImage = image;
        }

        int bytesPerLine = (width + 1) / 2;
        int extraBytes = ((bytesPerLine + 3) / 4) * 4 - bytesPerLine;

        IndexColorModel cm = (IndexColorModel) outImage.getColorModel();
        StringBuilder sb = new StringBuilder();

        sb.append("/* ").append(imageName).append(": */\n");
        sb.append("/* Width:  ").append(width).append(" */\n");
        sb.append("/* Height: ").append(height).append(" */\n");
        sb.append("/* Format: Windows Bitmap 4BPP */\n");
        sb.append("const unsigned char ");
        sb.append(Util.replaceInvalidCharacters(Util.getBaseName(imageName)));
        sb.append("[] = {\n");

        /*
         * Bitmap file header
         * =================
         * struct BITMAPFILEHEADER {
         WORD  bfType;
         DWORD bfSize;
         WORD  bfReserved1;
         WORD  bfReserved2;
         DWORD bfOffBits;
         }
         */
        sb.append("0x42, 0x4D, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x76, 0x00, 0x00, 0x00,\n");

        /*
         * Bitmap information header
         * =========================
         struct tagBITMAPINFOHEADER {
         DWORD biSize;
         LONG  biWidth;
         LONG  biHeight;
         WORD  biPlanes;
         WORD  biBitCount;
         DWORD biCompression;
         DWORD biSizeImage;
         LONG  biXPelsPerMeter;
         LONG  biYPelsPerMeter;
         DWORD biClrUsed;
         DWORD biClrImportant;
         }
         */
        sb.append("0x28, 0x00, 0x00, 0x00, 0x").append(byteToHex((byte) width)).append(", 0x00, 0x00, 0x00, 0x").append(byteToHex((byte) height)).append(", 0x00, 0x00, 0x00, ");
        //biPlanes, biBitCount, biCompression
        sb.append("0x01, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00,\n");
        //biSizeImage, biXPelsPerMeter, biYPelsPerMeter, biClrUsed
        sb.append("0x00, 0x00, 0x00, 0x00, 0xC4, 0x0E, 0x00, 0x00, 0xC4, 0x0E, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,\n");
        //biClrImportant
        sb.append("0x00, 0x00, 0x00, 0x00,\n");

        //Generating palette
        byte r[] = new byte[16];
        byte g[] = new byte[16];
        byte b[] = new byte[16];

        cm.getReds(r);
        cm.getGreens(g);
        cm.getBlues(b);

        for (int i = 0; i < 16; i++) {
            sb.append("0x").append(byteToHex(b[i])).append(", ");
            sb.append("0x").append(byteToHex(g[i])).append(", ");
            sb.append("0x").append(byteToHex(r[i])).append(", ");
            sb.append("0x00,\n");
        }

        //Generating pixels
        int count = 0;
        for (int y = height - 1; y >= 0; y--) {
            int state = 0x02;
            int color = 0;
            for (int x = 0; x < width; x++) {
                int pixelColor = outImage.getRGB(x, y) & 0x00FFFFFF;
                int pixelIndex = getColorIndex(r, g, b, pixelColor);

                switch (state) {
                    case 0x02:
                        color = (pixelIndex & 0x0F) << 4;
                        state = 1;
                        break;
                    case 0x01:
                        color |= pixelIndex & 0x0F;
                        state = 2;
                        sb.append("0x").append(byteToHex((byte) color)).append(", ");
                        count++;
                        color = 0;

                        if (count == 16) {
                            sb.append("\n");
                            count = 0;
                        }
                        break;
                }
            }

            //Output an extra byte
            if (state == 0x01) {
                sb.append("0x").append(byteToHex((byte) color)).append(", ");
                count++;

                if (count == 16) {
                    sb.append("\n");
                    count = 0;
                }
            }

            for (int i = 0; i < extraBytes; i++) {
                sb.append("0x00, ");
                count++;

                if (count == 16) {
                    sb.append("\n");
                    count = 0;
                }
            }
        }

        sb.append("\n};\n");

        return sb.toString();
    }

    private String generateCCodeForGeneric1BPPLinear() {
        StringBuilder sb = new StringBuilder();

        sb.append("/* ").append(imageName).append(": */\n");
        sb.append("/* Width:  ").append(width).append(" */\n");
        sb.append("/* Height: ").append(height).append(" */\n");
        sb.append("/* Format: Generic Bitmap 1BPP Linear */\n");
        sb.append("const unsigned char ");
        sb.append(Util.replaceInvalidCharacters(Util.getBaseName(imageName)));
        sb.append("[] = {\n");

        //Width and Height are bytes
        sb.append("0x").append(byteToHex((byte) width)).append(", //Width\n");
        sb.append("0x").append(byteToHex((byte) height)).append(", //Height\n");

        byte[] imageData = getData1BPP();
        int count = 0;

        for (int i = 0; i < imageData.length; i++) {
            byte b = imageData[i];
            sb.append("0x").append(byteToHex(b)).append(", ");

            count++;
            if (count == 16) {
                sb.append("\n");
                count = 0;
            }
        }
        sb.append("\n};\n");

        return sb.toString();
    }

    private String generateCCodeForGenericBitmap4BPP() {

        BufferedImage outImage;

        if (image.getType() != BufferedImage.TYPE_BYTE_INDEXED) {
            outImage = ColorReducer.reduce24(image, 16);
        } else {
            outImage = image;
        }

        IndexColorModel cm = (IndexColorModel) outImage.getColorModel();
        StringBuilder sb = new StringBuilder();

        sb.append("/* ").append(imageName).append(": */\n");
        sb.append("/* Width:  ").append(width).append(" */\n");
        sb.append("/* Height: ").append(height).append(" */\n");
        sb.append("/* Format: Generic Bitmap 4BPP */\n");
        sb.append("/* Palette Format: 5:6:5 (16 bits) */\n");
        sb.append("const unsigned char ");
        sb.append(Util.replaceInvalidCharacters(Util.getBaseName(imageName)));
        sb.append("[] = {\n");

        //Color Depth
        sb.append("0x00, 0x04, //Color Depth\n");

        //Image Size (16 bits)
        sb.append(wordToHex((short) height)).append(", //Height\n");
        sb.append(wordToHex((short) width)).append(", //Width\n");
        
        //Generating palette
        byte r[] = new byte[16];
        byte g[] = new byte[16];
        byte b[] = new byte[16];

        cm.getReds(r);
        cm.getGreens(g);
        cm.getBlues(b);

        //5:6:5 format supported for now
        for (int i = 0; i < 16; i++) {
            int color = ((r[i] & 0x1F) << 11) | ((g[i] & 0x3F) << 5) | (b[i] & 0x1F);

            sb.append(wordToHex((short) color)).append(",\n");
        }

        //Generating pixels
        int count = 0;
        for (int y = 0; y < height; y++) {
            int color = 0;
            
            for (int x = 0; x < width; x++) {
                int pixelColor = outImage.getRGB(x, y) & 0x00FFFFFF;
                int pixelIndex = getColorIndex(r, g, b, pixelColor);

                if ((x % 2) == 0) {
                    //First pixel in byte
                    color = (pixelIndex & 0x0F);
                } else {
                    //Second pixel in byte
                    color |= (pixelIndex & 0x0F) << 4;

                    sb.append("0x").append(byteToHex((byte) color)).append(", ");
                    count++;
                    color = 0;

                    if (count == 16) {
                        sb.append("\n");
                        count = 0;
                    }
                }
            }

            if ((width % 2) == 1) { //Width is ODD
                sb.append("0x").append(byteToHex((byte) color)).append(", ");
                count++;

                if (count == 16) {
                    sb.append("\n");
                    count = 0;
                }
            }
        }

        sb.append("\n};\n");

        return sb.toString();
    }

    public String generateCCode() {
        switch (outputFormat) {
            case RawImage.GENERIC_1BPP_PAGED:
                return generateCCodeForGeneric1BPPPaged();
            case RawImage.WINDOWS_BMP_4BPP:
                return generateCCodeForWindowsBitmap4BPP();
            case RawImage.GENERIC_1BPP_LINEAR:
                return generateCCodeForGeneric1BPPLinear();
            case RawImage.GENERIC_BMP_4BPP:
                return generateCCodeForGenericBitmap4BPP();
            default:
                return "/* Unsupported format */\n";
        }
    }

    @Override
    public String toString() {
        return imageName;
    }
}
