package org.agmip.translators.acmo;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit test for simple App.
 */
/**
 *
 * @author Meng Zhang
 */
public class AcmoDssatCsvOutputTest {

    AcmoDssatCsvOutput obDssatAcmoCsvTanslator;
    AcmoDssatOutputFileInput obAcmoDssatOutputFileInput;
    URL resource;

    @Before
    public void setUp() throws Exception {
        obDssatAcmoCsvTanslator = new AcmoDssatCsvOutput();
        obAcmoDssatOutputFileInput = new AcmoDssatOutputFileInput();
        resource = this.getClass().getClassLoader().getResource("testCsv.zip");
    }

    @Test
    public void test() throws IOException, Exception {

        HashMap result = obAcmoDssatOutputFileInput.readFile(resource.getPath());
        obDssatAcmoCsvTanslator.writeFile("", result);
        File file = obDssatAcmoCsvTanslator.getOutputFile();
        if (file != null) {
            assertTrue(file.exists());
            assertTrue(file.getName().matches("ACMO( \\(\\d\\))*.csv"));
//            assertTrue(file.delete());
        }
    }
}
