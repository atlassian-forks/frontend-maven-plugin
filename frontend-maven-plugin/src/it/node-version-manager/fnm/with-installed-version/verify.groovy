import org.codehaus.plexus.util.FileUtils

String buildLog = FileUtils.fileRead(new File(basedir, 'build.log'))
assert buildLog.contains('Installing node with FNM') : 'Node has been installed with a different version manager'

assert !new File(basedir, 'node').exists() : "Node was installed bypassing version manager"
assert new File(basedir, 'node_modules').exists() : "Node modules were not installed in the base directory"

assert buildLog.contains('.fnm/node-versions/v20.15.1/installation/bin/npm, ci') : 'npm was called from a different node version'