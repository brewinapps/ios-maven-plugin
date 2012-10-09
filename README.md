# maven-ios-plugin

#[![Build Status](https://secure.travis-ci.org/letsdev/maven-ios-plugin.png)](http://travis-ci.org/letsdev/maven-ios-plugin)

The maven-ios-plugin plugs in to the Maven build lifecycle to automate compilation and deployment of iOS applications. This enables continuous integration for the iOS platform with ease.

This Plugin is based on Brewin' Apps' ios-maven-plugin. Thanks.

## Features
1. Compilation of iOS applications
2. Distribution of iOS applications
3. Versioning of iOS applications
4. One-step HockeyApp deployment
5. Packaging of iOS applications (.ipa & .dSYM) incl. unlock/lock keychain for deployment to Nexus/Artifactory
6. Packaging of iOS frameworks for deployment to Nexus/Artifactory

## Requirements
1. The plugin relies on several tools that are only available on Mac OS X: xcodebuild, xcrun and agvtool.  Install the Xcode Command Line Tools (Xcode -> Preferences... -> Downloads).  
2. To let maven-ios-plugin take care of versioning, be sure to set 'Versioning System' in the project settings to `apple-generic`

## Maven Goals

### ios:build
Compiles the application and generates an IPA package

**Parameters**

1. ios.sourceDir			(default: src/ios)
2. ios.appName				(required)
3. ios.scheme
4. ios.sdk					(default: iphoneos)
5. ios.codeSignIdentity 	(required)
6. ios.configuration		(default: Release)  Release or Debug
7. ios.buildId              (The build number. e.g. 1234) For using jenkins as build server use ${env.BUILD_NUMBER} here
8. ios.target               (The Xcode build target)
9. ios.keychainPath         (The file system path to the keychain file) e.g. /Users/lestdev/Library/Keychains/letsdev.keychain
9. ios.keychainPassword     (The keychain password to use for unlock keychain) Befor the build the keychain will be unlocked and locked again after the build.

### ios:deploy
Deploys the IPA package as well as the generated dSYM.zip to HockeyApp

**Parameters**

1. ios.sourceDir
2. ios.appName
3. ios.scheme
4. ios.sdk
5. ios.codeSignIdentity
6. ios.configuration
8. ios.buildId
9. ios.hockeyAppToken
10. ios.releaseNotes

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

    <plugin>
	   <groupId>de.letsdev.maven.plugins</groupId>
	      <artifactId>maven-ios-plugin</artifactId>
	      <version>1.1</version>
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

**Deploy to HockeyApp**

To deploy to HockeyApp add `-Dios.hockeyAppToken=YOUR_TOKEN` as an argument and invoke `mvn ios:deploy`.

### Tips
1. ios-maven-plugin sets the CFBundleShortVersionString to the Maven project version by default. You can override this behaviour by adding the `-Dios.version` argument.
2. If you use buildId add CFBuildNumber to your Info.plist-file.
3. To set CFBuildNumber to the svn revision or git commit add `-Dios.buildId=$SVN_REVISION` or `-Dios.buildId=$GIT_COMMIT` respectively.

*WARNING: This is a work in progress, use with care.*
