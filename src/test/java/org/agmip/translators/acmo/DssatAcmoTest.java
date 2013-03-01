package org.agmip.translators.acmo;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.agmip.acmo.translators.dssat.DssatAcmo;
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
public class DssatAcmoTest {

    DssatAcmo runner;
    URL resource;

    @Before
    public void setUp() throws Exception {
        runner = new DssatAcmo();
        resource = this.getClass().getClassLoader().getResource("testCsv.zip");
    }

    @Test
    public void test() throws IOException, Exception {
        File file = runner.execute(resource.getPath(), "");
        if (file != null) {
            assertTrue(file.exists());
            assertTrue(file.getName().matches("ACMO_DSSAT( \\(\\d\\))*.csv"));
            assertTrue(file.delete());
        }
    }
}
