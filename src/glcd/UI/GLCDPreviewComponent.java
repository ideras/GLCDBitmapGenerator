package glcd.UI;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;

/**
 *
 * @author Ivan Deras
 */
public class GLCDPreviewComponent extends JComponent {

    private final int X_OFFSET = 4; //X offset
    private final int Y_OFFSET = 4; //Y offset
    private final int PIXEL_SIZE = 4; // 4x4 pixel size
    private final int width, height;

    private final GLCDColorProfile colorProfile = GLCDColorProfile.GREEN_PROFILE;

    private final byte[] screenContent;

    public GLCDPreviewComponent(int width, int height) {
        this.width = width;
        this.height = height;
        this.screenContent = new byte[(width * height + 7) / 8];

        setSize(width * PIXEL_SIZE + X_OFFSET * 2, height * PIXEL_SIZE + Y_OFFSET * 2);
    }

    private void paintPixel(Graphics2D g, int x, int y, boolean set) {
        Color borderColor = set ? colorProfile.getPixelOnBorderColor() : colorProfile.getPixelOffBorderColor();
        Color pixelColor = set ? colorProfile.getPixelColor() : colorProfile.getBackColor();

        g.setColor(borderColor);
        g.drawLine(x, y, x + PIXEL_SIZE, y);
        g.drawLine(x, y, x, y + PIXEL_SIZE);
        g.setColor(pixelColor);
        g.fillRect(x + 1, y + 1, PIXEL_SIZE - 1, PIXEL_SIZE - 1);
    }

    public void drawImage(int x, int y, ImageItem imgItm) {
        byte[] imgData = imgItm.getData1BPP();
        int pos = x + y * width;
        int index = pos / 8;
        int mask = 1 << (7 - pos % 8);
        int imageX = 0, imageY = 0;
        
        System.out.println("Threshold: " + imgItm.getThreshold());
        for (int i = 0; i < imgData.length; i++) {
            byte b = imgData[i];

            for (int bitPos = 7; bitPos >= 0; bitPos--) {

                if ((b & (1 << bitPos)) != 0) {
                    screenContent[index] |= mask;
                    System.out.print("1");
                } else {
                    screenContent[index] &= ~mask;
                    System.out.print("0");
                }
                mask >>= 1;
                imageX++;

                if (mask == 0) {
                    mask = 0x80;
                    index++;
                }

                if (imageX >= imgItm.getWidth()) {
                    y++; imageY++;
                    imageX = 0;
                    pos = x + y * width;
                    index = pos / 8;
                    mask = 1 << (7 - pos % 8);

                    System.out.println();
                    if (imageY >= imgItm.getHeight()) {
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        g2d.setColor(colorProfile.getBackColor());
        g2d.fillRect(0, 0, getWidth(), getHeight());

        int x = 0, y = 0;
        int row = Y_OFFSET;
        int column = X_OFFSET;

        for (int i = 0; i < screenContent.length; i++) {
            byte b = screenContent[i];

            for (int bitPos = 7; bitPos >= 0; bitPos--) {
                paintPixel(g2d, column, row, (b & (1 << bitPos)) != 0);
                column += PIXEL_SIZE;
                x++;

                if (x >= width) {
                    y++;
                    x = 0;
                    row += PIXEL_SIZE;
                    column = X_OFFSET;

                    if (y >= height) {
                        break;
                    }
                }
            }
        }
    }
}
