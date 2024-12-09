import org.codehaus.plexus.util.FileUtils

String buildLog = FileUtils.fileRead(new File(basedir, 'build.log'))
assert buildLog.contains('Requested node version v20.15.1 is not installed in version') : 'Node has been installed with a different version manager'

assert new File(basedir, 'node').exists() : "Node was installed using version manager"
assert new File(basedir, 'node_modules').exists() : "Node modules were not installed in the base directory"

assert buildLog.contains('node/node_modules/npm/bin/npm-cli.js, ci') : 'npm was called from a different node version'