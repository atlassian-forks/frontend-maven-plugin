// TODO share installation as test utils https://maven.apache.org/plugins/maven-invoker-plugin/integration-test-mojo.html#addTestClassPath
def p = "bash $basedir/install-fnm.sh".execute()
p.waitFor()
println p.text

def p2 = "bash -c export XTESTX=test".execute()
p2.waitFor()
println p2.text