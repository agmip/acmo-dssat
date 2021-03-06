package org.agmip.acmo.translators.dssat.core;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import static org.agmip.util.MapUtil.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DSSAT Output File Data I/O API Class
 *
 * @author Meng Zhang
 * @version 1.0
 */
public class AcmoDssatOutputFileInput extends AcmoCommonInput {

    private static final Logger log = LoggerFactory.getLogger(AcmoDssatOutputFileInput.class);
    public String outDataKey = "data";          // P.S. the key name might change

    /**
     * Constructor with no parameters Set jsonKey as "observed"
     *
     */
    public AcmoDssatOutputFileInput() {
        super();
        jsonKey = "out";
    }

    /**
     * DSSAT Output File Data input method for Controller using
     *
     * @param brMap The holder for BufferReader objects for all files
     * @return result data holder object
     */
    @Override
    protected HashMap readFile(HashMap brMap) throws IOException {

        HashMap ret = new HashMap();
        ret.put("meta", brMap.get("CSV"));
        ret.put("summary", readSummary(brMap));
//        ret.put("overview", readOverview(brMap));
//        ret.put("soilorg", readSoilOrg(brMap));
        return ret;
    }

    /**
     * DSSAT Output File Data input method (Summary.out)
     *
     * @param brMap The holder for BufferReader objects for all files
     * @return result data holder object
     */
    protected HashMap readSummary(HashMap brMap) throws IOException {

        HashMap file = new HashMap();
        String line;
        BufferedReader brOut;
        Object buf;
        LinkedHashMap formats = new LinkedHashMap();
//        String[] titles = new String[0];
        ArrayList<HashMap> sumArr = new ArrayList();

        buf = brMap.get("SUMMARY.OUT");

        // If Output File is no been found
        if (buf == null) {
            return file;
        } else {
            if (buf instanceof char[]) {
                brOut = new BufferedReader(new CharArrayReader((char[]) buf));
            } else {
                brOut = (BufferedReader) buf;
            }
        }

        while ((line = brOut.readLine()) != null) {

            // Get content type of line
            judgeContentType(line);

            // Read summary data
            if (flg[2].equals("data")) {

                // Read meta info
                if (flg[0].equals("meta") && flg[1].equals("meta info")) {

                    // Set variables' formats
                    formats.clear();
                    formats.put("null_1", 11);
                    formats.put("exname", 10);
                    formats.put("local_name", 62);
                    formats.put("null_2", 28);
                    formats.put("version", 28);
                    formats.put("date", line.length());
                    // Read line and save into return holder
                    HashMap tmp = readLine(line, formats);
                    String date = getObjectOr(tmp, "date", "");
                    String version = getObjectOr(tmp, "version", "");
                    if (date.length() > 22) {
                        version += date.substring(0, date.length() - 22).trim();
                        tmp.put("version", version);
                    } else if (date.length() < 22) {
                        version = version.substring(0, version.length() - 22 + date.length()).trim();
                        tmp.put("version", version);
                    }
                    file.putAll(tmp);

                } // Read data info 
                else {
                    // Set variables' formats
//                    formats.clear();
//                    formats.put("runno", 9);
//                    formats.put("trno", 7);
//                    formats.put("r#", 3);
//                    formats.put("o#", 3);
//                    formats.put("c#", 3);
//                    formats.put("cr", 3);
//                    formats.put("model", 9);
//                    formats.put("tnam", 26);
//                    formats.put("fnam", 9);
//                    formats.put("wsta", 9);
//                    formats.put("soil_id", 11);
//                    formats.put("sdat", 8);
//                    formats.put("pdat", 8);
//                    formats.put("edat", 8);
//                    formats.put("adat", 8);
//                    formats.put("mdat", 8);
//                    formats.put("hdat", 8);
//                    formats.put("dwap", 6);
//                    formats.put("cwam", 8);
//                    formats.put("hwam", 8);
//                    formats.put("hwah", 8);
//                    formats.put("bwah", 8);
//                    formats.put("pwam", 6);
//                    formats.put("hwum", 8);
//                    formats.put("h#am", 6);
//                    formats.put("h#um", 8);
//                    formats.put("hiam", 6);
//                    formats.put("laix", 6);
//                    formats.put("ir#m", 6);
//                    formats.put("ircm", 6);
//                    formats.put("prcm", 6);
//                    formats.put("etcm", 6);
//                    formats.put("epcm", 6);
//                    formats.put("escm", 6);
//                    formats.put("rocm", 6);
//                    formats.put("drcm", 6);
//                    formats.put("swxm", 6);
//                    formats.put("ni#m", 6);
//                    formats.put("nicm", 6);
//                    formats.put("nfxm", 6);
//                    formats.put("nucm", 6);
//                    formats.put("nlcm", 6);
//                    formats.put("niam", 6);
//                    formats.put("cnam", 6);
//                    formats.put("gnam", 6);
//                    formats.put("pi#m", 6);
//                    formats.put("picm", 6);
//                    formats.put("pupc", 6);
//                    formats.put("spam", 6);
//                    formats.put("ki#m", 6);
//                    formats.put("kicm", 6);
//                    formats.put("kupc", 6);
//                    formats.put("skam", 6);
//                    formats.put("recm", 6);
//                    formats.put("ontam", 7);
//                    formats.put("onam", 7);
//                    formats.put("optam", 7);
//                    formats.put("opam", 7);
//                    formats.put("octam", 8);
//                    formats.put("ocam", 8);
//                    formats.put("dmppm", 9);
//                    formats.put("dmpem", 9);
//                    formats.put("dmptm", 9);
//                    formats.put("dmpim", 9);
//                    formats.put("yppm", 9);
//                    formats.put("ypem", 9);
//                    formats.put("yptm", 9);
//                    formats.put("ypim", 9);
//                    formats.put("dpnam", 9);
//                    formats.put("dpnum", 9);
//                    formats.put("ypnam", 9);
//                    formats.put("ypnum", 9);
//                    formats.put("ndch", 6);
//                    formats.put("tmaxa", 6);
//                    formats.put("tmina", 6);
//                    formats.put("srada", 6);
//                    formats.put("dayla", 6);
//                    formats.put("co2a", 7);
//                    formats.put("prcp", 7);
//                    formats.put("etcp", 7);
                    // Read line and save into return holder
                    sumArr.add(readLine(line, formats));
                }

            } // Read Summary Info titles
            else if (flg[2].equals("title")) {

                // Set variables' formats based on title line
                formats.clear();
                formats.put("runno", 9);
                formats.put("trno", 7);
                formats.put("r#", 3);
                formats.put("o#", 3);
                formats.put("c#", 3);
                formats.put("cr", 3);
                formats.put("model", 9);
                formats.put("tnam", 26);
                formats.put("fnam", 9);
                formats.put("wsta", 9);
                formats.put("soil_id", 11);
//                line = line.replaceFirst("@", "").trim();
//                titles = line.split("\\.*\\s+");
//                for (String title : titles) {
//                    title = title.toLowerCase();
//                    if (defLength.containsKey(title)) {
//                        formats.put(title, defLength.get(title));
//                    } else {
//                        log.error("Unrecognized variable [{}] detected in title line of Summary.out file", title);
//                        return new HashMap();
//                    }
//                }

                if (line.length() < 93) {
                    log.warn("Detected unregconized format of title in Summary.out file: [{}]", line);
                    line = "";
                } else {
                    line = line.substring(92);
                }
                boolean isTitle = false;
                StringBuilder sb = new StringBuilder();
                int len = 0;
                for (char c : line.toCharArray()) {
                    if (c == ' ') {
                        if (isTitle) {
                            formats.put(sb.toString().toLowerCase(), len);
                            isTitle = false;
                            len = 1;
                        } else {
                            len++;
                        }
                    } else {
                        if (!isTitle) {
                            isTitle = true;
                            sb = new StringBuilder();
                        }
                        if (c != '.') {
                            sb.append(c);
                        }
                        len++;
                    }
                }
                if (isTitle) {
                    formats.put(sb.toString().toLowerCase(), len);
                }

            } else {
            }
        }

        file.put(outDataKey, sumArr);
        brOut.close();

        return file;
    }

