package org.marketsuite.component.comparator;

import java.io.Serializable;
import java.util.Comparator;
import java.util.StringTokenizer;

/**
 * A comparator used in table for sorting IP address type of columns.
 */
public class IpAddressComparator implements Comparator<Object> , Serializable{

	public int compare(Object o1, Object o2) {
        String str1 = o1.toString();
        String str2 = o2.toString();
        IpAddress ip1 = new IpAddress(str1);
        IpAddress ip2 = new IpAddress(str2);

        if (ip1.isNotIpAddress() || ip2.isNotIpAddress()) {
            return str1.compareTo(str2);
        }
        int res = ip1.getOctet1().compareTo(ip2.getOctet1());
            if (res == 0) {
                res = ip1.getOctet2().compareTo(ip2.getOctet2());
                if (res == 0) {
                    res = ip1.getOctet3().compareTo(ip2.getOctet3());
                    if (res == 0) {
                        res = ip1.getOctet4().compareTo(ip2.getOctet4());
                    }
                }
            }
            return res;
    }

    private static class IpAddress {
        public boolean isNotIpAddress() {
            return notIpAddress;
        }

        public Long getOctet1() {
            return l1;
        }

        public Long getOctet2() {
            return l2;
        }

        public Long getOctet3() {
            return l3;
        }

        public Long getOctet4() {
            return l4;
        }

        private boolean notIpAddress;
        private Long l1 = Long.valueOf(0);
        private Long l2 = Long.valueOf(0);
        private Long l3 = Long.valueOf(0);
        private Long l4 = Long.valueOf(0);

        public IpAddress(String ipAddress) {
            try {
                StringTokenizer tokens = new StringTokenizer(ipAddress, ".");
                if (tokens.hasMoreTokens()) {
                    l1 = Long.parseLong(tokens.nextToken().trim());
                } else {
//                notIpAddress = true;
                }
                if (tokens.hasMoreTokens()) {
                    l2 = Long.parseLong(tokens.nextToken().trim());
                } else {
//                isNotIpAddress = true;
                }
                if (tokens.hasMoreTokens()) {
                    l3 = Long.parseLong(tokens.nextToken().trim());
                } else {
//                isNotIpAddress = true;
                }
                if (tokens.hasMoreTokens()) {
                    l4 = Long.parseLong(tokens.nextToken().trim());
                } else {
//                isNotIpAddress = true;
                }
            } catch (NumberFormatException e) {
                notIpAddress = true;
            }
        }
    }
}

