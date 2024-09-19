import org.codehaus.plexus.util.FileUtils

String buildLog = FileUtils.fileRead(new File(basedir, 'build.log'))
assert buildLog.contains('BUILD SUCCESS') : 'build was not successful'

assert !new File(basedir, 'with-npm/node').exists() : "with-npm: Node was installed bypassing version manager"
assert new File(basedir, 'with-npm/node_modules').exists() : "with-npm: Node modules were not installed in the base directory"

assert !new File(basedir, 'with-yarn/node/node').exists() : "with-yarn: Node was installed bypassing version manager"
assert new File(basedir, 'with-yarn/node/yarn').exists() : "with-yarn: Yarn wrapper has been downloaded"
assert new File(basedir, 'with-yarn/node_modules').exists() : "with-yarn: Node modules were not installed in the base directory"