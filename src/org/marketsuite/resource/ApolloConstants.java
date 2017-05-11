package org.marketsuite.resource;

import java.util.Locale;
import java.util.ResourceBundle;

public class ApolloConstants {
    public final static ResourceBundle APOLLO_BUNDLE =
       ResourceBundle.getBundle("org.marketsuite.resource.ApolloBundle", Locale.ENGLISH);
    public static final String[] MARKETS = {
        "Broad / Regional Market",
        "Country",
        "U.S. Sector",
        "Industry Group",
        "Commodity",
        "Currency"
    };
}
