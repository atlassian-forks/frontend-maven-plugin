import org.codehaus.plexus.util.FileUtils

String buildLog = FileUtils.fileRead(new File(basedir, 'build.log'))
assert buildLog.contains('Using FNM version manager') : 'Node has been installed with a different version manager'

assert !new File(basedir, 'node/node').exists() : "Node was installed bypassing version manager"
assert new File(basedir, 'node_modules').exists() : "Node modules were not installed in the base directory"
assert new File(basedir, 'dist').exists() : "Webpack has not generated distribution output"

assert buildLog.contains('v16.20.2') : 'Node was not provided from the version manager'
