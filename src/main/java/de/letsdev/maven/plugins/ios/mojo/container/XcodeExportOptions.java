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

package de.letsdev.maven.plugins.ios.mojo.container;

import java.util.Map;

public class XcodeExportOptions {
    /**
     * defining compileBitcode parameter for export options plist
     * For non-App Store exports, should Xcode re-compile the app from bitcode?
     *
     * @parameter property="ios.xcodeExportOptions.compileBitcode"
     * default-value="false"
     */
    public boolean compileBitcode;
    
    /**
     * defining embedOnDemandResourcesAssetPacksInBundle parameter for export options plist
     * For non-App Store exports, if the app uses On Demand Resources and this is YES, asset packs are embedded in the app bundle so that the app can be tested without a server to host asset packs.
     * Defaults to YES unless onDemandResourcesAssetPacksBaseURL is specified.
     *
     * @parameter property="ios.xcodeExportOptions.embedOnDemandResourcesAssetPacksInBundle"
     * default-value="true"
     */
    public boolean embedOnDemandResourcesAssetPacksInBundle;

    /**
     * defining iCloudContainerEnvironment parameter for export options plist
     * If the app is using CloudKit, this configures the "com.apple.developer.icloud-container-environment" entitlement. Available options vary depending on the type of provisioning profile used, but may include: Development and Production.
     *
     * @parameter property="ios.xcodeExportOptions.iCloudContainerEnvironment"
     * default-value=""
     */
    public String iCloudContainerEnvironment;

    /**
     * defining installerSigningCertificate parameter for export options plist
     * For manual signing only. Provide a certificate name, SHA-1 hash, or automatic selector to use for signing. Automatic selectors allow Xcode to pick the newest installed certificate of a particular type. The available automatic selectors are "Mac Installer Distribution" and "Developer ID Installer".
     * Defaults to an automatic certificate selector matching the current distribution method.
     *
     * @parameter property="ios.xcodeExportOptions.installerSigningCertificate"
     * default-value=""
     */
    public String installerSigningCertificate;

    /**
     * defining manifest parameter for export options plist
     * For non-App Store exports, users can download your app over the web by opening your distribution manifest file in a web browser.
     * To generate a distribution manifest, the value of this key should be a dictionary with three sub-keys: appURL, displayImageURL, fullSizeImageURL. The additional sub-key assetPackManifestURL is required when using on-demand resources.
     *
     * @parameter property="ios.xcodeExportOptions.manifest"
     * default-value=""
     */
    public XcodeExportOptionsManifest manifest;

    /**
     * defining method parameter for export options plist
     * Describes how Xcode should export the archive. Available options: app-store, ad-hoc, package, enterprise, development, developer-id, and mac-application. The list of options varies based on the type of archive. Defaults to development.
     *
     * @parameter property="ios.xcodeExportOptions.method"
     * default-value="enterprise"
     */
    public String method;

    /**
     * defining onDemandResourcesAssetPacksBaseURL parameter for export options plist
     * For non-App Store exports, if the app uses On Demand Resources and embedOnDemandResourcesAssetPacksInBundle isn't YES, this should be a base URL specifying where asset packs are going to be hosted. This configures the app to download asset packs from the specified URL.
     *
     * @parameter property="ios.xcodeExportOptions.onDemandResourcesAssetPacksBaseURL"
     * default-value=""
     */
    public String onDemandResourcesAssetPacksBaseURL;

    /**
     * defining provisioningProfiles parameter for export options plist
     * For manual signing only. Specify the provisioning profile to use for each executable in your app. Keys in this dictionary are the bundle identifiers of executables; values are the provisioning profile name or UUID to use.
     *
     * @parameter property="ios.xcodeExportOptions.provisioningProfiles"
     * default-value=""
     */
    public Map<String, String> provisioningProfiles;

    /**
     * defining signingCertificate parameter for export options plist
     * For manual signing only. Provide a certificate name, SHA-1 hash, or automatic selector to use for signing. Automatic selectors allow Xcode to pick the newest installed certificate of a particular type.
     * The available automatic selectors are "Mac App Distribution", "iOS Distribution", "iOS Developer", "Developer ID Application", and "Mac Developer". Defaults to an automatic certificate selector matching the current distribution method.
     *
     * @parameter property="ios.xcodeExportOptions.signingCertificate"
     * default-value=""
     */
    public String signingCertificate;

    /**
     * defining signingStyle parameter for export options plist
     * The signing style to use when re-signing the app for distribution. Options are manual or automatic. Apps that were automatically signed when archived can be signed manually or automatically during distribution, and default to automatic.
     * Apps that were manually signed when archived must be manually signed during distribtion, so the value of signingStyle is ignored.
     *
     * @parameter property="ios.xcodeExportOptions.signingStyle"
     * default-value=""
     */
    public String signingStyle;

    /**
     * defining stripSwiftSymbols parameter for export options plist
     * Should symbols be stripped from Swift libraries in your IPA? Defaults to YES.
     *
     * @parameter property="ios.xcodeExportOptions.stripSwiftSymbols"
     * default-value="true"
     */
    public boolean stripSwiftSymbols;

    /**
     * defining teamID parameter for export options plist
     * The Developer Portal team to use for this export. Defaults to the team used to build the archive.
     *
     * @parameter property="ios.xcodeExportOptions.teamID"
     * default-value=""
     */
    public String teamID;

    /**
     * defining thinning parameter for export options plist
     * For non-App Store exports, should Xcode thin the package for one or more device variants? Available options: <none> (Xcode produces a non-thinned universal app), <thin-for-all-variants> (Xcode produces a universal app and all available thinned variants), or a model identifier for a specific device (e.g. "iPhone7,1").
     * Defaults to <none>.
     *
     * @parameter property="ios.xcodeExportOptions.thinning"
     * default-value="<none>"
     */
    public String thinning;

    /**
     * defining uploadBitcode parameter for export options plist
     * For App Store exports, should the package include bitcode? Defaults to YES.
     *
     * @parameter property="ios.xcodeExportOptions.uploadBitcode"
     * default-value="false"
     */
    public boolean uploadBitcode;

    /**
     * defining uploadSymbols parameter for export options plist
     * For App Store exports, should the package include symbols? Defaults to YES.
     *
     * @parameter property="ios.xcodeExportOptions.uploadSymbols"
     * default-value="true"
     */
    public boolean uploadSymbols;

}
