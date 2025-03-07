def p = "bash $basedir/install-fnm.sh".execute()
p.waitForProcessOutput(System.out, System.err)