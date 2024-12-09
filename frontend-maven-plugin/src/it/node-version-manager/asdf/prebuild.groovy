def p = "bash $basedir/install-asdf.sh".execute()
p.waitForProcessOutput(System.out, System.err)