    /**
     * DSSAT Output File Data input method (SoilOrg.out)
     *
     * @param brMap The holder for BufferReader objects for all files
     * @return the array of result data holder object
     */
    protected ArrayList<HashMap> readSoilOrg(HashMap brMap) throws IOException {

        ArrayList<HashMap> file = new ArrayList<HashMap>();
        HashMap data = new HashMap();
        ArrayList<HashMap> subArr = new ArrayList<HashMap>();
        String line;
        BufferedReader brOut;
        Object buf;
        LinkedHashMap formats = new LinkedHashMap();

        buf = brMap.get("SOILORG.OUT");

        // If Output File File is no been found
        if (buf == null) {
            return file;
        } else {
            if (buf instanceof char[]) {
                brOut = new BufferedReader(new CharArrayReader((char[]) buf));
            } else {
                brOut = (BufferedReader) buf;
            }
        }

        while ((line = brOut.readLine()) != null) {

            // Get content type of line
            judgeContentType(line);

            // Read soil organic data
            if (flg[2].equals("data")) {

                if (flg[1].equals("meta info")) {
                    if (line.trim().toUpperCase().startsWith("*RUN")) {
                        // Set new data object
                        data = new HashMap();
                        subArr = new ArrayList<HashMap>();
                        data.put(outDataKey, subArr);
                        file.add(data);
                        // Set variables' formats
                        formats.clear();
                        formats.put("null", 4);
                        formats.put("runno", 5);
                        // Get reading result
                        data.putAll(readLine(line, formats));
                    } else if (line.trim().toUpperCase().startsWith("EXPERIMENT")) {
                        // Set variables' formats
                        formats.clear();
                        formats.put("null", 17);
                        formats.put("exp_id", 9);
                        formats.put("cr", 3);
                        // Get reading result
                        data.putAll(readLine(line, formats));
                    } else if (line.trim().toUpperCase().startsWith("TREATMENT")) {
                        // Set variables' formats
                        formats.clear();
                        formats.put("null", 10);
                        formats.put("trno", 3);
                        // Get reading result
                        data.putAll(readLine(line, formats));
                    }
                } else {
                    // Set variables' formats
                    formats.clear();
                    formats.put("year", 5);
                    formats.put("doy", 4);
                    formats.put("das", 6);
                    formats.put("omac", 8);
                    formats.put("scdd", 8);
                    formats.put("socd", 8);
                    formats.put("sc0d", 8);
                    formats.put("sctd", 8);
                    formats.put("somct", 8);
                    formats.put("lctd", 8);
                    formats.put("onac", 8);
                    formats.put("sndd", 8);
                    formats.put("sond", 8);
                    formats.put("sn0d", 8);
                    formats.put("sntd", 8);
                    formats.put("somnt", 8);
                    formats.put("lntd", 8);
                    // Read line and save into return holder
                    subArr.add(readLine(line, formats));
                }
            }
        }

        // Set solic_final and surfc_final
        for (int i = 0; i < file.size(); i++) {
            data = file.get(i);
            subArr = getObjectOr(data, outDataKey, new ArrayList());
            if (!subArr.isEmpty()) {
                data.put("solic_init", getObjectOr(subArr.get(0), "sctd", ""));
                data.put("surfc_init", getObjectOr(subArr.get(0), "sc0d", ""));
                data.put("solic_final", getObjectOr(subArr.get(subArr.size() - 1), "sctd", ""));
                data.put("surfc_final", getObjectOr(subArr.get(subArr.size() - 1), "sc0d", ""));
            }

        }

        brOut.close();
        return file;
    }

