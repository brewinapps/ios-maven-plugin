package de.letsdev.maven.plugins.ios;

/**
 * Maven iOS Plugin
 *
 * User: cwack
 * Date: 09.10.2012
 * Time: 19:54:44
 *
 * This code is copyright (c) 2012 let's dev.
 * URL: http://www.letsdev.de
 * e-Mail: contact@letsdev.de
 */

public class Utils {

    public enum PLUGIN_PROPERTIES {

        APPNAME("appName"),
        CODE_SIGN_IDENTITY("codeSignIdentity"),
        SDK("sdk"),
        SOURCE_DIR("sourceDir"),
        TARGET_DIR("targetDir"),
        CONFIGURATION("configuration"),
        BUILD_ID("buildId"),
        SCHEME("scheme"),
        TARGET("target"),
        KEYCHAIN_PATH("keychainPath"),
        KEYCHAIN_PASSWORD("keychainPassword");

        private PLUGIN_PROPERTIES(String name) {
            this.name = name;
        }

        private final String name;

        public String toString() {
            return name;
        }
    }
}
