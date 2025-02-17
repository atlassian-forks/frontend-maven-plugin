import org.codehaus.plexus.util.FileUtils

// assert
assert !new File(basedir, 'node/node').exists() : "Node was installed in the custom install directory"
assert new File(basedir, 'node_modules').exists() : "Node modules were not installed in the base directory"

String buildLog = FileUtils.fileRead(new File(basedir, 'build.log'))
assert !buildLog.contains('Using NVM') : 'Node has been installed with a version manager but should use provided node'
assert buildLog.contains('Provided node executable has version v20.15.1, but v20.16.0 was requested in configuration.') : 'FMP should warn about version mismatch'
