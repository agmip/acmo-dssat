package org.agmip.translators.acmo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.agmip.core.types.TranslatorInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DSSAT Experiment Data I/O API Class
 *
 * @author Meng Zhang
 * @version 1.0
 */
public abstract class AcmoCommonInput implements TranslatorInput {
    private static final Logger log = LoggerFactory.getLogger(AcmoCommonInput.class);
    protected String[] flg = {"", "", ""};
    protected int flg4 = 0;
    protected String defValR = "-99.0";
    protected String defValC = "";
    protected String defValI = "-99";
    protected String defValD = "20110101";
    protected String jsonKey = "unknown";

    /**
     * DSSAT Data Output method for Controller using
     *
     * @param m The holder for BufferReader objects for all files
     * @return result data holder object
     */
    protected abstract HashMap readFile(HashMap m) throws IOException;

    /**
     * DSSAT XFile Data input method, always return the first data object
     *
     * @param arg0 file name
     * @return result data holder object
     */
    @Override
    public HashMap readFile(String arg0) {

        HashMap ret = new HashMap();
        String filePath = arg0;

        try {
            // read file by file
            ret = readFile(getBufferReader(filePath));

        } catch (FileNotFoundException fe) {
            System.out.println("File not found under following path : [" + filePath + "]!");
            return ret;
        } catch (Exception e) {
            //System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return ret;
//        return readFileById(arg0, 0);
    }
    
    /**
     * Set reading flgs for reading lines
     *
     * @param line the string of reading line
     */
    protected void judgeContentType(String line) {
        // Section Title line
        if (line.startsWith("*")) {

            setTitleFlgs(line);
            flg4 = 0;

        } // Data title line
        else if (line.startsWith("@")) {

            flg[1] = line.substring(1).trim().toLowerCase();
            flg[2] = "title";
            flg4++;

        } // Comment line
        else if (line.startsWith("!")) {

            flg[2] = "comment";

        } // Data line
        else if (!line.trim().equals("")) {

            flg[2] = "data";

        } // Continued blank line
        else if (flg[2].equals("blank")) {

            flg[0] = "";
            flg[1] = "";
            flg[2] = "blank";
            flg4 = 0;

        } else {

//            flg[0] = "";
            flg[1] = "";
            flg[2] = "blank";
        }
    }

    /**
     * Set reading flgs for title lines (the line marked with *)
     *
     * @param line the string of reading line
     */
    protected abstract void setTitleFlgs(String line);

    /**
     * Divide the data in the line into a map (Default invalid value is null,
     * which means not to be sore in the json)
     *
     * @param line The string of line read from data file
     * @param formats The definition of length for each data field (String
     * itemName : Integer length)
     * @return the map contains divided data with keys from original string
     */
    protected HashMap readLine(String line, LinkedHashMap<String, Integer> formats) {

        return readLine(line, formats, null);
    }

    /**
     * Divide the data in the line into a map
     *
     * @param line The string of line read from data file
     * @param formats The definition of length for each data field (String
     * itemName : Integer length)
     * @param invalidValue The text will replace the original reading when its
     * value is invalid
     * @return the map contains divided data with keys from original string
     */
    protected HashMap readLine(String line, LinkedHashMap<String, Integer> formats, String invalidValue) {

        HashMap ret = new HashMap();
        int length;
        String tmp;

        for (String key : formats.keySet()) {
            // To avoid to be over limit of string lenght
            length = Math.min((Integer) formats.get(key), line.length());
            if (!((String) key).equals("") && !((String) key).startsWith("null")) {
                tmp = line.substring(0, length).trim();
                // if the value is in valid keep blank string in it
                if (checkValidValue(tmp)) {
                    ret.put(key, tmp);
                } else {
                    if (invalidValue != null) {
                        ret.put(key, invalidValue);   // P.S. "" means missing or invalid value
                    }
                }
            }
            line = line.substring(length);
        }

        return ret;
    }

    /**
     * Check if input is a valid value
     *
     * @return check result
     */
    protected boolean checkValidValue(String value) {
        if (value == null || value.trim().equals(defValC) || value.equals(defValI) || value.equals(defValR)) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Get BufferReader for each type of file
     *
     * @param filePath the full path of the input file
     * @return result the holder of BufferReader for different type of files
     * @throws FileNotFoundException
     * @throws IOException
     */
    protected static HashMap getBufferReader(String filePath) throws FileNotFoundException, IOException {

        HashMap result = new HashMap();
        InputStream in;
        String[] tmp = filePath.split("[\\/]");

        // If input File is ZIP file
        if (filePath.toUpperCase().endsWith(".ZIP")) {

            // Get experiment name
            ZipEntry entry;

            // Read Files
            in = new ZipInputStream(new FileInputStream(filePath));

            while ((entry = ((ZipInputStream) in).getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    
                    if (entry.getName().toUpperCase().endsWith(".OUT")) {
                        result.put(entry.getName().toUpperCase(), getBuf(in, (int) entry.getSize()));
                    } else if (entry.getName().toUpperCase().equals("ACMO_META.DAT")) {
                        result.put("CSV", getBuf(in, (int) entry.getSize()));
                    } else if (entry.getName().toUpperCase().endsWith(".JSON")) {
                        result.put(entry.getName().toUpperCase(), getBuf(in, (int) entry.getSize()));
                    }
                }
            }
        } // If input File is not ZIP file
        else {
            in = new FileInputStream(filePath);
            File f = new File(filePath);
            if (f.getName().toUpperCase().endsWith(".OUT")) {
                result.put(f.getName().toUpperCase(), new BufferedReader(new InputStreamReader(in)));
            } else if (f.getName().toUpperCase().endsWith(".CSV")) {
                result.put("CSV", new BufferedReader(new InputStreamReader(in)));
            } else if (filePath.toUpperCase().endsWith(".JSON")) {
                result.put(f.getName().toUpperCase(), new BufferedReader(new InputStreamReader(in)));
            }
        }

        return result;
    }

    /**
     * Get BufferReader object from Zip entry
     *
     * @param in The input stream of zip file
     * @param size The entry size
     * @return result The char array for current entry
     * @throws IOException
     */
    private static char[] getBuf(InputStream in, int size) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(in));
        char[] buf = new char[size];
        br.read(buf);
        return buf;
    }

