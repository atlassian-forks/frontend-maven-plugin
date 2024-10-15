import org.codehaus.plexus.util.FileUtils

String buildLog = FileUtils.fileRead(new File(basedir, 'build.log'))
assert buildLog.contains('Installing node with FNM') : 'Node has been installed with a different version manager'
assert buildLog.contains('error: Can\'t find version in dotfiles. Please provide a version manually to the command.') : 'Node installation hasn\'t failed with a descriptive message'
