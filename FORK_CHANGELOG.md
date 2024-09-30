# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased] 1.15.0-atlassian-1

### Added

- [DCA11Y-1145]: Added support for node version managers (fnm, mise, asdf, nvm, nvs) to install node and run npm tasks. Use of version manager is enabled by default and can be controlled with `useNodeVersionManager` configuration property.
- [DCA11Y-1145]: Automatic version detection of the Node version from `.tool-versions`, `.node-version`, and `.nvmrc` files
- [DCA11Y-1145]: The configuration property `nodeVersionFile` to specify a file that can be read in `install-node-and-npm`, `install-node-and-pnpm`, and `install-node-and-yarn`


### Changed

- [DCA11Y-1145]: `nodeVersion` property is not required any more, if `useNodeVersionManager` is not set to false
- [DCA11Y-1145]: Now tolerant of `v` missing or present at the start of a Node version.