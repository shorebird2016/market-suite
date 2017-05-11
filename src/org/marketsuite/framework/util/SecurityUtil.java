package org.marketsuite.framework.util;

import org.marketsuite.framework.resource.FrameworkConstants;
import org.jasypt.util.text.BasicTextEncryptor;
import org.marketsuite.framework.resource.FrameworkConstants;

import java.io.*;
import java.net.Inet4Address;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Calendar;

public class SecurityUtil {
    /**
     * check user license: all conditions must meet
     * (1) c:/database/-log existence
     * (2) -log creation time stamp older than 3 month from today
     * (3) -log not empty
     * (4) -lic existence and empty
     * (5) -lic last modified time stamp = first line(time stamp) in -log
     * (6) -lic last modified time stamp older than 3 month from today
     * @return true if pass
     */
    public static boolean validateLicense() {
        //look for file "-log" under c:/database
        BufferedReader br = null;
        try {
            //if can't open or read log file, no good
            br = new BufferedReader(new FileReader(FrameworkConstants.DATA_FOLDER + File.separator + LOG_FILE));
            String line = br.readLine();

            //empty file, no good
            if (line == null || line.equals(""))
                return false;

            //~lic must exist
            br = new BufferedReader(new FileReader(FrameworkConstants.DATA_FOLDER + File.separator + LICENSE_FILE));

            //~lic must have zero length (empty)
            File lic_file = new File(FrameworkConstants.DATA_FOLDER + File.separator + LICENSE_FILE);
            if (lic_file.length() == 0)//must not be empty
                return false;

            //check old license file: last modified time of -lic not more than 2 months ago
            long exist_time = lic_file.lastModified();
            long now = Calendar.getInstance().getTimeInMillis();
            long ms_day = 24 * 60 * 60 * 1000;
            if ( (now - exist_time) > (ms_day * 60.0) )
                return false;

//todo check first line of -log matches last modified date of -lic

        } catch (IOException ioe) {
            //some files don't exist
            return false;
        } finally {
            if (br != null)
                try {
                    br.close();
                } catch (IOException e) {
                    return false;//can't close file
                }
        }
        return true;
    }

    /**
     * Update log file and refresh time stamp on license file, called when app closes.
     */
    public static void updateUsage() {
        try {
            //add date to log file
            FileWriter fw = new FileWriter(FrameworkConstants.DATA_FOLDER + File.separator + LOG_FILE, true);
            BufferedWriter bw = new BufferedWriter(fw);
            String log_line = FrameworkConstants.YAHOO_DATE_FORMAT.format(Calendar.getInstance().getTime()) + "\n";
            bw.write(log_line.toCharArray());
            bw.close();

            //refresh license file time stamp
            File license_file = new File(FrameworkConstants.DATA_FOLDER + File.separator + LICENSE_FILE);
            license_file.setLastModified(System.currentTimeMillis());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Encode MAC address of this computer into ~lic file
     */
    public static void encryptMacAddrIntoLicence() throws IOException {
        NetworkInterface network = NetworkInterface.getByInetAddress(Inet4Address.getLocalHost());
        byte[] mac = network.getHardwareAddress();
        StringBuilder buf = new StringBuilder();
        for (int i=0; i<mac.length; i++)
            buf.append(String.format("%02X%s", mac[i], (i<mac.length-1) ? ":" : ""));

        //encrypt the string
        BasicTextEncryptor ecp = new BasicTextEncryptor();  ecp.setPassword(PASSWORD);
        String ecpd = ecp.encrypt(buf.toString());

        //write to ~lic file
        FileWriter fw = new FileWriter(FrameworkConstants.DATA_FOLDER + File.separator + LICENSE_FILE, false);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(ecpd);
        bw.close();
    }

    public static boolean verifyMacAddr() throws IOException {
        NetworkInterface network = NetworkInterface.getByInetAddress(Inet4Address.getLocalHost());
        byte[] mac = network.getHardwareAddress();
        StringBuilder buf = new StringBuilder();
        for (int i=0; i<mac.length; i++)
            buf.append(String.format("%02X%s", mac[i], (i<mac.length-1) ? ":" : ""));

        //read ~lic
        BufferedReader br = new BufferedReader(new FileReader(FrameworkConstants.DATA_FOLDER + File.separator + LICENSE_FILE));
        String line = br.readLine();

        //empty file, no good
        if (line == null || line.equals(""))
            return false;

        //decrypt the string
        BasicTextEncryptor ecp = new BasicTextEncryptor();  ecp.setPassword(PASSWORD);
        String dcp = ecp.decrypt(line);
        return dcp.equals(buf.toString());
    }

    //----- literal -----
    private static String PASSWORD = "CLEC";
    private static String LOG_FILE = "~log";
    private static String LICENSE_FILE = "~lic";

    //license generation
    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("-pc")) //run on PC, MAC is default
            FrameworkConstants.DATA_FOLDER = FrameworkConstants.DATA_FOLDER_PC;//default MAC
        try {
            encryptMacAddrIntoLicence();
            if (verifyMacAddr())
                System.err.println("...License Generation Successful...");
            else
                System.err.println("...License Generation Failed...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