    /**
     * Get a copy of input map
     *
     * @param m input map
     * @return the copy of whole input map
     */
    protected static HashMap CopyList(HashMap m) {
        HashMap ret = new HashMap();

        for (Object key : m.keySet()) {
            if (m.get(key) instanceof String) {
                ret.put(key, m.get(key));
            } else if (m.get(key) instanceof HashMap) {
                ret.put(key, CopyList((HashMap) m.get(key)));
            } else if (m.get(key) instanceof ArrayList) {
                ret.put(key, CopyList((ArrayList) m.get(key)));
            }
        }

        return ret;
    }

    /**
     * Get a copy of input array
     *
     * @param arr input ArrayList
     * @return the copy of whole input array
     */
    protected static ArrayList CopyList(ArrayList arr) {
        ArrayList ret = new ArrayList();
        for (int i = 0; i < arr.size(); i++) {
            if (arr.get(i) instanceof String) {
                ret.add(arr.get(i));
            } else if (arr.get(i) instanceof HashMap) {
                ret.add(CopyList((HashMap) arr.get(i)));
            } else if (arr.get(i) instanceof ArrayList) {
                ret.add(CopyList((ArrayList) arr.get(i)));
            }
        }

        return ret;
    }

    /**
     * Add the new item into array by having same key value
     *
     * @param arr the target array
     * @param item the input item which will be added into array
     * @param key the primary key item's name
     */
    protected void addToArray(ArrayList arr, HashMap item, Object key) {
        HashMap elem;
        boolean unmatchFlg = true;

        // Added logging (cv)
        log.debug("Array: {}", arr.toString());
        log.debug("Item: {}", item.toString());
        log.debug("Key: {}", key);
        
        
        for (int i = 0; i < arr.size(); i++) {
            elem = (HashMap) arr.get(i);
            if (!key.getClass().isArray()) {
                if (elem.get(key).equals(item.get(key))) {
                    elem.putAll(item);
                    unmatchFlg = false;
                    break;
                }
            } else {
                Object[] keys = (Object[]) key;
                boolean equalFlg = true;
                for (int j = 0; j < keys.length; j++) {
                    if (!elem.get(keys[j]).equals(item.get(keys[j]))) {
                        equalFlg = false;
                        break;
                    }
                }
                if (equalFlg) {
                    elem.putAll(item);
                    unmatchFlg = false;
                    break;
                }
            }
        }
        if (unmatchFlg) {
            arr.add(item);
        }
    }
}
