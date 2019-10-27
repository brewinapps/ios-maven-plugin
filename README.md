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
       <execution>
            <id>maven-ios-example-project</id>
            <goals>
                <goal>build</goal>
                <goal>package</goal>
                <goal>deploy</goal>
            </goals>  
            <configuration>
                <appName>example-project</appName>
                <projectName>ios-maven-example-project</projectName>
                <scheme>ios-maven-example-project</scheme>
                <infoPlist>ios-maven-example-project/Info.plist</infoPlist>
                <bundleIdentifier>de.letsdev.ios-maven-example-project</bundleIdentifier>
                <codeSignIdentity>iPhone Distribution: let&apos;s dev GmbH &amp; Co. KG</codeSignIdentity>
                <keychainPath>${user.home}/Library/Keychains/ld-enterprise.keychain</keychainPath>
                <keychainPassword>ld-enterprise</keychainPassword>
                <provisioningProfileName>ldentwildcarddistribution</provisioningProfileName>
            </configuration> 
        </execution>
    </executions>
</plugin>

...
```

### Framework build

```
...

<plugin>
    <groupId>de.letsdev.maven.plugins</groupId>
    <artifactId>ios-maven-plugin</artifactId>
    <extensions>true</extensions>
    <executions>
       <execution>
            <id>maven-ios-example-project</id>
            <goals>
                <goal>build</goal>
                <goal>package</goal>
                <goal>deploy</goal>
            </goals>  
            <configuration>
                <appName>example-project</appName>
                <projectName>ios-framework-maven-example</projectName>
                <scheme>ios-framework-maven-example</scheme>
                <infoPlist>ios-framework-maven-example/Info.plist</infoPlist>
                <iOSFrameworkBuild>true</iOSFrameworkBuild>
                <iphoneosArchitectures>arm64</iphoneosArchitectures>
                <iphonesimulatorArchitectures>x86_64</iphonesimulatorArchitectures>
            </configuration> 
        </execution>
    </executions>
</plugin>

...
```          

## Examples

see /examples directory