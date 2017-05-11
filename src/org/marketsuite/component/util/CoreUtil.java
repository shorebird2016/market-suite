package org.marketsuite.component.util;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFrame;

import org.marketsuite.component.dialog.MultiResultDialog;
import org.marketsuite.component.resource.LazyIcon;
import org.marketsuite.component.dialog.MultiResultDialog;

//Collection of utilities.
public class CoreUtil {
    //---------- measuring execution time --------
    private static long appStartTime;
    public static void setAppStartTime() { appStartTime = System.currentTimeMillis(); }
    public static void showTimeFromAppStart(String msg) {
        long end = System.currentTimeMillis();
        System.out.println(msg + "[" + (end - appStartTime) + " ms] from App Startup" /*+ "\t" + end*/);
    }
    private static long deltaTime;
    public static void setDeltaTimeStart(String start_msg) {
        deltaTime = System.currentTimeMillis();
        if (start_msg.isEmpty()) return;//don't print if empty
        System.out.print(start_msg/* + "\t" + deltaTime*/);
    }
    public static void showDeltaTime(String msg) {
        long end = System.currentTimeMillis();
        System.out.println(msg + "\t[" + (end - deltaTime) + " ms]"/* + "\t" + end*/);
    }
    public static long setDeltaTimeFinish() {
        long end = System.currentTimeMillis();
        return end - deltaTime;
    }

    public static long ipv4ToNumber(String ip) {
        String splitIP[] = ip.split("\\.");
        if (splitIP.length == 4) {
            long result = Integer.parseInt(splitIP[0].trim());
            result = result << 8;
            result |= Integer.parseInt(splitIP[1].trim());
            result = result << 8;
            result |= Integer.parseInt(splitIP[2].trim());
            result = result << 8;
            result |= Integer.parseInt(splitIP[3].trim());
            return result;
        }
        return 0;
    }

    /**
     * Convert MAC address(in XX:XX:XX:XX:XX:XX format) into an array of bytes.
     * Assumption: must conform to format
     *
     * @param mac_addr to be converted
     * @return array of integers, one for each hex digit
     */
    public static int[] macAddressToBytes(String mac_addr) {
        int[] ret = new int[6];
        int index = 0;
        for (int i = 0; i < ret.length; i++) {
            byte[] bytes = mac_addr.substring(index, index + 2).getBytes();
            index += 3;
            ret[i] = hexToNumber(bytes);
        }
        return ret;
    }

    /**
     * Covert a hex byte into a number.
     *
     * @param hex byte to be converted
     * @return number between 0 to 15, or -1 if bad format
     */
    public static int hexToDigit(byte hex) {
        if (hex <= 0x39 && hex >= 0x30) //0..9
            return hex - 0x30;
        else if (hex >= 0x41 && hex <= 0x46) //upper case A-F
            return hex - 55;
        else if (hex >= 0x61 && hex <= 0x66) //lower case a-f
            return hex - 87;
        else
            return -1;
    }

    /**
     * Helper to convert up to 2 hex bytes into number. MSB first
     *
     * @param hex array of bytes as input
     * @return -1 if input has no bytes
     */
    public static int hexToNumber(byte[] hex) {
        int num;
        switch (hex.length) {
            case 0:
            default:
                return -1;

            case 1://single digit
                num = hexToDigit(hex[0]);
                break;

            case 2://2 digits, MSB first
                num = 16 * hexToDigit(hex[0]) + hexToDigit(hex[1]);
                break;
        }
        return num;
    }

    //Covert 6 hex bytes into a number using binary arithmetic.
    public static long macBytesToNumber(int[] bytes) {
        long ret = 0;
        int power = 5;//most significant hex
        for (int i = 0; i < bytes.length; i++)
            ret += bytes[i] * Math.pow(256, power--);
        return ret;
    }

    public static String boolToString(boolean flag) {
        return flag ? "true" : "false";
    }

    public static boolean stringToBool(String str) {
        return str.equals("true");
    }

    //IPV4 related utilities
    //Check validity of an IPv4 address per PFS, (1)0.0.0.0 (2)127.x.x.x
    // (3) valid till 223.255.255.255 (4)empty or null or x.x.x, x.x..etc
    public static boolean isValidIPv4Address(String ip) {
        if (ip == null || ip.equals(""))
            return false;

        //check less than 4 octets
        StringTokenizer stk = new StringTokenizer(ip, ".");
        if (stk.countTokens() != 4)
            return false;

        //check all zeros and ranges
        int[] octets = splitOctets(ip);
        boolean all_zero = true;
        for (int i = 0; i < octets.length; i++) {
            if (octets[i] != 0)
                all_zero = false;
            if (octets[i] < 0 || octets[i] > 255)//any one out of range?
                return false;
        }
        if (all_zero)
            return false;

        //check leading octet to be 127 and smaller than 224 (per PFS)
        return octets[0] != 127 && octets[0] < 224;
    }

