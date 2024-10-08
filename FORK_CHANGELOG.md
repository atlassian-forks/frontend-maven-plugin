# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [Unreleased]

### Added

- [DCA11Y-1145]: Automatic version detection of the Node version from `.tool-versions`, `.node-version`, and `.nvmrc` files
- [DCA11Y-1145]: The configuration property `nodeVersionFile` to specify a file that can be read in `install-node-and-npm`, `install-node-and-pnpm`, and `install-node-and-yarn`

### Changed

- [DCA11Y-1145]: Now tolerant of `v` missing or present at the start of a Node version

[DCA11Y-1145]: https://hello.jira.atlassian.cloud/browse/DCA11Y-1145
[unreleased]: https://github.com/olivierlacan/keep-a-changelog/compare/v1.15.0...HEAD
