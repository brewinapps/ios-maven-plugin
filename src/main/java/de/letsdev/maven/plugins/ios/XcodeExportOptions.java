/**
 * maven-ios-plugin
 * <p>
 * User: fkoebel
 * Date: 2017-05-07
 * <p>
 * This code is copyright (c) 2017 let's dev.
 * URL: http://www.letsdev.de
 * e-Mail: contact@letsdev.de
 */

package de.letsdev.maven.plugins.ios;

public class XcodeExportOptions {
    /**
     * defining method parameter for export options plist
     * available values: app-store, ad-hoc, enterprise, development
     *
     * @parameter property="ios.xcodeExportOptions.method"
     * default-value="enterprise"
     */
    public String method;

    /**
     * defining compileBitcode parameter for export options plist
     *
     * @parameter property="ios.xcodeExportOptions.compileBitcode"
     * default-value="false"
     */
    public boolean compileBitcode;

    /**
     * defining uploadBitcode parameter for export options plist
     *
     * @parameter property="ios.xcodeExportOptions.uploadBitcode"
     * default-value="false"
     */
    public boolean uploadBitcode;

    /**
     * defining uploadSymbols parameter for export options plist
     *
     * @parameter property="ios.xcodeExportOptions.uploadSymbols"
     * default-value="true"
     */
    public boolean uploadSymbols;

    /**
     * defining thinning parameter for export options plist
     * available values: <none>, <thin-for-all-variants>, or device specifier like iPad3,1 or iPhone5,1
     *
     * @parameter property="ios.xcodeExportOptions.thinning"
     * default-value="<none>"
     */
    public String thinning;
}
