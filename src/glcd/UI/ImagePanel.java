package glcd.UI;

import java.awt.*;
import javax.swing.*;

/**
 *
 * @author Ivan Deras
 */
public class ImagePanel extends JPanel {

    private ImageItem imageItem;

    public ImagePanel() 
    {
        imageItem = null;
    }

    public ImageItem getImageItem() {
        return imageItem;
    }

    public void setImageItem(ImageItem imageItem) {
        this.imageItem = imageItem;
        repaint();
    }

    public void scaleImage(float scale) {
        float scaleX = imageItem.getScaleX();
        float scaleY = imageItem.getScaleY();
        
        scaleX += scale;
        scaleY += scale;
        
        imageItem.setScaleX(scaleX);
        imageItem.setScaleY(scaleY);
        
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        super.paint(g);
        if (imageItem == null) {
            g.setFont(new Font("Sans", Font.PLAIN, 18));
            g.drawString("Please select an image ...", 10, 50);
        } else {
            g2d.scale(imageItem.getScaleX(), imageItem.getScaleY());
            g.drawImage(imageItem.getImage(), 4, 4, null);
        }
    }
}
