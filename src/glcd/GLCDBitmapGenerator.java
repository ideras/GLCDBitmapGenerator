package glcd;

import glcd.UI.MainForm;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author Ivan Deras
 */
public class GLCDBitmapGenerator
{

    /**
     * @param args the command line arguments
     */
    public static void main(String args[])
    {
        /* Set the Windows look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            // Set System L&F
            String systemLookAndFeel = UIManager.getSystemLookAndFeelClassName();
            
            UIManager.setLookAndFeel(systemLookAndFeel);
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println(e.getStackTrace());
        } catch (ClassNotFoundException e) {
            System.err.println(e.getStackTrace());
        } catch (InstantiationException e) {
            System.err.println(e.getStackTrace());
        } catch (IllegalAccessException e) {
            System.err.println(e.getStackTrace());
        }
        //</editor-fold>

        new MainForm().setVisible(true);
    }
}
