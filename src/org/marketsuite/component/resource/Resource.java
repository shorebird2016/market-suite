package org.marketsuite.component.resource;

import java.util.Locale;

public class Resource {
    public static Resource getInstance() {
        if (_Instance == null)
            _Instance = new Resource();
        return _Instance;
    }
    private static Resource _Instance;

    public final static Locale DEFAULT_LOCALE = Locale.ENGLISH;
}
