# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased] 1.15.0-atlassian-1

### Added

- [DCA11Y-1145]: Added support for node version managers (fnm, mise, asdf, nvm, nvs) to install node and run npm tasks. Use of version manager is enabled by default and can be controlled with `useNodeVersionManager` configuration property.

### Changed

- [DCA11Y-1145]: `nodeVersion` property is not required any more, if `useNodeVersionManager` is not set to false.