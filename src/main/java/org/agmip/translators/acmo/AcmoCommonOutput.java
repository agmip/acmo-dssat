package org.agmip.translators.acmo;

import java.io.File;
import java.util.Calendar;
import java.util.HashMap;
import org.agmip.core.types.TranslatorOutput;
import static org.agmip.util.MapUtil.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DSSAT Experiment Data I/O API Class
 *
 * @author Meng Zhang
 * @version 1.0
 */
public abstract class AcmoCommonOutput implements TranslatorOutput {

    private static final Logger log = LoggerFactory.getLogger(AcmoCommonInput.class);
    // Default value for each type of value (R: real number; C: String; I: Integer; D: Date)
    protected String defValR = "0.00";
    protected String defValC = "";
    protected String defValI = "0";
    protected String defValD = "-99";
    protected String defValBlank = "";
    // construct the error message in the output
    protected StringBuilder sbError;
    protected File outputFile;

    /**
     * Format the number with maximum length and type
     *
     * @param bits Maximum length of the output string
     * @param str Input string of number
     * @return formated string of number
     */
    protected String formatNumStr(int bits, HashMap m, Object key, String defVal) {

        String ret = "";
        String str = getObjectOr(m, key, defVal);
        double decimalPower;
        long decimalPart;
        double input;
        String[] inputStr = str.split("\\.");
        if (str.trim().equals("")) {
            return String.format("%" + bits + "s", defVal);
        } else if (inputStr[0].length() > bits) {
            //throw new Exception();
            sbError.append("! Waring: There is a variable [").append(key).append("] with oversized number [").append(str).append("] (Limitation is ").append(bits).append("bits)\r\n");
            return String.format("%" + bits + "s", defVal);
        } else {
            ret = inputStr[0];

            if (inputStr.length > 1 && inputStr[0].length() < bits) {

                if (inputStr[1].length() <= bits - inputStr[0].length() - 1) {
                    ret = ret + "." + inputStr[1];
                } else {
                    try {
                        input = Math.abs(Double.valueOf(str));
                    } catch (Exception e) {
                        // TODO throw exception
                        return str;
                    }
                    //decimalPower = Math.pow(10, Math.min(bits - inputStr[0].length(), inputStr[1].length()) - 1);
                    decimalPower = Math.pow(10, bits - inputStr[0].length() - 1);
                    decimalPart = Double.valueOf(Math.round(input * decimalPower) % decimalPower).longValue();
                    ret = ret + "." + (decimalPart == 0 && (bits - inputStr[0].length() < 2) ? "" : decimalPart);
                }
            }
            if (ret.length() < bits) {
                ret = String.format("%1$" + bits + "s", ret);
            }
        }

        return ret;
    }

    /**
     * Translate data str from "yyyyddd" to "yyyy-MM-dd"
     *
     * @param str date string with format of "yyyyddd"
     * @return result date string with format of "yyyy-MM-dd"
     */
    protected static String formatDateStr(String str) {

        return formatDateStr(str, "-");
    }

    /**
     * Translate data str from "yyyyddd" to "yyyy-MM-dd" plus days you want
     *
     * @param date date string with format of "yyyyddd"
     * @param strDays the number of days need to be added on
     * @return result date string with format of "yyyy-MM-dd"
     */
    protected static String formatDateStr(String date, String seperator) {

        // Initial Calendar object
        Calendar cal = Calendar.getInstance();
        date = date.replaceAll("\\D", "");
        try {
            // Set date with input value
            cal.set(Calendar.YEAR, Integer.parseInt(date.substring(0, 4)));
            if (date.length() == 8) {
                cal.set(Calendar.MONTH, Integer.parseInt(date.substring(4, 6)) - 1);
                cal.set(Calendar.DATE, Integer.parseInt(date.substring(6)));
            } else {
                cal.set(Calendar.DAY_OF_YEAR, Integer.parseInt(date.substring(4)));
            }
            return String.format("%1$04d" + seperator + "%2$02d" + seperator + "%3$02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH));
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * Revise output path
     *
     * @param path the output path
     * @return revised path
     */
    public static String revisePath(String path) {
        if (!path.trim().equals("")) {
//            path = path.replaceAll("/", File.separator);
            if (!path.endsWith(File.separator)) {
                path += File.separator;
            }
            File f = new File(path);
            if (f.isFile()) {
                f = f.getParentFile();
            }
            if (f != null && !f.exists()) {
                f.mkdirs();
            }
        }
        return path;
    }

    /**
     * Get output file object
     */
    public File getOutputFile() {
        return outputFile;
    }

    /**
     * Set default value for missing data
     *
     */
    protected void setDefVal() {

        // defValD = ""; No need to set default value for Date type in weather file
        defValR = "-99";
        defValC = "-99";
        defValI = "-99";
        sbError = new StringBuilder();
        outputFile = null;
    }
}
