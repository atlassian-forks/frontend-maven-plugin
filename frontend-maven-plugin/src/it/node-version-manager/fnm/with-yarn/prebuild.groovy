def p = "bash $basedir/install-fnm.sh".execute()
p.waitFor()
println p.text