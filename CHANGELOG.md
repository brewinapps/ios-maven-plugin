# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.18.5] - 2019-10-18
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