    public static boolean isValidUnicastAddress(String ip) {
        if (ip == null || ip.equals(""))
            return false;

        //check less than 4 octets
        StringTokenizer stk = new StringTokenizer(ip, ".");
        if (stk.countTokens() != 4)
            return false;

        int[] octets = splitOctets(ip);
        return (octets[0] < 224);
    }

    public static boolean isValidMulticastAddress(String ip) {
        if (ip == null || ip.equals(""))
            return false;

        //check less than 4 octets
        StringTokenizer stk = new StringTokenizer(ip, ".");
        if (stk.countTokens() != 4)
            return false;

        int[] octets = splitOctets(ip);
        return (octets[0] < 240 && octets[0] >= 224);
    }

    /* check special IPv4 addresses:
       |255.255.255.255 | Limited Broadcast address.  Datagram with this  |
       |                | address will be received and processed by all   |
       |                | the hosts in the local network.  This datagram  |
       |                | is not forwarded to other networks by routers.  |
       |xxx.255.255.255 | Directed broadcast address.  The datagram with  |
       |xxx.xxx.255.255 | this IP address is received by all the hosts in |
       |xxx.xxx.xxx.255 | the specified network.  The x  represents the   |
       |                | network ID bits.                                |
    */
    public static boolean isBroadcastAddress(String ip) {
        if (!isValidIPv4Address(ip))
            return false;

        int[] octets = splitOctets(ip);
        if (octets[0] == 255 && octets[1] == 255 && octets[2] == 255 && octets[3] == 255)
            return true;
        if (octets[1] == 255 && octets[2] == 255 && octets[3] == 255)
            return true;
        if (octets[2] == 255 && octets[3] == 255)
            return true;
        if (octets[3] == 255)
            return true;
        return false;
    }

    /**
     * Retrieve a list of octets from IP address.
     *
     * @param ip IP Address in question
     * @return null for bad input
     */
    public static int[] splitOctets(String ip) {
        String splitIP[] = ip.split("\\.");
        int[] ret = new int[4];

        try {
            ret[0] = Integer.parseInt(splitIP[0]);
            ret[1] = Integer.parseInt(splitIP[1]);
            ret[2] = Integer.parseInt(splitIP[2]);
            ret[3] = Integer.parseInt(splitIP[3]);
        } catch (Exception nfe) {
            ret[0] = ret[1] = ret[2] = ret[3] = -1;
        }
        return ret;
    }

    /**
     * Remove all leading 0s from an IPV4 address.
     *
     * @param ip a valid IPV4 address with 4 octets
     * @return IP address w/o leading 0s
     */
    public static String stripLeading0sFromIPV4(String ip) {
        //split into 4 strings, remove leading 0s and combine
        StringBuffer val = new StringBuffer();
        StringTokenizer stk = new StringTokenizer(ip, ".");
        while (stk.hasMoreElements()) {
            String octet = stk.nextToken();
            val.append(removeLeading0s(octet)).append(".");
        }
        val.setLength(val.length() - 1);//remove last .
        return val.toString();
    }

    //MAC address related utilities
    public static boolean isValidMacAddress(String address) {
        String regularExpressionForMACAddress = "[0-9A-Fa-f][0-9A-Fa-f][:][0-9A-Fa-f][0-9A-Fa-f][:][0-9A-Fa-f][0-9A-Fa-f]" +
                "[:][0-9A-Fa-f][0-9A-Fa-f][:][0-9A-Fa-f][0-9A-Fa-f][:][0-9A-Fa-f][0-9A-Fa-f]";
        Matcher matcher = Pattern.compile(regularExpressionForMACAddress).matcher(address);
        return matcher.matches();
    }

    /**
     * To validate MAC address with special considerations to rule out special addresses
     * following these rules (documented in http://www.iana.org/assignments/ethernet-numbers)
     * 00:00:00:00:00:00 ff:ff:ff:ff:ff:ff xy:xx:xx:xx:xx:xx
     * Where y is one of 1,3,5,7,9,b,d,f
     * Assumption: mac_address already has valid XX:XX:XX:XX:XX:XX
     *
     * @param mac_addr address to validate
     * @return true = special address
     */
    public static boolean isSpecialMacAddress(String mac_addr) {
        int[] octets = macAddressToBytes(mac_addr);
        //check all 00 and FF's
        if (octets[0] == 0 && octets[1] == 0 && octets[2] == 0 &&
                octets[3] == 0 && octets[4] == 0 && octets[5] == 0)
            return true;

        if (octets[0] == 0xFF && octets[1] == 0xFF && octets[2] == 0xFF &&
                octets[3] == 0xFF && octets[4] == 0xFF && octets[5] == 0xFF)
            return true;

        //check first number to be not even
        return (octets[0] % 2 != 0);
    }

    //Remove leading 0's of a given string. Single 0 or null is omitted.
    public static String removeLeading0s(String inp) {
        if (inp.length() <= 1)
            return inp;

        int index = 0;
        while (index < inp.length()) {
            char c = inp.charAt(index);
            if (c != '0') {
                return inp.substring(index, inp.length());
            }
            index++;
        }
        //all 0s
        return "0";
    }

