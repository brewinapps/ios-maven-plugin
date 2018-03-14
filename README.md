# maven-ios-plugin

[![Build Status](https://secure.travis-ci.org/letsdev/maven-ios-plugin.png)](http://travis-ci.org/letsdev/maven-ios-plugin)

The maven-ios-plugin plugs in to the Maven build lifecycle to automate compilation and deployment of iOS applications. This enables continuous integration for the iOS platform with ease.

This is a plugin provided by let's dev GmbH & Co.KG

http://www.letsdev.de - professional mobile solutions

## Last-Changes

2018-03-14 - Release version 1.17.0: Supporting target dependencies for framework builds<br />
2018-02-09 - Release version 1.16.29: Supporting xcpretty and string replacements in files<br />

## Features
1. Compilation and siging of iOS applications
2. Distribution of iOS applications
3. Versioning of iOS applications
4. One-step HockeyApp deployment
5. Packaging of iOS applications (.ipa & .dSYM) incl. unlock/lock keychain for deployment to Nexus/Artifactory
6. Compilation of universal iOS frameworks
7. Packaging of iOS frameworks for deployment to Nexus/Artifactory
8. Use Multiple executions e.g for branding or customizing of apps. (Different app icon names, different display names etc.)
9. Compilation of macosx frameworks
10. Customizing / flaovouring of apps replacing assets folders
11. Change app displayname without XCode Project changes
12. Change Version without XCode Project changes
13. Change App Icons without XCode Project changes
14. Set the path to Xcode installation for the build (xcodeVersion parameter). The path is set via xcode-select command, which runs under sudo (current user must be added to /etc/sudoers)
15. Integrates xctests execution and ocunit2junit test result conversion,


## Requirements
1. The plugin relies on several tools that are only available on Mac OS X: xcodebuild, xcrun and agvtool.  Install the Xcode Command Line Tools (Xcode -> Preferences... -> Downloads).  
2. To let maven-ios-plugin take care of versioning, be sure to set 'Versioning System' in the project settings to `apple-generic`

## Maven Goals

### ios:build
Compiles the application and generates an IPA package

**Parameters**

1. ios.sourceDir			    (default: src/ios)
2. ios.appName     		        (required)  is also the name of the bundle identifier
3. ios.classifier     		    will be added to the .ipa file name
4. ios.buildXCArchiveEnabled       (default: true) flag for iOS export to xcarchive enabled. If false the .app will be generated instead of xcarchive. You must set the xcode "scheme" value. Also the XCode scheme must be shared in the xcode project!
5. ios.scheme                   Is necessary for xcarchive builds. XCode Version > 6. The scheme must be "shared" within the xcode project!
6. ios.sdk					    (default: iphoneos)
7. ios.codeSignIdentity
8. ios.configuration		    (default: Release)  Release or Debug
9. ios.buildId                  (The build number. e.g. 1234) For using jenkins as build server use ${env.BUILD_NUMBER} here
10. ios.target                   (The Xcode build target)
11. ios.keychainPath             (The file system path to the keychain file) e.g. /Users/lestdev/Library/Keychains/letsdev.keychain
12. ios.keychainPassword        (The keychain password to use for unlock keychain) Before the build the keychain will be unlocked and locked again after the build.
13. ios.infoPlist               (default: projectName/projectName-Info.plist) The path to the Info.plist, relative to the project directory.
14. ios.ipaVersion              (The version number for the IPA, different to the maven project version)
15. ios.fileReplacements        (List of files or directories to replace while executing build)
16. ios.projectName             (The name of the project.)
17. ios.provisioningProfileUUID (The UUID of the provisioning profile to be used. If not set the default provisioning profile will be used instead)
18. ios.bundleIdentifier        (The bundle identifier to overwrite in info plist. If not set the default bundle identifier will be used instead)
19. ios.displayName             (The display name to overwrite in info plist. If not set the default display name will be used instead)
20. ios.appIconName             (The app icon name to overwrite in info plist. If not set the default app icon name will be used instead. e.g. <appIconName>free-icon.png</appIconName>)
21. ios.iOSFrameworkBuild       (flag for building iOS frameworks in multi execution environment)
22. ios.iphoneosArchitectures   (default: arm64 armv7) architectures build with iphoneos sdk
23. ios.iphonesimulatorArchitectures (default: i386 x86_64) architectures build with iphonesimulator sdk (only used for framework builds)
24. ios.iphonesimulatorBitcodeEnabled (default: true) determines if -fembed-bitcode switch is enabled for iphonesimulator builds
25. ios.gccPreprocessorDefinitions (optional) properties delivered to xcodebuild via GCC_PREPROCESSOR_DEFINITIONS
26. ios.macOSFrameworkBuild       (flag for building macosx frameworks)
27. ios.codeSigningEnabled           (default: true) Enabled or disable code signing for the app
28. ios.codeSigningWithResourceRulesEnabled   (default: false) flag for iOS code signing with resources rules enabled. Following will be added to code sign execution: <pre>CODE_SIGN_RESOURCE_RULES_PATH=$(SDKROOT)/ResourceRules.plist</pre> . This was necessary from iOS SDK 6.1 until 8.0
29. ios.codeSignEntitlements    (default {scheme}/{target}.entitlements, in directory of .xcodeproj file)
30. ios.cocoaPodsEnabled        (default: false) Determines if the project contains Cocoapods dependencies. If set, the dependencies will be installed during execution. When Cocoapods is enabled, a given appName will not passed to the xcodebuild command.
31. ios.iTunesConnectUsername   username to login to iTunesConnect
32. ios.iTunesConnectPassword   password to login to iTunesConnect
33. ios.xcodeVersion            The path to the xcode version, which should be used for the build process
34. ios.xcTestsScheme           Specifies the scheme, used for the execution of xctests
35. ios.xcTestsDestination      Specifies the destination, used for the execution of xctests
36. ios.xcTestsSdk              Specifies the sdk, used for the execution of xctests
37. ios.provisioningProfileSpecifier           Specifies PROVISIONING_PROFILE_SPECIFIER in project file
38. ios.developmentTeam           Specifies DEVELOPMENT_TEAM in project file
39. ios.resetSimulators           Specifies if iphone simulators should be resetted before testing
40. ios.xcodeBuildParameters      Added xcodeBuildParameters parameter
41. ios.xcodeExportOptions      Added xcodeExportOptions parameter (parameter defining values for exportOptionsPlist flag)
42. ios.stringReplacements        (List of strings to replace while executing build)
43. ios.targetDependencies      (List of strings, naming the target dependencies of the target)

### ios:deploy
Deploys the IPA package as well as the generated dSYM.zip to HockeyApp
Also deploys a ios framework. Then the dependency type is "ios-framework". The framework folder will be compressed by zip and then deployed as zip.

**Parameters**

1. ios.sourceDir
2. ios.appName
3. ios.scheme
4. ios.sdk
5. ios.codeSignIdentity
6. ios.configuration
7. ios.buildId
8. ios.hockeyAppToken
9. ios.releaseNotes
10. ios.deployIpaPath
11. ios.deployIconPath

## Getting started with ios-maven-plugin and Jenkins

**Use Packaging to build iOS-Framework or IPA**

	<project>
		<groupId>de.letsdev.ios.app.maven</groupId>
		<artifactId>maven-ios-project</artifactId>
		<packaging>ipa</packaging> <!-- <packaging>ios-framework</packaging> -->

**Configure a basic POM for your iOS project or module and add:**

    <plugin>
        <groupId>de.letsdev.maven.plugins</groupId>
        <artifactId>maven-ios-plugin</artifactId>
        <version>1.0</version>
        <extensions>true</extensions>                
        <configuration>
            <codeSignIdentity>iPhone Distribution: ACME Inc</codeSignIdentity>
            <appName>AcmeApp</appName>
        </configuration>				                
    </plugin>

    
**Use the maven-dependency-plugin to unpack dependencies**
    
    <plugin>
	    <groupId>org.apache.maven.plugins</groupId>
	    <artifactId>maven-dependency-plugin</artifactId>
	    <version>2.4</version>
	    <executions>
	      <execution>
	        <id>unpack-ios-dependencies</id>
	        <phase>compile</phase>
	        <goals>
	          <goal>unpack-dependencies</goal>
	        </goals>
	        <configuration>
	          <outputDirectory>${project.build.directory}/ios-dependencies</outputDirectory>
	        </configuration>
	      </execution>
	    </executions>
	 </plugin>
            
**Compile to verify**

    mvn clean compile

**Allow jenkins to access your keychain**

To sign the package, unlock the keychain on the jenkins node. The two commands below can be set up as a pre-build shell script.

```
    <plugin>
	   <groupId>de.letsdev.maven.plugins</groupId>
	      <artifactId>maven-ios-plugin</artifactId>
	      <version>1.12</version>
	      <extensions>true</extensions>
	      <configuration>
	          <codeSignIdentity>iPhone Distribution: let's dev iOS App Development</codeSignIdentity>
	          <appName>MaveniOSApp</appName>
		      <scheme>letsdev</scheme>
			  <buildId>${env.BUILD_NUMBER}</buildId>
			  <configuration>Release</configuration>
              <keychainPath>/Users/letsdev/Library/Keychains/letsdev.keychain</keychainPath>
              <keychainPassword>theKeyChainPassword</keychainPassword>
	      </configuration>
	</plugin>
```

**Build a ios maven framework**

***Attention***
The filesystem structure must look like that.

```
src/ios/LDMyiOSFramework/LDMyiOSFramework <- The sources of the project main target
src/ios/LDMyiOSFramework/LDMyiOSFramework.xcodeproj  <- by convetion the xcode project file has to be here
src/ios/LDMyiOSFramework/framework <- the place for the framework target
pom.xml
```

The pom.xml has to be adjusted like following:

Snippet:

```
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>de.letsdev.ios.frameworks</groupId>
    <artifactId>LDMyiOSFramework</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>ios-framework</packaging>

    <!-- Plugin configuration -->

                <plugin>
                    <groupId>de.letsdev.maven.plugins</groupId>
                    <artifactId>maven-ios-plugin</artifactId>
                    <version>1.9.3</version>
                    <extensions>true</extensions>
                    <configuration>
                        <appName>LDMyiOSFramework</appName>
                        <!--<target>framework</target>-->  <!-- framework xcode target is default here -->
                        <buildId>${env.BUILD_NUMBER}</buildId>
                        <configuration>Release</configuration>
                    </configuration>
                </plugin>
    <!-- Plugin configuration -->

</project>
```

**Use iOS Frameworks with the maven plugin**

Configure the dependency plugin to unpack the ios framework by the dependency plugin.

```
...

    <plugin>
	    <groupId>org.apache.maven.plugins</groupId>
	    <artifactId>maven-dependency-plugin</artifactId>
	    <version>2.4</version>
	    <executions>
	      <execution>
	        <id>unpack-ios-dependencies</id>
	        <phase>compile</phase>
	        <goals>
	          <goal>unpack-dependencies</goal>
	        </goals>
	        <configuration>
	          <outputDirectory>${project.build.directory}/ios-dependencies</outputDirectory>
	        </configuration>
	      </execution>
	    </executions>
	 </plugin>

...
```

Add the dependency in the pom.xml of your project into dependencies section.

```
...
        <dependency>
            <groupId>de.letsdev.ios.frameworks</groupId>
            <artifactId>LDMyiOSFramework</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <type>ios-framework</type>
        </dependency>
...
```

**Build universal iOS Framework in multi execution environment**

Configure the maven plugin to build an universal framework in one execution block.

```
...

<plugin>
    <groupId>de.letsdev.maven.plugins</groupId>
    <artifactId>maven-ios-plugin</artifactId>
    <extensions>true</extensions>
    <executions>
        ...
        <execution>
            <id>my-framework</id>
            <goals>
                <goal>build</goal>
                <goal>package</goal>
                <goal>deploy</goal>
            </goals>
            <configuration>
                <appName>LDMyiOSFramework</appName>
                <projectName>LDMyiOSFramework</projectName>
                <target>LDMyiOSFramework</target>
                <infoPlist>Info.plist</infoPlist>
                <buildId>${env.BUILD_NUMBER}</buildId>
                <ipaVersion>${project.version}</ipaVersion>
                <configuration>Release</configuration>
                <iOSFrameworkBuild>true</iOSFrameworkBuild>
                <iphoneosArchitectures>arm64 armv7</iphoneosArchitectures>
                <iphonesimulatorArchitectures>i386 x86_64</iphonesimulatorArchitectures>
            </configuration>
        </execution>
    </executions>
</plugin>

...
```

**Build project containing Cocoapods dependencies**

```
...

<plugin>
    <groupId>de.letsdev.maven.plugins</groupId>
    <artifactId>maven-ios-plugin</artifactId>
    <extensions>true</extensions>
    <executions>
        ...
        <execution>
            <id>my-project</id>
            <goals>
                <goal>build</goal>
                <goal>package</goal>
                <goal>deploy</goal>
            </goals>
            <configuration>
                <appName>LDMyiOSProject</appName>
                <cocoaPodsEnabled>true</cocoaPodsEnabled>
            </configuration>
        </execution>
    </executions>
</plugin>

...
```

Cocoapods dependencies will be installed via the "pod install" command.

**Deploy to HockeyApp**

To deploy to HockeyApp add `-Dios.hockeyAppToken=YOUR_TOKEN` as an argument and invoke `mvn ios:deploy`.

### Tips
1. ios-maven-plugin sets the CFBundleShortVersionString to the Maven project version by default. You can override this behaviour by adding the `-Dios.version` argument.
2. If you use buildId add CFBuildNumber to your Info.plist-file.
3. To set CFBuildNumber to the svn revision or git commit add `-Dios.buildId=$SVN_REVISION` or `-Dios.buildId=$GIT_COMMIT` respectively.

*WARNING: This is a work in progress, use with care.*
