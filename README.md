# maven-ios-plugin

#[![Build Status](https://secure.travis-ci.org/letsdev/maven-ios-plugin.png)](http://travis-ci.org/letsdev/maven-ios-plugin)

The maven-ios-plugin plugs in to the Maven build lifecycle to automate compilation and deployment of iOS applications. This enables continuous integration for the iOS platform with ease.

This is a plugin provided by let's dev GmbH & Co.KG

http://www.letsdev.de - professional mobile solutions

## Last-Changes

2015-04-21 - Added support for building universal frameworks (architectures arm64, armv7, armv7s, i386, x86_64 supported)
2015-04-04 - Added auto generation of deploy plist file<br />
2014-11-02 - Release version 1.9.3<br />
2014-11-02 - Adjusted to xcode 6.1 and iOS8 build, some issues occurred here<br />
2014-11-02 - Prepared build with an clean step<br />
2014-11-02 - Fixed issue with precompiled headers path in /var/folders/...<br />


## Features
1. Compilation of iOS applications
2. Distribution of iOS applications
3. Versioning of iOS applications
4. One-step HockeyApp deployment
5. Packaging of iOS applications (.ipa & .dSYM) incl. unlock/lock keychain for deployment to Nexus/Artifactory
6. Compilation of universal iOS frameworks
7. Packaging of iOS frameworks for deployment to Nexus/Artifactory
8. Use Multiple executions e.g for branding or customizing of apps. (Different app icon names, different display names etc.)

## Requirements
1. The plugin relies on several tools that are only available on Mac OS X: xcodebuild, xcrun and agvtool.  Install the Xcode Command Line Tools (Xcode -> Preferences... -> Downloads).  
2. To let maven-ios-plugin take care of versioning, be sure to set 'Versioning System' in the project settings to `apple-generic`

## Maven Goals

### ios:build
Compiles the application and generates an IPA package

**Parameters**

1. ios.sourceDir			    (default: src/ios)
2. ios.appName     		        (required)  is also the name of the bundle identifier
3. ios.scheme
4. ios.sdk					    (default: iphoneos)
5. ios.codeSignIdentity
6. ios.configuration		    (default: Release)  Release or Debug
7. ios.buildId                  (The build number. e.g. 1234) For using jenkins as build server use ${env.BUILD_NUMBER} here
8. ios.target                   (The Xcode build target)
9. ios.keychainPath             (The file system path to the keychain file) e.g. /Users/lestdev/Library/Keychains/letsdev.keychain
10. ios.keychainPassword        (The keychain password to use for unlock keychain) Before the build the keychain will be unlocked and locked again after the build.
11. ios.infoPlist               (default: projectName/projectName-Info.plist) The path to the Info.plist, relative to the project directory.
12. ios.ipaVersion              (The version number for the IPA, different to the maven project version.)
13. ios.assetsDirectory         (The name of the assets folder. The assets folder in your project has to be "assets".)
14. ios.projectName             (The name of the project.)
15. ios.provisioningProfileUUID (The UUID of the provisioning profile to be used. If not set the default provisioning profile will be used instead.)
16. ios.bundleIdentifier        (The bundle identifier to overwrite in info plist. If not set the default bundle identifier will be used instead.)
17. ios.displayName             (The display name to overwrite in info plist. If not set the default display name will be used instead.)
18. ios.appIconName             (The app icon name to overwrite in info plist. If not set the default app icon name will be used instead. e.g. <appIconName>free-icon.png</appIconName>)
19. ios.iOSFrameworkBuild       (flag for building iOS frameworks in multi execution environment)
20. ios.iphoneosArchitectures   (default: arm64 armv7 armv7s) architectures build with iphoneos sdk
21. ios.iphonesimulatorArchitectures (default: i386 x86_64) architectures build with iphonesimulator sdk (only used for framework builds)

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
	      <version>1.9.3</version>
	      <extensions>true</extensions>
	      <configuration>
	          <codeSignIdentity>iPhone Distribution: let's dev iOS App Development</codeSignIdentity>
	          <appName>MaveniOSApp</appName>
		      <target>letsdev</target>
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
                <iphoneosArchitectures>arm64 armv7 armv7s</iphoneosArchitectures>
                <iphonesimulatorArchitectures>i386 x86_64</iphonesimulatorArchitectures>
            </configuration>
        </execution>
    </executions>
</plugin>

...
```


**Deploy to HockeyApp**

To deploy to HockeyApp add `-Dios.hockeyAppToken=YOUR_TOKEN` as an argument and invoke `mvn ios:deploy`.

### Tips
1. ios-maven-plugin sets the CFBundleShortVersionString to the Maven project version by default. You can override this behaviour by adding the `-Dios.version` argument.
2. If you use buildId add CFBuildNumber to your Info.plist-file.
3. To set CFBuildNumber to the svn revision or git commit add `-Dios.buildId=$SVN_REVISION` or `-Dios.buildId=$GIT_COMMIT` respectively.

*WARNING: This is a work in progress, use with care.*
