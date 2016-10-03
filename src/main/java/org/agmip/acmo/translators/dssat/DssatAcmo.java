package org.agmip.acmo.translators.dssat;

import java.io.File;
import java.util.HashMap;
import org.agmip.acmo.translators.AcmoTranslator;
import org.agmip.acmo.translators.AcmoVersionRecordable;
import org.agmip.acmo.translators.dssat.core.AcmoDssatCsvOutput;
import org.agmip.acmo.translators.dssat.core.AcmoDssatOutputFileInput;
import org.agmip.common.Functions;
//import static org.agmip.common.Functions.printStackTrace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ACMO CSV translator for DSSAT output data
 *
 * @author Meng Zhang
 */
public class DssatAcmo implements AcmoTranslator, AcmoVersionRecordable {

    private static final Logger LOG = LoggerFactory.getLogger(DssatAcmo.class);
    private String acmoVer = "";

    /**
     * Output ACMO csv File with DSSAT output data
     *
     * @param sourceFolder
     * @param destFolder
     * @return The output file
     */
    @Override
    public File execute(String sourceFolder, String destFolder) {
        // Read DSSAT output data
        AcmoDssatOutputFileInput dssatReader = new AcmoDssatOutputFileInput();
        HashMap result = dssatReader.readFile(sourceFolder);
        result.put("acmoVer", acmoVer);
        try {
            // Output CSV File
            AcmoDssatCsvOutput csvWriter = new AcmoDssatCsvOutput(sourceFolder);
            csvWriter.writeFile(destFolder, result);
            return csvWriter.getOutputFile();
        } catch (Exception e) {
            LOG.error(Functions.getStackTrace(e));
            return null;
        }
    }

    @Override
    public void recordAcmoVersion(String acmoVer) {
        this.acmoVer = acmoVer;
    }
}
