def p = "powershell $basedir/install-fnm.ps1".execute()
p.waitForProcessOutput(System.out, System.err)