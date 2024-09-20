import org.codehaus.plexus.util.FileUtils

String buildLog = FileUtils.fileRead(new File(basedir, 'build.log'))
assert buildLog.contains('Installing node with FNM') : 'Node has been installed with a different version manager'

assert !new File(basedir, 'node').exists() : "with-npm: Node was installed bypassing version manager"
assert new File(basedir, 'node_modules').exists() : "with-npm: Node modules were not installed in the base directory"