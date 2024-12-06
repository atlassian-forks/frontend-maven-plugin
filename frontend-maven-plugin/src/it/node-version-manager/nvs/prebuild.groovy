def p = "bash $basedir/install-nvs.sh".execute()
p.waitForProcessOutput(System.out, System.err)