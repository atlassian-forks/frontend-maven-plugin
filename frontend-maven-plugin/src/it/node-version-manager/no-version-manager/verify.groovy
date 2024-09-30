import org.codehaus.plexus.util.FileUtils

String buildLog = FileUtils.fileRead(new File(basedir, 'build.log'))
assert buildLog.contains('falling back to standard node installation') : 'Should notify the user about using standard installation'
assert buildLog.contains('Installing node version v20.15.1') : 'Should trigger standard installation'
