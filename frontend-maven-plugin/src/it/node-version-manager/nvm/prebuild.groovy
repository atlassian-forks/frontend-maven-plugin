def p = "bash $basedir/install-nvm.sh".execute()
p.waitForProcessOutput(System.out, System.err)