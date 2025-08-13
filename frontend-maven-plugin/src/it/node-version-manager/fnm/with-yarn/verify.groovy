import org.codehaus.plexus.util.FileUtils

String buildLog = FileUtils.fileRead(new File(basedir, 'build.log'))
assert buildLog.contains('Using FNM version manager') : 'Node has been installed with a different version manager'

assert !new File(basedir, 'node/node').exists() : "Node was installed bypassing version manager"
assert new File(basedir, 'node/yarn').exists() : "Yarn wrapper has been downloaded"
assert new File(basedir, 'node_modules').exists() : "Node modules were not installed in the base directory"

assert buildLog.contains('v12.3.1') : 'Node was not provided from the version manager'
