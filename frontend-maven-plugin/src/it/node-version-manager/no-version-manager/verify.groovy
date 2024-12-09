import org.codehaus.plexus.util.FileUtils

String buildLog = FileUtils.fileRead(new File(basedir, 'build.log'))
assert buildLog.contains('You have configured `useNodeVersionManager=true` but node version manager couldn\'t be identified') : 'Should notify the user about missing node version manager'
assert buildLog.contains('Installing node version v20.15.1') : 'Should trigger standard installation'
