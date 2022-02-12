package com.booleanbyte.wpplugin.worleycaves;

import java.io.IOException;
import java.util.Properties;


public class Version {

    public static final String VERSION;

    private Version() {
        // Prevent instantiation?
    }

    static {
        Properties versionProps = new Properties();

        try {
            versionProps.load(Version.class.getResourceAsStream("/com.booleanbyte.wpplugin.worleycaves.properties"));
            VERSION = versionProps.getProperty("com.booleanbyte.wpplugin.worleycaves.version");
        } catch (IOException e) {
            throw new RuntimeException("I/O error loading version number from classpath", e);
        }
    }
}
