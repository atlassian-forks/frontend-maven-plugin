import org.codehaus.plexus.util.FileUtils

// assert
assert !new File(basedir, 'node').exists() : "Node was installed in the custom install directory"
assert new File(basedir, 'node_modules').exists() : "Node modules were not installed in the base directory"

String buildLog = FileUtils.fileRead(new File(basedir, 'build.log'))
assert buildLog.contains('Installing node with MISE') : 'Node has been installed with a different version manager'
