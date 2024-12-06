// TODO share installation as test utils https://maven.apache.org/plugins/maven-invoker-plugin/integration-test-mojo.html#addTestClassPath
def p = "bash $basedir/install-fnm.sh".execute()
p.waitForProcessOutput(System.out, System.err)