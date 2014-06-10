/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package glcd.GLCDImageLoader;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.util.ArrayList;

/**
 *
 * @author ideras
 */
public class RawImage {

    private ArrayList<Integer> rawData;
    private int format;
    String name;

    public RawImage(ArrayList<Integer> rawData, int format, String name) {
        this.rawData = rawData;
        this.format = format;
        this.name = name;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public ArrayList<Integer> getRawData() {
        return rawData;
    }

    public void setRawData(ArrayList<Integer> rawData) {
        this.rawData = rawData;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //KS0108 use 8 bits pages
    private void drawPage(BufferedImage img, int pageData, int row, int col) {
        for (int i = 0; i < 8; i++) {
            if ((pageData & (1 << i)) != 0) {
                img.setRGB(col, row, 0x00);
            } else {
                img.setRGB(col, row, 0xFFFFFF);
            }
            row++;
        }
    }

    private BufferedImage buildImageFromKS0108Format(int width, int height, int pageCount, int index) throws GLCDImageLoaderException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        int row = 0;

        try {
            for (int page = 0; page < pageCount; page++) {
                for (int col = 0; col < width; col++) {
                    drawPage(image, rawData.get(index).intValue(), row, col);
                    index++;
                }
                row += 8;
            }
        } catch (IndexOutOfBoundsException ex) {
            throw new GLCDImageLoaderException("Error converting image '" + name + "'. Invalid image format");
        }

        return image;
    }

    private int getRGB(int r, int g, int b) {
        return ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | ((b & 0xFF));
    }

    private BufferedImage buildImageFromWindowsBitmap4BPP() {
        int i, j;
        int imageX, imageY;

        int biHeaderSize = rawData.get(14);
        int width = rawData.get(18);
        int height = rawData.get(22);

        // check for clipping
        if ((height <= 0) || // bitmap is unexpectedly encoded in top-to-bottom pixel order
                ((width % 2) != 0)) { // bottom cut off

            return null;
        }

        byte[] b = new byte[16];
        byte[] g = new byte[16];
        byte[] r = new byte[16];

        //Read palette: 16 entries
        i = 14 + biHeaderSize;
        j = 0;
        while (j < 16) {
            b[j] = rawData.get(i).byteValue();
            i++;
            g[j] = rawData.get(i).byteValue();
            i++;
            r[j] = rawData.get(i).byteValue();
            i += 2;
            j++;
        }

        IndexColorModel palette = new IndexColorModel(4, 16, r, g, b);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, palette);

        // bitmaps are encoded backwards, so start at the bottom left corner of the image
        imageY = height - 1;
        imageX = 0;

        j = rawData.get(10);                  // byte 10 contains the offset where image data can be found
        for (i = 1; i <= (width * height / 2); i++) {
            // the left pixel is in the upper 4 bits
            int leftPixel = (rawData.get(j) >> 4) & 0xF;
            int rightPixel = rawData.get(j) & 0xF;

            int color = getRGB(r[leftPixel], g[leftPixel], b[leftPixel]);
            image.setRGB(imageX, imageY, color);
            imageX++;
            color = getRGB(r[rightPixel], g[rightPixel], b[rightPixel]);
            image.setRGB(imageX, imageY, color);
            imageX++;

            j++;
            if ((i % (width / 2)) == 0) {     // at the end of a row
                imageY--;
                imageX = 0;

                // bitmaps are 32-bit word aligned
                switch ((width / 2) % 4) {      // skip any padding
                    case 0:
                        j = j + 0;
                        break;
                    case 1:
                        j = j + 3;
                        break;
                    case 2:
                        j = j + 2;
                        break;
                    case 3:
                        j = j + 1;
                        break;
                }
            }
        }

        return image;
    }

    private BufferedImage buildImageFromGenericBitmap1BPP() throws GLCDImageLoaderException {
        try {
            int width = rawData.get(0).intValue();
            int height = rawData.get(1).intValue();

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            
            int x = 0, y = 0;
            for (int i = 2; i < rawData.size(); i++) {
                int value = rawData.get(i).intValue() & 0xFF;
                int mask = 0x80;
                
                while (mask != 0) {
                    if ((value & mask) != 0)
                        image.setRGB(x, y, 0x00);
                    else
                        image.setRGB(x, y, 0x00FFFFFF);
                    mask >>= 1;
                    x++;
                    if (x >= width) {
                        y++;
                        x = 0;
                        if (y >= height) break;
                    }
                }
            }
            
            return image;
            
        } catch (IndexOutOfBoundsException ex) {
            throw new GLCDImageLoaderException("Error converting image '" + name + "'. Invalid image format");
        }
    }

    public BufferedImage toImage() throws GLCDImageLoaderException {
        int width, height, pageCount;

        switch (format) {
            case GENERIC_1BPP_PAGED_T1:
                width = rawData.get(0).intValue();
                height = rawData.get(1).intValue();
                pageCount = height / 8;

                return buildImageFromKS0108Format(width, height, pageCount, 2);

            case GENERIC_1BPP_PAGED_T2:
                pageCount = rawData.size() / 128;
                width = 128;
                height = pageCount * 8;

                return buildImageFromKS0108Format(width, height, pageCount, 0);

            case WINDOWS_BMP_4BPP:
                return buildImageFromWindowsBitmap4BPP();
            case GENERIC_1BPP_LINEAR:
                return buildImageFromGenericBitmap1BPP();
            default:
                return null;
        }
    }

    public static final int GENERIC_1BPP_PAGED = 1; //1 bit per pixel, paged format
    public static final int GENERIC_1BPP_PAGED_T1 = 2; //1 bit per pixel, width and height set in the array
    public static final int GENERIC_1BPP_PAGED_T2 = 3; //1 bit per pixel, width and height need to be computed
    public static final int WINDOWS_BMP_4BPP = 4; //LM3S1968 or LM3S8962's 4-bit grayscale OLED display
    public static final int GENERIC_1BPP_LINEAR = 5; //Bitmap 1 bit per pixel, linear format
}
