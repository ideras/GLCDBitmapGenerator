package glcd.GLCDImageLoader;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;

/**
 *
 * @author ideras
 */
public class RawImage {

    private int[] rawData;
    private int format;
    private int width, height;
    String name;

    public RawImage(int[] rawData, int format, int width, int height, String name) {
        this.rawData = rawData;
        this.format = format;
        this.width = width;
        this.height = height;
        this.name = name;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public int[] getRawData() {
        return rawData;
    }

    public void setRawData(int[] rawData) {
        this.rawData = rawData;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    //Paged Format uses 8 bits per pages
    private void drawPage(BufferedImage img, int pageData, int row, int col) {
        for (byte mask = 1; mask != 0; mask <<= 1) {
            if ((pageData & mask) != 0) {
                img.setRGB(col, row, 0x00);
            } else {
                img.setRGB(col, row, 0xFFFFFF);
            }
            row++;
        }
    }

    private BufferedImage buildImageFrom1BPPPaged() throws GLCDImageLoaderException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        int row = 0, index = 0;
        int pageCount = height/8;

        try {
            for (int page = 0; page < pageCount; page++) {
                for (int col = 0; col < width; col++) {
                    drawPage(image, rawData[index], row, col);
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

        int biHeaderSize = rawData[14];
        width = rawData[18];
        height = rawData[22];

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
            b[j] = (byte)rawData[i];
            i++;
            g[j] = (byte)rawData[i];
            i++;
            r[j] = (byte)rawData[i];
            i += 2;
            j++;
        }

        IndexColorModel palette = new IndexColorModel(4, 16, r, g, b);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, palette);

        // bitmaps are encoded backwards, so start at the bottom left corner of the image
        imageY = height - 1;
        imageX = 0;

        j = rawData[10];                  // byte 10 contains the offset where image data can be found
        for (i = 1; i <= (width * height / 2); i++) {
            // the left pixel is in the upper 4 bits
            int leftPixel = (rawData[j] >> 4) & 0xF;
            int rightPixel = rawData[j] & 0xF;

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

    private BufferedImage buildImageFrom1BPPLinear() throws GLCDImageLoaderException {
        try {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            
            int x = 0, y = 0;
            for (int i = 0; i < rawData.length; i++) {
                int value = rawData[i] & 0xFF;
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
    
    private BufferedImage buildImageFromGenericBitmap4BPP() {
        int i, j;
        int x, y;

        if (rawData[1] != 4)
            return null;
        
        //Big Endian
        height = (rawData[3] << 8) | rawData[2];
        width = (rawData[5] << 8) | rawData[4];

        //Palette
        byte[] b = new byte[16];
        byte[] g = new byte[16];
        byte[] r = new byte[16];

        //Read palette: 16 entries
        i = 6;
        j = 0;
        while (j < 16) {
            int color = (rawData[i+1] << 8) | rawData[i];
            i += 2;
            
            //Decode 5:6:5 color
            byte red = (byte)(color >> 11);
            byte green = (byte)((color & 0x07E0) >> 5);
            byte blue = (byte)(color & 0x001F);
            
            r[j] = (byte)((red << 3) + 7);
            g[j] = (byte)((green << 2) + 3);
            b[j] = (byte)((blue << 3) + 7);
            j++;
        }

        IndexColorModel palette = new IndexColorModel(4, 16, r, g, b);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_INDEXED, palette);

        j = i;
        int data = 0, color, pixel;
        
        for (y = 0; y < height; y++) {
            for (x = 0; x < width; x++) {
                if ((x % 2) == 0) {
                    data = rawData[i++];
                    pixel = data & 0xF;
                } else {
                    pixel = (data & 0xF0) >> 4;
                }
            
                color = getRGB(r[pixel], g[pixel], b[pixel]);
                image.setRGB(x, y, color);
            }
        }

        return image;
    }

    public BufferedImage toImage() throws GLCDImageLoaderException {
        switch (format) {
            case GENERIC_1BPP_PAGED:
                return buildImageFrom1BPPPaged();
            case WINDOWS_BMP_4BPP:
                return buildImageFromWindowsBitmap4BPP();
            case GENERIC_1BPP_LINEAR:
                return buildImageFrom1BPPLinear();
            case GENERIC_BMP_4BPP:
                return buildImageFromGenericBitmap4BPP();
            default:
                return null;
        }
    }

    public static final int GENERIC_1BPP_PAGED = 1; //1 bit per pixel, paged format
    public static final int WINDOWS_BMP_4BPP = 2; //Windows Bitmap 4 bit per pixel
    public static final int GENERIC_1BPP_LINEAR = 3; //1 bit per pixel, linear format
    public static final int GENERIC_BMP_4BPP = 4; //Generic Bitmap 4 bit per pixel
}
