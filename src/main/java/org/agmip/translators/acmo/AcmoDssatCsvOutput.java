package org.agmip.translators.acmo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import static org.agmip.translators.acmo.AcmoCommonOutput.*;
import static org.agmip.util.MapUtil.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DSSAT AFile Data I/O API Class
 *
 * @author Meng Zhang
 * @version 1.0
 */
public class AcmoDssatCsvOutput extends AcmoCommonOutput {

    private static final Logger log = LoggerFactory.getLogger(AcmoDssatCsvOutput.class);

    /**
     * Get output file object
     */
    @Override
    public File getOutputFile() {
        return outputFile;
    }

    /**
     * Generate ACMO CSV file
     *
     * @param outputCsvPath The path for output csv file
     * @param data The data holder for model output data and meta data
     */
    public void writeFile(String outputCsvPath, Map data) throws IOException {

        HashMap sumData = getObjectOr(data, "summary", new HashMap());
        ArrayList<HashMap> sumSubArr = getObjectOr(sumData, "data", new ArrayList<HashMap>());
        HashMap sumSubData;

        Object buf = data.get("meta");
        BufferedReader brCsv;
        // If Output File File is no been found
        if (buf == null) {
            log.error("META DATA FILE IS MISSING");
            return;
        } else {
            if (buf instanceof char[]) {
                brCsv = new BufferedReader(new CharArrayReader((char[]) buf));
            } else {
                brCsv = (BufferedReader) buf;
            }
        }

        // Get Model version
        String version = getObjectOr(sumData, "vevsion", "Ver. N/A");
        StringBuilder sbData;

        // Write CSV File
        outputCsvPath = revisePath(outputCsvPath);
        outputFile = createCsvFile(outputCsvPath);
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
        String line;
        String titleLine = "";
        String[] titles;
//        int curDataLineNo = 1;
        // Write titles
        while ((line = brCsv.readLine()) != null) {
            if (line.startsWith("*") || line.startsWith("\"*\"")) {
                break;
            } else {
                bw.write(line);
                bw.write("\r\n");
//                curDataLineNo++;
                titleLine = line;
            }
        }

        // Get titles
        if (titleLine.endsWith("\"")) {
            titleLine = titleLine.substring(0, titleLine.length() - 1);
        }
        titles = titleLine.split("\"?,\"?");

        // Get key item position
        int pdateCol = getIndex(titles, "PDATE");
        int sdatCol = getIndex(titles, "SDAT");
        int hdateCol = getIndex(titles, "HDATE");
        int cropModelCol = getIndex(titles, "CROP_MODEL");
        if (cropModelCol < 0) {
            log.error("MISSING TITLE <CROP_MODEL> TO LOCATE OUTPUT POSITION");
            bw.write("MISSING TITLE <CROP_MODEL> TO LOCATE OUTPUT POSITION");
            bw.close();
            return;
        }

        // Write data
        int index = 0;
        do {
            // Check if the index is over the limitation.
            String[] tmp = line.split(",");
            tmp[pdateCol] = formatDateStr(tmp[pdateCol]);
            tmp[sdatCol] = formatDateStr(tmp[sdatCol]);
            tmp[hdateCol] = formatDateStr(tmp[hdateCol]);
            // remove the comma for blank cell which will be filled with output value
            line = trimComma(tmp, cropModelCol);
            bw.write(line);

            // wirte simulation output info
            if (index >= sumSubArr.size()) {
                bw.write(",\"DSSAT\"");
            } else {
                sbData = new StringBuilder();
                sumSubData = sumSubArr.get(index);
                sbData.append(",\"DSSAT\",\"DSSAT ").append(getObjectOr(sumSubData, "model", "")).append(" ").append(version).append("\""); // MODEL_VER
                sbData.append(",\"").append(getObjectOr(sumSubData, "hwah", "")).append("\""); // HWAH
                sbData.append(",\"").append(getObjectOr(sumSubData, "cwam", "")).append("\""); // CWAH
                sbData.append(",\"").append(formatDateStr(getObjectOr(sumSubData, "adat", ""))).append("\""); // ADAT
                sbData.append(",\"").append(formatDateStr(getObjectOr(sumSubData, "mdat", ""))).append("\""); // MDAT
                sbData.append(",\"").append(formatDateStr(getObjectOr(sumSubData, "hdat", ""))).append("\""); // HDATE
                sbData.append(",\"").append(getObjectOr(sumSubData, "laix", "")).append("\""); // LAIX
                sbData.append(",\"").append(getObjectOr(sumSubData, "prcp", "")).append("\""); // PRCP
                sbData.append(",\"").append(getObjectOr(sumSubData, "etcp", "")).append("\""); // ETCP
                sbData.append(",\"").append(getObjectOr(sumSubData, "nucm", "")).append("\""); // NUCM
                sbData.append(",\"").append(getObjectOr(sumSubData, "nlcm", "")).append("\""); // NLCM
                bw.write(sbData.toString());
            }

            bw.write("\r\n");
//            curDataLineNo++;
            index++;
        } while((line = brCsv.readLine()) != null);

        // Check if there is record not been output yet
        if (index != sumSubArr.size()) {
            log.warn("The recoreds in Summary.out are not match with the ones in meta data.");
        }

        bw.close();
    }

    /**
     * Remove the comma in the end of the line and combine to a new String
     *
     * @param strs input array of string which is splited by comma
     * @param length the expected length of that array
     * @return
     */
    private String trimComma(String[] strs, int length) {
        StringBuilder sb = new StringBuilder();
        sb.append(strs[0]);
        int min = Math.min(strs.length, length);
        for (int i = 1; i < min; i++) {
            sb.append(",").append(strs[i]);
        }
        for (int i = min; i < length; i++) {
            sb.append(",");
        }
        return sb.toString();
    }

    /**
     * Get the index number for the targeted title
     *
     * @param titles The array of titles
     * @param name The name of title
     * @return The index of title in the line
     */
    private int getIndex(String[] titles, String name) {

        for (int i = 0; i < titles.length; i++) {
            if (titles[i].equals(name)) {
                return i;
            }
        }
        return -1;
    }

    private File createCsvFile(String outputCsvPath) {
        File f = new File(outputCsvPath + "ACMO_DSSAT.csv");
        int count = 1;
        while (f.exists()) {
            f = new File(outputCsvPath + "ACMO_DSSAT (" + count + ").csv");
            count++;
        }
        return f;
    }
}