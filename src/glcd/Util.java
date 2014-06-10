package glcd;

import glcd.GLCDImageLoader.GLCDImageLoaderException;
import glcd.GLCDImageLoader.Lexer;
import glcd.GLCDImageLoader.Parser;
import glcd.GLCDImageLoader.RawImage;
import glcd.GLCDImageLoader.RawImageData;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 *
 * @author Ivan Deras
 */
public class Util {

    public static String getBaseName(String fileName) {
        if (fileName.isEmpty()) {
            return "";
        }

        int index = fileName.lastIndexOf('.');
        if (index != -1) {
            return fileName.substring(0, index);
        } else {
            return fileName;
        }
    }

    public static String rename(String name, String ext) {
        return getBaseName(name) + "." + ext;
    }

    public static String replaceInvalidCharacters(String identifier) {
        if (identifier.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        char ch = identifier.charAt(0);
        if (Character.isDigit(ch)) {
            sb.append('_');
        }

        for (int i = 0; i < identifier.length(); i++) {
            ch = identifier.charAt(i);
            if (Character.isDigit(ch)
                    || Character.isLetter(ch)) {
                sb.append(ch);
            } else {
                sb.append('_');
            }
        }

        return sb.toString();
    }

    public static List<RawImage> importImageFromCSource(File imageFile, int format) throws IOException, GLCDImageLoaderException {
        FileInputStream is;
        try {
            is = new FileInputStream(imageFile);
        } catch (FileNotFoundException ex) {
            return null;
        }
        ArrayList<RawImage> listRawImages = new ArrayList<RawImage>();
        BufferedInputStream in = new BufferedInputStream(is);
        Lexer lexer = new Lexer(in);
        Parser parser = new Parser(lexer);
        RawImageData rawImageData = null;

        do {
            rawImageData = parser.getImageData();

            if (rawImageData != null) {
                if (format == RawImage.GENERIC_1BPP_PAGED) {
                    format = (rawImageData.getSize() == -1) ? RawImage.GENERIC_1BPP_PAGED_T1 : RawImage.GENERIC_1BPP_PAGED_T2;
                }

                RawImage rawImage = new RawImage(rawImageData.getRawData(), format, rawImageData.getName());
                listRawImages.add(rawImage);
            }
        } while (rawImageData != null);

        return listRawImages;
    }

    public static String getDateTime() {
        final String[] monthName = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};

        final Calendar now = GregorianCalendar.getInstance();
        int day = now.get(Calendar.DAY_OF_MONTH);
        int month = now.get(Calendar.MONTH);
        int year = now.get(Calendar.YEAR);
        int hour = now.get(Calendar.HOUR);
        int minute = now.get(Calendar.MINUTE);
        String am_pm = now.get(Calendar.AM_PM) == Calendar.AM ? "AM" : "PM";
        String date = day + " " + monthName[month] + " " + year;
        String time = hour + ":" + minute + " " + am_pm;

        return date + " " + time;
    }
}
