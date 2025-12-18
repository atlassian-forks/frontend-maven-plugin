# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [1.15.1-atlassian-6] - 2025-12-18

### Added
- [DCA11Y-2444]: Useful thread names for frontend execution in multithreaded Maven builds instead of simply "Exec Stream Pumper"

## [1.15.1-atlassian-5] - 2025-08-14

### Fixed
- [DCA11Y-2101]: Node should actually be used from the version manager instead of whatever happened to be on the `PATH` environment variable when running `yarn`, `bower`, `corepack`, `ember`, `grunt`, `gulp`, `jspm`, `karma`, `npx`, & `pnpm` commands (Yarn, Bower, Corepack, .... Mojos).
- [DCA11Y-2115]: Corepack should be used from the version manager installation.

## [1.15.1-atlassian-4] - 2025-08-11

### Added
- [DCA11Y-1145]: Added support for using already installed node from default install directory of node version manager (fnm, mise, asdf, nvm, nvs). Use of version manager is enabled by default and can be controlled with `useNodeVersionManager` configuration property.
- [DCA11Y-1145]: Added support for explicitly defining node installation directory with `installedNodeDirectory` configuration property or `AFMP_INSTALLED_NODE_DIRECTORY` environment variable.

### Changed
- [DCA11Y-1145]: `nodeVersion` property is not required any more, if `useNodeVersionManager` is not set to false

### Fixed
- [DCA11Y-1504]: Local mise configuration files have now precedence over global ones
- [DCA11Y-1733]: Incremental compilation causing memory pressure from reading in entire files

## [1.15.1-atlassian-3]  - 2024-11-29

### Fixed

- [DCA11Y-1274]: Null arguments for mojos would fail the build
- [DCA11Y-1274]: Fix incremental with Yarn berry by fixing runtime detection
- [DCA11Y-1274]: Corepack Mojo incremental works
- [DCA11Y-1274]: Fix updating of digest versions without clean install
- [DCA11Y-1274]: Download dev metrics now correctly report PAC
- [DCA11Y-1145]: Fixed the legacy "downloadRoot" argument for PNPM & NPM installation

### Added

- [DCA11Y-1274]: ".flattened-pom.xml" & ".git" & ".node" to the excluded filenames list after finding it in Jira
- [DCA11Y-1274]: Log message indicating how much time is saved

## [1.15.1-atlassian-2] - 2024-11-26

### Added

- [DCA11Y-1274]: Incremental builds for Yarn, Corepack and NPM goals 

## [1.15.1-atlassian-1] - 2024-11-25

- [DCA11Y-1145]: Automatic version detection of the Node version from `.tool-versions`, `.node-version`, and `.nvmrc` files
- [DCA11Y-1145]: The configuration property `nodeVersionFile` to specify a file that can be read in `install-node-and-npm`, `install-node-and-pnpm`, and `install-node-and-yarn`

### Changed

- [DCA11Y-1145]: Now tolerant of `v` missing or present at the start of a Node version


[DCA11Y-2444]: https://hello.jira.atlassian.cloud/browse/DCA11Y-2444
[DCA11Y-2115]: https://hello.jira.atlassian.cloud/browse/DCA11Y-2115
[DCA11Y-2101]: https://hello.jira.atlassian.cloud/browse/DCA11Y-2101
[DCA11Y-1733]: https://hello.jira.atlassian.cloud/browse/DCA11Y-1733
[DCA11Y-1504]: https://hello.jira.atlassian.cloud/browse/DCA11Y-1504
[DCA11Y-1274]: https://hello.jira.atlassian.cloud/browse/DCA11Y-1274
[DCA11Y-1145]: https://hello.jira.atlassian.cloud/browse/DCA11Y-1145

[unreleased]: https://github.com/atlassian-forks/frontend-maven-plugin/compare/frontend-plugins-1.15.1-atlassian-6...HEAD
[1.15.1-atlassian-6]: https://github.com/atlassian-forks/frontend-maven-plugin/compare/frontend-plugins-1.15.1-atlassian-5...frontend-plugins-1.15.1-atlassian-6
[1.15.1-atlassian-5]: https://github.com/atlassian-forks/frontend-maven-plugin/compare/frontend-plugins-1.15.1-atlassian-4...frontend-plugins-1.15.1-atlassian-5
[1.15.1-atlassian-4]: https://github.com/atlassian-forks/frontend-maven-plugin/compare/frontend-plugins-1.15.1-atlassian-3...frontend-plugins-1.15.1-atlassian-4
[1.15.1-atlassian-3]: https://github.com/atlassian-forks/frontend-maven-plugin/compare/frontend-plugins-1.15.1-atlassian-2...frontend-plugins-1.15.1-atlassian-3
[1.15.1-atlassian-2]: https://github.com/atlassian-forks/frontend-maven-plugin/compare/frontend-plugins-1.15.1-atlassian-1...frontend-plugins-1.15.1-atlassian-2
[1.15.1-atlassian-1]: https://github.com/atlassian-forks/frontend-maven-plugin/compare/frontend-plugins-1.15.1-atlassian-1-16519678...frontend-plugins-1.15.1-atlassian-1
[1.15.1-atlassian-1-16519678]: https://github.com/atlassian-forks/frontend-maven-plugin/compare/frontend-plugins-1.15.1...frontend-plugins-1.15.1-atlassian-1-16519678
