package org.marketsuite.component.table;

/**
 * A data class that holds fill down parameters.
 */
public class FillDownParam {
     /* CTOR: create object using raw string with special types(see literals)
     * @param raw input string
     * @param type see literal
     * @param pos position of octet (only for IP_OCTET, IP_RANGE) 0..3
     */
    public FillDownParam(String raw, int type, int pos) {
        switch(type) {
            case IP_OCTET:
                extractOctet(raw, pos);
                break;

            case TRAILING_NUMBER:
                extractTrailNumber(raw);
                break;

            case IPV4_RANGE:
                extractIPV4Range(raw, pos);
                break;

            case AS_IS:
                baseNumber = 1;
                prefix = raw;
                break;
        }
    }

    //Extract parameters for IP octet type.
    private void extractOctet(String ip, int pos) {
        //calculate index of each . char
        int[] dot_pos = new int[3];
        int dot_idx = 0;
        for (int i=0; i<ip.length(); i++)
            if (ip.charAt(i) == '.')
                dot_pos[dot_idx++] = i;

        switch(pos) {
            case FIRST_OCTET:
                baseNumber = Integer.parseInt(ip.substring(0, dot_pos[0]));
                prefix = "";
                postfix = ip.substring(dot_pos[0], ip.length());
                break;

            case SECOND_OCTET:
                baseNumber = Integer.parseInt(ip.substring(dot_pos[0] + 1, dot_pos[1]));
                prefix = ip.substring(0, dot_pos[0] + 1);
                postfix = ip.substring(dot_pos[1], ip.length());
                break;

            case THIRD_OCTET:
                baseNumber = Integer.parseInt(ip.substring(dot_pos[1] + 1, dot_pos[2]));
                prefix = ip.substring(0, dot_pos[1] + 1);
                postfix = ip.substring(dot_pos[2], ip.length());
                break;

            case FOURTH_OCTET:
                baseNumber = Integer.parseInt(ip.substring(dot_pos[2] + 1, ip.length()));
                prefix = ip.substring(0, dot_pos[2] + 1);
                postfix = "";
                break;
        }
    }

    /**
     * Extract a number that is appended to the end of supplied string for fill down operation.
     * @param str input string with possible pattern
     */
    private void extractTrailNumber(String str) {
        //look for digits at end of string
        StringBuffer buf = new StringBuffer();
        int pos = 0;
        for (pos = str.length() - 1; pos >= 0; pos--) {
            char c = str.charAt(pos);
            if (Character.isDigit(c))
                buf.append(c);
            else
                break;
        }
        buf.reverse();
        if (buf.length() == 0) {//found no digits
            baseNumber = 0;
            prefix = str;
        }
        else {//some trailing digits exist
            try {
                baseNumber = Integer.parseInt(buf.toString());
            }catch(NumberFormatException nfe) {
                baseNumber = 0;
            }

            //find out numbers of leading 0's and keep the position
/*            for (int i=0; i<buf.length(); i++) {
                if (buf.charAt(i) == '0')
                    lead0_count++;
                else
                    break;
            }*/
            }
        prefix = str.substring(0, str.length() - buf.length());// + lead0_count);
    }

    /**
     * Extract start and end IP numbers, prefix and postfix from raw string.
     * Assuming the format follows "IP1 - IP2"
     * If only 1 IP Address is present, then baseNumber, baseNumber2 are the same, midfix is empty
     * @param pos position of octet (only for IP_OCTET, IP_RANGE) 0..3
     */
    private void extractIPV4Range(String raw, int pos) {
        if (raw.indexOf("-") == -1) {//for single address
            extractOctet(raw, pos);
            baseNumber2 = baseNumber;
            return;
        }

        //calculate index of each . char
        trueRange = true;
        int[] dot_pos = new int[6];
        int dash_pos = -1;
        int dot_idx = 0;
        for (int i=0; i<raw.length(); i++) {
            if (raw.charAt(i) == '.')
                dot_pos[dot_idx++] = i;
            else if (raw.charAt(i) == '-')
                dash_pos = i;
        }

        switch(pos) {
            case FIRST_OCTET:
                baseNumber = Integer.parseInt(raw.substring(0, dot_pos[0]));
                baseNumber2 = Integer.parseInt(raw.substring(dash_pos + 1, dot_pos[3]).trim());
                prefix = "";
                midfix = raw.substring(dot_pos[0], dash_pos + 1);//extract space got trimmed
                postfix = raw.substring(dot_pos[3], raw.length());
                break;

            case SECOND_OCTET:
                baseNumber = Integer.parseInt(raw.substring(dot_pos[0] + 1, dot_pos[1]));
                baseNumber2 = Integer.parseInt(raw.substring(dot_pos[3] + 1, dot_pos[4]));
                prefix = raw.substring(0, dot_pos[0] + 1);
                midfix = raw.substring(dot_pos[1], dot_pos[3] + 1);
                postfix = raw.substring(dot_pos[4], raw.length());
                break;

            case THIRD_OCTET:
                baseNumber = Integer.parseInt(raw.substring(dot_pos[1] + 1, dot_pos[2]));
                baseNumber2 = Integer.parseInt(raw.substring(dot_pos[4] + 1, dot_pos[5]));
                prefix = raw.substring(0, dot_pos[1] + 1);
                midfix = raw.substring(dot_pos[2], dot_pos[4] + 1);
                postfix = raw.substring(dot_pos[5], raw.length());
                break;

            case FOURTH_OCTET:
                baseNumber = Integer.parseInt(raw.substring(dot_pos[2] + 1, dash_pos).trim());
                baseNumber2 = Integer.parseInt(raw.substring(dot_pos[5] + 1, raw.length()));
                prefix = raw.substring(0, dot_pos[2] + 1);
                midfix = raw.substring(dash_pos, dot_pos[5] + 1);
                postfix = "";
                break;
        }
    }

    public int getBaseNumber() {
        return baseNumber;
    }

    public int getBaseNumber2() {
        return baseNumber2;
    }

    public String getPrefix() {
        return (prefix == null) ? "" : prefix;
    }

    public String getMidfix() {
        return (midfix == null) ? "" : midfix;
    }

    public String getPostfix() {
        return (postfix == null) ? "" : postfix;
    }

    public boolean isTrueRange() {
        return trueRange;
    }

    //instance variables
    private int baseNumber = -1;
    private int baseNumber2 = -1;//only used in IP_RANGE
    private String prefix = "";
    private String midfix = "";//only used in IPV4_RANGE
    private String postfix = "";
    private boolean trueRange;//for IPV4_RANGE and MAC range

    //literals
    //types of fill down parameters
    public static final int TRAILING_NUMBER = 1000;//extract trailing number from a string
    public static final int IP_OCTET = 1100;//octets in IP address
    public static final int IPV4_RANGE = 1200;//range of IPV4 addresses
    public static final int MAC_BYTE = 1300;//MAC address bytes
    public static final int MAC_RANGE = 1400;//range of MAC addresses
    public static final int AS_IS = 1500; //Use the entire string as-is

    public static final int FIRST_OCTET = 0;//most significant
    public static final int SECOND_OCTET = 1;
    public static final int THIRD_OCTET = 2;
    public static final int FOURTH_OCTET = 3;
}