    public static String convertGMTtoLocal(String gmt) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_STRING);
        Date date = sdf.parse(gmt);
        long utc_ms = date.getTime();
        Calendar cal = Calendar.getInstance();
        long offset = cal.getTimeZone().getOffset(cal.getTimeInMillis());//already adjust for day light saving
        long num = utc_ms + offset;
        return sdf.format(new Date(num));
    }


    /**
     * Convert carriage return / line feed inside string into <br> for HTML display.  Also add <br> to wrap
     * text formatted in html.
     *
     * @param text1    source to be converted
     * @param wrap_len number of characters to start wrapping
     * @return converted text
     */
    public static String wrapText(String text1, int wrap_len) {
        if (text1 == null)
            return "";

        //substitute \n to blanks
        String text = text1.replaceAll("\n", "<br>&nbsp;&nbsp;");

        //add blanks after delimiters
        char[] src = text.toCharArray();
        char[] delimiter = {' ', ',', /*'.',*/ ';'};
        //        src = text.toCharArray();
        StringBuffer out = new StringBuffer();
        int idx = 0;
        int start = 0;
        int count = 0;
        while (idx < src.length) {
            char ch = src[idx];
            count++;
            for (int i = 0; i < delimiter.length; i++) {
                if (ch == delimiter[i]) { //append blank
                    out.append(new String(src, start, count) + " ");
                    start = idx + 1;
                    count = 0;
                }
            }
            idx++;
        }
        out.append(new String(src, start, count));
        String new_text = out.toString();
        out = new StringBuffer();
        out.append("<html>");
        //        int line_num = 0;
        //split into list of words
        StringTokenizer stk = new StringTokenizer(new_text, " "); //list of words
        StringBuffer line_buf = new StringBuffer();
        while (stk.hasMoreElements()) {
            String word = (String) stk.nextElement() + " ";
            line_buf.append(word);
            if (line_buf.length() >= wrap_len) { //copy group of words
                out.append(line_buf + "<br>&nbsp;&nbsp;");
                line_buf = new StringBuffer();
            }
        }
        if (line_buf.length() > 0)
            out.append(line_buf);
        out.append("</html>");
        return out.toString();
    }

    public static String getShortNameFromClasspath(String origStr) {
        if (origStr == null)
            return "";
        int pos = origStr.lastIndexOf('.');
        if (pos < 0)
            return "";
        //		Debug.println( "origStr = " + origStr + "   pos: "+pos);
        return origStr.substring(pos + 1);
    }

    public static String getPathOnly(String origStr, boolean bFromClasspath) {
        String name = "";
        String sepa = bFromClasspath ? "." : "/";
        StringTokenizer st = new StringTokenizer(origStr, sepa);

        while (st.hasMoreTokens()) {
            name = st.nextToken();
        }
        return origStr.substring(0, origStr.length() - name.length());
    }

    /**
     * This method returns a string from the resource resBundle.
     */
    public static String getString(ResourceBundle resBundle, String key) {
        String value = null;
        try {
            value = resBundle.getString(key);
        } catch (MissingResourceException e) {
            System.out.println("java.util.MissingResourceException: Couldn't find value for: " + key);
        }
        if (value == null) {
            value = "Could not find resource: " + key + "  ";
        }
        return value;
    }

    //helper to display single exception with hyperlink to show detail
    // info - shows up on first column, title - dialog title
    public static void showSingleResult(JFrame parent, String info, String title, Exception exception) {
        ArrayList<String> info_string = new ArrayList<String>();
        info_string.add(info);
        ArrayList<Exception> msg = new ArrayList<Exception>();
        msg.add(exception);
        new MultiResultDialog(parent, LazyIcon.APP_ICON.getImage(), title, info_string, msg);
    }

    private static final String FORMAT_STRING = "yyyy/MM/dd HH:mm:ss";

    /**
     * Simple method to quickly read a text file into a string.
     *
     * @param file source object
     * @return string of the file
     * @throws java.io.IOException fail to read
     */
    public static String readTextFile(File file) throws IOException {
        StringBuilder ret = new StringBuilder();
        Scanner scanner = new Scanner(file, "UTF-8");
        try {
            while (scanner.hasNextLine())
                ret.append(scanner.nextLine()).append(NEW_LINE);
        } finally {
            scanner.close();
        }
        return ret.toString();
    }

    public static void writeTextFile(File file, String text) throws IOException {
        Writer out = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
        try {
            out.write(text);
        } finally {
            out.close();
        }
    }

    private static final String NEW_LINE = System.getProperty("line.separator");

    //reverse an Integer array
    public static ArrayList<Integer> reverse(ArrayList<Integer> source) {
        ArrayList<Integer> ret = new ArrayList<>();
        int size = source.size();
        for (int i = 0; i < size; i++)
            ret.add(source.get(size - i - 1));
        return ret;
    }
}