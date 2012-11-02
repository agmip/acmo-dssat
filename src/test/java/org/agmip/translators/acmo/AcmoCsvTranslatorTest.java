package org.agmip.translators.acmo;

import java.io.File;
import java.io.IOException;
import java.net.URL;
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
public class AcmoCsvTranslatorTest {

    AcmoCsvTranslator obDssatAcmoCsvTanslator;
    URL resource;

    @Before
    public void setUp() throws Exception {
        obDssatAcmoCsvTanslator = new AcmoCsvTranslator();
        resource = this.getClass().getClassLoader().getResource("testCsv.zip");
    }

    @Test
    public void test() throws IOException, Exception {

        obDssatAcmoCsvTanslator.writeCsvFile("", resource.getPath());
        File file = obDssatAcmoCsvTanslator.getOutputFile();
        if (file != null) {
            assertTrue(file.exists());
            assertTrue(file.getName().equals("ACMO.csv"));
//            assertTrue(file.delete());
        }
    }
}
