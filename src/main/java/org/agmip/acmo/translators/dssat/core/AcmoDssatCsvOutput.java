package org.agmip.acmo.translators.dssat.core;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import static org.agmip.acmo.translators.dssat.core.AcmoCommonOutput.*;
import org.agmip.acmo.util.AcmoUtil;
import org.agmip.util.MapUtil;
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
    private String metaFilePath = null;
    
    public AcmoDssatCsvOutput() {
    }
    
    public AcmoDssatCsvOutput(String sourceFolder) {
        File dir = new File(sourceFolder);
        if (!dir.isDirectory()) {
            dir = dir.getParentFile();
        }
        this.metaFilePath = dir.getAbsolutePath() + File.separator + "ACMO_meta.dat";
    }

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
    @Override
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
        String version = getObjectOr(sumData, "version", "Ver. N/A");
        StringBuilder sbData;

        // Write CSV File
        outputCsvPath = revisePath(outputCsvPath);
        outputFile = AcmoUtil.createCsvFile(outputCsvPath, "DSSAT", metaFilePath);
        BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
        String line;
        while ((line = brCsv.readLine()) != null) {
            if (line.startsWith("*") || line.startsWith("\"*\"")) {
                break;
            } else {
                bw.write(line);
                bw.write("\r\n");
            }
        }

        // Write data
        int index = 0;
        do {
            // Check if the line is ended with comma
            if (line.endsWith(",") && line.matches(".+,[^,]+,$")) {
                line = line.substring(0, line.length() - 1);
            }
            line = AcmoUtil.addAcmouiVersion(line, MapUtil.getValueOr(data, "acmoVer", ""));
            bw.write(line);

            // wirte simulation output info
            if (index < sumSubArr.size()) {
                sbData = new StringBuilder();
                sumSubData = sumSubArr.get(index);
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
                sbData.append(",\"").append(getObjectOr(sumSubData, "epcp", "")).append("\""); // EPCP
                sbData.append(",\"").append(getObjectOr(sumSubData, "escp", "")).append("\""); // ESCP
                bw.write(sbData.toString());
            }

            bw.write("\r\n");
            index++;
        } while((line = brCsv.readLine()) != null);

        // Check if there is record not been output yet
        if (index != sumSubArr.size()) {
            log.warn("The number of records in Summary.out does not match the metadata.");
        }

        bw.close();
    }
}
