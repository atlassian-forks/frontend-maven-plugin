def p = "bash $basedir/install-mise.sh".execute()
p.waitForProcessOutput(System.out, System.err)