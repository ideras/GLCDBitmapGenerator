/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package glcd.UI;

import java.awt.Color;

/**
 *
 * @author ideras
 */
public class GLCDColorProfile {
    private final Color backColor;
    private final Color pixelColor;
    private final Color pixelOnBorderColor;
    private final Color pixelOffBorderColor;

    public GLCDColorProfile(Color backColor, Color pixelColor, Color pixelOnBorderColor, Color pixelOffBorderColor) {
        this.backColor = backColor;
        this.pixelColor = pixelColor;
        this.pixelOnBorderColor = pixelOnBorderColor;
        this.pixelOffBorderColor = pixelOffBorderColor;
    }

    public Color getBackColor() {
        return backColor;
    }

    public Color getPixelColor() {
        return pixelColor;
    }

    public Color getPixelOnBorderColor() {
        return pixelOnBorderColor;
    }

    public Color getPixelOffBorderColor() {
        return pixelOffBorderColor;
    }
    
    public static final GLCDColorProfile GREEN_PROFILE = new GLCDColorProfile( new Color(0x95c200), //Background color
                                                                               Color.BLACK,         //Pixel On Color
                                                                               new Color(0x42802b), //Pixel On border color
                                                                               new Color(0x89bb1d)  //Pixel off border color
                                                                             );
    
    public static final GLCDColorProfile BLUE_PROFILE = new GLCDColorProfile(  new Color(0x0080C1), //Background color
                                                                               Color.WHITE,         //Pixel On Color
                                                                               new Color(0xDBFFFF), //Pixel On border color
                                                                               new Color(0x1A89C1)  //Pixel off border color
                                                                             );
}
