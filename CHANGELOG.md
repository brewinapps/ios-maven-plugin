# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [UNRELEASED]
### Added
- SwiftPM support (added flag "swiftPmDisableAutomaticPackageResolution" to disable automatic package resolution; using Package.resolved file instead)

### Fixed
- installing cocoapods and carthage dependencies before xcodebuild clean command

## [1.20.0] - 2021-07-15
### Added
- carthage update command arguments

## [1.19.9] - 2021-05-11
### Added
- skipTests argument

## [1.19.8] - 2020-11-02
### Fixed
- using appName as name for xcarchive in order to prevent overriding xcarchive files

## [1.19.7] - 2020-10-29
### Fixed
- shutting down ios simulators

## [1.19.6] - 2020-10-29
### Fixed
- shutting down ios simulators

## [1.19.5] - 2020-10-28
### Fixed
- shutting down ios simulators

## [1.19.4] - 2020-07-17
### Fixed
- setting test sdk as optional parameter

## [1.19.3] - 2020-07-17
### Fixed
- setting build sdk as optional parameter

## [1.19.2] - 2020-06-18
### Added
- possibility to wrap xcodebuild commands with own executable

### Fixed
- bundle identifier detection from provisioning profile

## [1.19.1] - 2020-06-09
### Changed
- fixed issue with crash after setting some parameters in "Info.plist" file

### Added
- You can now add your own Info.plist parameters in pom

## [1.19.0] - 2020-04-09
### Added
- bundle identifier determination from .mobileprovision file

### Fixed
- removing of simulator architectures for maven test goal
- erasing simulator contents

## [1.18.11] - 2019-12-17
### Added
- xcframeworks will be generated per default (iOSXcFrameworkBuild), when building a iOS/macOS library

## [1.18.10] - 2019-11-26
### Changed
- added space to some parameters in ProjectTester
- provisioning profiles get set correctly now

### Added
- kills simulator application when resetting devices

## [1.18.9] - 2019-11-04
### Added
- added sdk argument for xcodebuild clean

## [1.18.8] - 2019-11-04
### Changed
- fixed xcpretty log file argument 

## [1.18.7] - 2019-10-31
### Added
- reading name from provisioning profile, setting provisioningProfileSpecifier 

### Changed
- adjusted integration of altool, supporting app store uploads with Xcode 11 

## [1.18.6] - 2019-10-28
### Added
- added xcpretty for test execution
- added support for Carthage dependencies (carthageEnabled)
- added configuration for xcode build directory (derivedDataPath)
- added configuration for xcode build directory while executing tests (xcTestsDerivedDataPath)
- integrated xcpretty-json-formatter (installation: gem install xcpretty-json-formatter)
- added configuration for provisioningProfileName
- supporting OCLint with xcpretty

### Changed
- erasing xcode simulator contents before running tests
- setting x86_64 as default simulator architecture
- setting iPhone X,OS=latest as default xc test destination
- xctest execution can be done with xcode workspace files
- stripping simulator architectures for app-store builds
