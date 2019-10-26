# ios-maven-plugin

[![Build Status](https://secure.travis-ci.org/letsdev/maven-ios-plugin.png)](http://travis-ci.org/letsdev/maven-ios-plugin)

The ios-maven-plugin plugs in to the Maven build lifecycle to automate compilation and deployment of iOS applications. This enables continuous integration for the iOS platform with ease.

This is a plugin provided by let's dev GmbH & Co.KG

https://www.letsdev.de - professional mobile solutions


## Configuration

### App build (minimal configuration)

```
...

<plugin>
    <groupId>de.letsdev.maven.plugins</groupId>
    <artifactId>ios-maven-plugin</artifactId>
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