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
        ArrayList<HashMap> ovwSubArr = getObjectOr(data, "overview", new ArrayList());
        HashMap ovwSubData;
        if (sumSubArr.size() != ovwSubArr.size()) {
            log.error("THE RECORDS IS NOT MATCHED BETWEEN SUMMARY.OUT AND OVERVIEW.OUT");
            return;
        }

        Object buf = data.get("meta");
        BufferedReader brCsv;
        // If Output File File is no been found
        if (buf == null) {
            log.error("CSV FILE IS MISSING IN THE INPUT ZIP PACKAGE");
            return;
        } else {
            if (buf instanceof char[]) {
                brCsv = new BufferedReader(new CharArrayReader((char[]) buf));
            } else {
                brCsv = (BufferedReader) buf;
            }
        }

        // Get simulation output values from output files by experiment id
        HashMap<String, String> sumValMap = new HashMap();
//        ArrayList<String> sumValArr = new ArrayList();
        String version = getObjectOr(sumData, "vevsion", "Ver. N/A");
        StringBuilder sbData;
        for (int i = 0; i < sumSubArr.size(); i++) {
            sbData = new StringBuilder();
            sumSubData = sumSubArr.get(i);
            ovwSubData = ovwSubArr.get(i);
            String runno_sum = getObjectOr(sumSubData, "runno", "sum");
            String runno_ovw = getObjectOr(ovwSubData, "runno", "ovm");
            String trno = getObjectOr(ovwSubData, "trno", "1");
            String pdat = formatDateStr(getObjectOr(sumSubData, "pdat", ""), "");
            String exp_id = getObjectOr(ovwSubData, "exp_id", "");
            String key = exp_id + "__" + trno + "," + pdat;
            if (Integer.parseInt(runno_sum) > 999) {
                runno_ovw = (Integer.parseInt(runno_ovw)) + 1000 + "";
            }
            if (!runno_sum.equals(runno_ovw)) {
                log.warn("THE ORDER OF No." + (i + 1) + " RECORD [" + exp_id + "] IS NOT MATCHED BETWEEN SUMMARY AND OVERVIEW OUTPUT FILE");
                continue;
            }

            // Create CSV data
            if (!sumValMap.containsKey(key)) {
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
                sumValMap.put(key, sbData.toString()); // P.S. since non-DSSAT model won't have multiple treament, thus trno is not used as the part of key
            } else {
                log.warn("REPEATED RECORD IN SUMMARY FILE WITH SAME PDAT AND EXNAME");
            }
//            sumValArr.add(sbData.toString());

        }

        // Write CSV File
        outputCsvPath = revisePath(outputCsvPath);
        outputFile = createCsvFile(outputCsvPath);
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
        String line;
        String titleLine = "";
        String[] titles;
        int curDataLineNo = 1;
        // Write titles
        while ((line = brCsv.readLine()) != null) {
            if (line.startsWith("*") || line.startsWith("\"*\"")) {
                break;
            } else {
                bw.write(line);
                bw.write("\r\n");
                curDataLineNo++;
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
        int exnameCol = getIndex(titles, "EXNAME");
        int cropModelCol = getIndex(titles, "CROP_MODEL");
        if (pdateCol < 0 || exnameCol < 0 || cropModelCol < 0) {
            log.error("MISSING TITLE FOR PDATE, EXNAME OR CROP_MODEL IN LINE " + (curDataLineNo - 1));
            bw.write("MISSING TITLE FOR PDATE, EXNAME OR CROP_MODEL IN LINE " + (curDataLineNo - 1));
            bw.close();
            return;
        }

        // Write data
        while (line != null) {

            // currently exname (exp_id) is located in the 3rd spot of row
            String[] tmp = line.split(",");
            if (tmp.length < pdateCol + 1 || tmp[exnameCol].trim().equals("") || tmp[pdateCol].trim().equals("")) {
                bw.write(line);
                log.warn("MISSING EXNAME OR SDAT IN LINE " + curDataLineNo);
            } else {
                tmp[pdateCol] = tmp[pdateCol].replaceAll("/", "");
                // remove the comma for blank cell which will be filled with output value
                line = trimComma(tmp, cropModelCol);
                bw.write(line);

                // wirte simulation output info
                if (!tmp[exnameCol].matches("\\w+_+\\d+")) {
                    tmp[exnameCol] += "__1";
                } else if (!tmp[exnameCol].matches("\\w+__\\d+")) {
                    tmp[exnameCol] = tmp[exnameCol].replaceAll("_+", "__");
                }
                String scvKey = tmp[exnameCol] + "," + tmp[pdateCol];
                if (sumValMap.containsKey(scvKey)) {
                    bw.write(sumValMap.remove(scvKey)); // P.S. temporal way for multiple treatment
                } else {
                    bw.write(",\"DSSAT\"");
                    log.warn("THE SIMULATION OUTPUT DATA FOR [" + scvKey + "] IS MISSING");
//                    if (curDataLineNo - 4 < sumValArr.size()) {
//                        bw.write(sumValArr.get(curDataLineNo - 4));
//                    } else {
//                        log.warn("THE SIMULATION OUTPUT DATA FOR [" + tmp[2] + "] IS MISSING");
//                    }
                }
            }

            bw.write("\r\n");
            curDataLineNo++;
            line = brCsv.readLine();
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
        File f = new File(outputCsvPath + "ACMO.csv");
        int count = 1;
        while (f.exists()) {
            f = new File(outputCsvPath + "ACMO (" + count + ").csv");
            count++;
        }
        return f;
    }
}
