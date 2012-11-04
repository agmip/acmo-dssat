package org.agmip.translators.acmo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
public class AcmoCsvTranslator {

    private static final Logger log = LoggerFactory.getLogger(AcmoCommonInput.class);
    private File outputFile;

    /**
     * Get output file object
     */
    public File getOutputFile() {
        return outputFile;
    }

    /**
     * Generate ACMO CSV file
     *
     * @param outputCsvPath The path for output csv file
     * @param inputFilePath The path for input zip file which contains *.OUT and
     * acmo.json
     */
    public void writeCsvFile(String outputCsvPath, String inputFilePath) throws IOException {

        // Read input zip file
        HashMap brMap = AcmoCommonInput.getBufferReader(inputFilePath);

        // Get input csv file from zip
        Object buf = brMap.get("CSV");
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

        // Get input dssat simulation ouput files from zip
        AcmoDssatOutputFileInput dssatReader = new AcmoDssatOutputFileInput();
        HashMap sumData = dssatReader.readSummary(brMap);
        ArrayList<HashMap> sumSubArr = getObjectOr(sumData, "data", new ArrayList<HashMap>());
        HashMap sumSubData;
        ArrayList<HashMap> ovwSubArr = dssatReader.readOverview(brMap);
        HashMap ovwSubData;

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
            String pdat = formatDateStr(getObjectOr(sumSubData, "pdat", ""));
            String exp_id = getObjectOr(ovwSubData, "exp_id", "");
            String key = exp_id + "," + pdat;
            if (!runno_sum.equals(runno_ovw)) {
                log.warn("THE ORDER OF No." + (i + 1) + " RECORD [" + exp_id + "] IS NOT MATCHED BETWEEN SUMMARY AND OVERVIEW OUTPUT FILE");
                continue;
            }

            // Create CSV data
            if (!sumValMap.containsKey(key)) {
                sbData.append(",\"DSSAT ").append(getObjectOr(sumSubData, "model", "")).append(" ").append(version).append("\""); // MODEL_VER
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
        outputFile = new File(outputCsvPath + "ACMO.csv");
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
        String line;
        int curDataLineNo = 4;
        // Write titles
        for (int i = 1; i < curDataLineNo; i++) {
            bw.write(brCsv.readLine());
            bw.write("\r\n");
        }
        // Write data
        while ((line = brCsv.readLine()) != null) {

            // currently exname (exp_id) is located in the 3rd spot of row
            String[] tmp = line.split(",");
            if (tmp.length < 21 || tmp[3].trim().equals("") || tmp[20].trim().equals("")) {
                bw.write(line);
                log.warn("MISSING EXNAME OR SDAT IN LINE " + curDataLineNo);
            } else {
                tmp[20] = tmp[20].replaceAll("/", "-");
                // remove the comma for blank cell which will be filled with output value
                if (line.endsWith(",")) {
                    line = trimComma(tmp, 35);
                }
                bw.write(line);

                // wirte simulation output info
                String scvKey = tmp[3] + "," + tmp[20];
                if (sumValMap.containsKey(scvKey)) {
                    bw.write(sumValMap.remove(scvKey)); // P.S. temporal way for multiple treatment
                } else {
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
        }
        bw.close();
    }

    /**
     * Remove the comma in the end of the line and combine to a new String
     * @param strs  input array of string which is splited by comma
     * @param length the expected length of that array
     * @return 
     */
    private String trimComma(String[] strs, int length) {
        StringBuilder sb = new StringBuilder();
        sb.append(strs[0]);
        for (int i = 1; i < strs.length; i++) {
            sb.append(",").append(strs[i]);
        }
        for (int i = strs.length; i < length; i++) {
            sb.append(",");
        }
        return sb.toString();
    }
}
