# ios-maven-plugin

The ios-maven-plugin plugs in to the Maven build lifecycle to automate compilation, archiving and deployment of iOS applications. This enables continuous integration for the iOS platform with ease.

## Features
1. Compilation of iOS applications
2. Distribution of iOS applications
3. Versioning of iOS applications
4. One-step HockeyApp deployment

## Requirements
1. The plugin relies on several tools that are only available on Mac OS X: xcodebuild, xcrun and agvtool.
2. To let ios-maven-plugin take care of versioning, be sure to set 'Versioning System' in the project settings to `apple-generic`

## Maven Goals

### ios:build
Compiles the application and generates an IPA package

**Parameters**

1. ios.sourceDir
2. ios.appName
3. ios.scheme
4. ios.sdk
5. ios.codeSignIdentity
6. ios.configuration
7. ios.buildId

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

**Configure a basic POM for your iOS project or module and add:**

    <plugin>
        <groupId>com.brewinapps.maven.plugins</groupId>
        <artifactId>ios-maven-plugin</artifactId>
        <version>1.0</version>
        <configuration>
            <sourceDir>AppName</sourceDir>
            <appName>AppName</appName>
            <codeSignIdentity>iPhone Developer: Acme Inc</codeSignIdentity>
        </configuration>
        <executions>
            <execution>
                <goals>
                    <goal>build</goal>
                </goals>
            </execution>
        </executions>
    </plugin>

**Compile to verify**

    mvn clean compile

**Allow jenkins to access your keychain**

To sign the package, unlock the keychain on the jenkins node. The two commands below can be set up as a pre-build shell script.

    security list-keychains -s ~/Library/Keychains/jenkins.keychain
    security unlock-keychain -p CHANGEME ~/Library/Keychains/jenkins.keychain

**Deploy to HockeyApp**

To deploy to HockeyApp add `-Dios.hockeyAppToken=YOUR_TOKEN` as an argument and invoke `mvn ios:deploy`.

### Tips
1. ios-maven-plugin sets the CFBundleShortVersionString to the Maven project version by default. You can override this behaviour by adding the `-Dios.version` argument.
2. To set CFBundleVersion to the svn revision or git commit add `-Dios.buildId=$SVN_REVISION` or `-Dios.buildId=$GIT_COMMIT` respectively.

## Plans
1. Set up proper packaging for deployment to Nexus/Artifactory

*WARNING: This is a work in progress, use with care.*