    /**
     * DSSAT Output File Data input method (Overview.out)
     *
     * @param brMap The holder for BufferReader objects for all files
     * @return the array of result data holder object
     */
    protected ArrayList<HashMap> readOverview(HashMap brMap) throws IOException {

        ArrayList<HashMap> file = new ArrayList<HashMap>();
        HashMap data = new HashMap();
        ArrayList<HashMap> subArr;
        String line;
        BufferedReader brOut;
        Object buf;
        LinkedHashMap formats = new LinkedHashMap();

        buf = brMap.get("OVERVIEW.OUT");

        // If Output File File is no been found
        if (buf == null) {
            return file;
        } else {
            if (buf instanceof char[]) {
                brOut = new BufferedReader(new CharArrayReader((char[]) buf));
            } else {
                brOut = (BufferedReader) buf;
            }
        }

        while ((line = brOut.readLine()) != null) {

            // Get content type of line
            judgeContentType(line);

            // Read soil organic data
            if (flg[2].equals("data")) {

                if (flg[1].equals("meta info")) {
                    if (line.trim().toUpperCase().startsWith("*RUN")) {
                        // Set new data object
                        data = new HashMap();
                        subArr = new ArrayList<HashMap>();
                        data.put(outDataKey, subArr);
                        file.add(data);
                        // Set variables' formats
                        formats.clear();
                        formats.put("null", 4);
                        formats.put("runno", 5);
                        // Get reading result
                        data.putAll(readLine(line, formats));
                    } else if (line.trim().toUpperCase().startsWith("EXPERIMENT")) {
                        // Set variables' formats
                        formats.clear();
                        formats.put("null", 17);
                        formats.put("exp_id", 9);
                        formats.put("cr", 3);
                        // Get reading result
                        data.putAll(readLine(line, formats));
                    } else if (line.trim().toUpperCase().startsWith("TREATMENT")) {
                        // Set variables' formats
                        formats.clear();
                        formats.put("null", 10);
                        formats.put("trno", 3);
                        // Get reading result
                        data.putAll(readLine(line, formats));
                    }
                }
            }
        }

        brOut.close();
        return file;
    }

    /**
     * Set reading flags for title lines (marked with *)
     *
     * @param line the string of reading line
     */
    @Override
    protected void setTitleFlgs(String line) {
        flg[0] = "meta";
        flg[1] = "meta info";
        flg[2] = "data";
    }
}
